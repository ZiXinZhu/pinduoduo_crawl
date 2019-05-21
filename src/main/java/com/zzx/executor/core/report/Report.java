package com.zzx.executor.core.report;


import com.alibaba.fastjson.JSONObject;
import com.zzx.executor.dao.TradeEntityMapper;
import com.zzx.executor.entity.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@EnableScheduling
public class Report implements Runnable {

    @Autowired
    TradeEntityMapper mapper;
    @Autowired
    Report report;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExecutorService threadPool;

    @Lazy(false)
    @Scheduled(cron = "*/20 * * * * ?")
    public void report(){
        System.out.println("进入上报");
        threadPool.submit(report);
    }
    @Override
    public void run() {
        List<TradeEntity> list=get();
        for (TradeEntity record : list) {
            httpReport(record);
        }
    }

    private List<TradeEntity> get(){
        List<TradeEntity> list=mapper.rereport(gettime());
        System.out.println(list.toString());
        return list;
    }

    private void httpReport(TradeEntity record) {

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
        System.out.println("**************补单上报*************");
            if (response != null && response.getBody().contains("success")) {
               mapper.upda(record.getRemark()) ;
            }
    }


    public static String gettime() {
        Date date = new Date();
        long times = date.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(times);

        return time;
    }


}
