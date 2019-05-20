package com.zzx.executor.core;

import com.alibaba.fastjson.JSONObject;

import com.zzx.executor.dao.TradeEntityMapper;
import com.zzx.executor.entity.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.zzx.executor.util.ExecutorUtil.getRecordHash;

@Component
public class DataProcessor implements Runnable {
    private static final long CLEAR_INTERVAL = 24 * 3600 * 1000L;//24小时
    private static String UserParam = "Default";
    private static long lastClear = new Date().getTime();
    private static int timeInterval = 5;
    private static String reportURL = "http://adapter.3460a.cn/alipayNewTransfer/notifybank";
    private Queue<List<TradeEntity>> dataQueue = new ConcurrentLinkedQueue<>();
    private Map<String, TradeEntity> dataCache = new ConcurrentHashMap<>(50);


    @Autowired
    private UIController controller;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TradeEntityMapper tradeDao;

    @Scheduled(cron = "0 0 1 * * *")
    public void clearCache() {
        dataCache.clear();
        controller.clearCache();
    }


    public void initParams() {
        String url = controller.reportURL();
        if (!StringUtils.isEmpty(url)) {
            reportURL = url;
        }
        timeInterval = controller.getTimeInterval();
        UserParam = controller.getUserParam();
    }


    public boolean addData(List<TradeEntity> records) {
        if (records == null || records.size() == 0) {
            return false;
        }
        dataQueue.add(records);
        return true;
    }

    @Override
    public void run() {
        execute();
    }

    /**
     * 循环从数据队列（DataQueue）取数据并依次执行去重、上报、显示、保存
     */
    private void execute() {
        while (true) {
            try {
                //每24小时清理一次缓存
                if (timeToClear()){
                    lastClear = new Date().getTime();
                    clearCache();
                }
                List<TradeEntity> records = dataQueue.poll();

                if (records == null || records.size() == 0){
                    //显示(数据没有更新也刷新显示)
                    messageShow();
                    //没有新数据，则刷新频率降低，间隔为5s
                    Thread.sleep(5000);
                    continue;
                }
                //除重
                records = messageClear(records);
                if (records.size() != 0) {
                    //上报
                    records = messageReport(records);
                    //同步
                    synchToChache(records);
                }
                //显示
                messageShow();
                if (records.size() == 0) {
                    continue;
                }
                //保存
                messageSave(records);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private boolean timeToClear(){
        return new Date().getTime() - lastClear > CLEAR_INTERVAL;
    }

    private void synchToChache(List<TradeEntity> records){
        for (TradeEntity record: records){
            String hash = getRecordHash(record);
            dataCache.put(hash, record);
        }
    }
    public List<TradeEntity> messageReport(List<TradeEntity> records) {
        if (records == null || records.size() == 0) {
            return records;
        }
        for (TradeEntity record : records) {
            record.setReport(httpReport(record));
        }

        return records;
    }


    private boolean httpReport(TradeEntity record) {
        if (StringUtils.isEmpty(reportURL)) {
            return false;
        }
        int i = 0;
        while (i < 3) {
            i++;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject params = new JSONObject();
            params.put("tradeDate", record.getTradeDate());
            params.put("tradeTime", record.getTradeTime());
            params.put("money", record.getMoney());
            params.put("tradeType", record.getTradeType());
            params.put("remark", record.getRemark());
            params.put("identity", record.getIdentity());
            params.put("bank", record.getBank());
            params.put("bankAccount", record.getBankAccount());
            HttpEntity<String> requestEntity =
                    new HttpEntity<>(params.toJSONString(), headers);
            ResponseEntity<String> response = null;
            try {
                response = restTemplate.exchange("http://47.112.101.44:8983/pinduoduo/notify", HttpMethod.POST,
                        requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response != null && response.getBody().contains("success")) {
                return true;
            }
        }
        return false;
    }

    public List<TradeEntity> messageSave(List<TradeEntity> records) {
        if (records == null || records.size() == 0) {
            return records;
        }
        for (TradeEntity record : records) {
            tradeDao.insert(record);
        }
        return records;
    }

    public void messageShow() {
        controller.show(dataCache.values());
    }

    public boolean containsRecord(TradeEntity record) {
        if (record == null) {
            return true;
        }
        String hash = getRecordHash(record);
        return dataCache.containsKey(hash);
    }


    private List<TradeEntity> messageClearInMemory(List<TradeEntity> records) {
        if (records == null || records.size() == 0) {
            return new LinkedList<>();
        }
        List<TradeEntity> temp = new ArrayList<>();
        for (TradeEntity record : records) {
            String hash = getRecordHash(record);
            //内存中不存在
            if (!dataCache.containsKey(hash)) {
                dataCache.put(hash, record);
                temp.add(record);
            }
        }
        return temp;
    }

    public List<TradeEntity> messageClear(List<TradeEntity> records) {
        List<TradeEntity> temp = messageClearInMemory(records);

        return messageClearInDB(temp);
    }

    private List<TradeEntity> messageClearInDB(List<TradeEntity> records) {
        if (records == null || records.size() == 0) {
            return new LinkedList<>();
        }
        List<TradeEntity> temp = new ArrayList<>();

        for (TradeEntity record : records) {
            List<TradeEntity> dbRecord = checkExist(record);
            if (!exist(dbRecord)) {
                //数据库中不存在（除刚启动外，绝大部分情况走该分支）
                temp.add(record);
            } else {
                //数据库中已存在，更新数据库记录到内存（主要是更新上报情况）
                String hash = getRecordHash(record);
                dataCache.get(hash).setReport(dbRecord.get(0).getReport());
            }
        }
        return temp;
    }

    private boolean exist(List<TradeEntity> records) {
        return !(records == null || records.size() == 0);
    }

    private List<TradeEntity> checkExist(TradeEntity record) {
        if (record == null || record.getTradeDate() == null || record.getTradeTime() == null
                || StringUtils.isEmpty(record.getMoney())) {
            return null;
        }
        return tradeDao.checkExist(record.getTradeDate(), record.getTradeTime(),
                record.getMoney(), record.getBankAccount());
    }

    public static int getTimeInterval() {
        return timeInterval;
    }
}
