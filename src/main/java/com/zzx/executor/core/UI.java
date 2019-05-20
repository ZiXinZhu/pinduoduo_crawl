package com.zzx.executor.core;


import com.zzx.executor.entity.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

@Component
public class UI implements Runnable {
    private static String reportURL = "http://adapter.ivpai.com/alipayNewTransfer/notifybank";
    private UICore jDialog;
    private JTable table;
    private JTextField urlField;
    private JComboBox<String> intervalSelect;
    private JButton login;
    private JButton start;
    private JButton stop;
    private JTextField addtionField;
    private DefaultTableModel model;
    private boolean removing = false;//是否正在清空监控屏
    private Comparator<TradeEntity> comparator = new TimeComparator();



    @Autowired
    @Qualifier(value = "loginListener")
    private ActionListener loginListener;


    @Autowired
    @Qualifier(value = "startScanListener")
    private ActionListener startScanListener;

    @Autowired
    @Qualifier(value = "stopScanListener")
    private ActionListener stopScanListener;

    @Autowired
    private IEOperator ieOperator;


    //TODO 附加参数
    public void showName(String name){
        this.addtionField.setText(" "+name);
        //this.addtionField.setEnabled(false);
    }
    @Override
    public void run(){
        try {
            jDialog = new UICore();
            jDialog.initUI();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public UICore getCore(){
        return jDialog;
    }

    public class UICore{

        public void clearData(){
            if (model == null){
                return;
            }
            removing = true;
            model.getDataVector().removeAllElements();
            removing = false;
        }
        public void show(Collection<TradeEntity> records) {
            if (records == null || records.size() == 0 || model == null) {
                return;
            }
            while (removing){
                //如果正在清除监控数据，循环等待清理结束再继续显示
            }

            //升序排列
//            records.(comparator);
            model.getDataVector().removeAllElements();
            //升序排列后，越靠近现在的记录越晚插入监控屏，效率更高
            for (TradeEntity record:records) {
                insertToRightPlace(record);
            }
        }

        /**
         * 显示到正确位置
         * 依次比较得出正确位置
         * @param record
         */
        private void insertToRightPlace(TradeEntity record){
            int i = 0;
            Iterator var = model.getDataVector().iterator();

            while (var.hasNext()){
                Vector<String> row = (Vector<String>) var.next();
                String date = row.get(0);
                String dateTime = date + row.get(1);

                if (StringUtils.isEmpty(dateTime) || compare(record, dateTime) >= 0){
                    //正确的位置
                    break;
                }
                i++;
            }
            String tradeDate = record.getTradeDate();
            String tradeTime = record.getTradeTime();
            String money = record.getMoney();
            String tradeType = record.getTradeType();
            String remark = record.getRemark();
            String bank = record.getBank();
            String report = record.getReport()?"上报成功":"上报失败";

            String data[] = {tradeDate, tradeTime, money, tradeType, remark, bank, report};
            model.insertRow(i, data);
        }

        private int compare(TradeEntity o1, String o2) {
            char[] a = (o1.getTradeDate() + o1.getTradeTime()).toCharArray();
            char[] b = o2.toCharArray();
            int length = a.length;

            for (int i = 0; i < length; i++){
                if (a[i]>b[i]){
                    return 1;  //o1时间比o2时间大
                }else if (a[i]<b[i]){
                    return -1;
                }
            }
            return 0;
        }

        public String reportURL() {
            if (urlField == null){
                return "";
            }
            String url = urlField.getText();
            if (StringUtils.isEmpty(url)){
                return "";
            }
            return url;
        }

        public String getUserParam() {
            if (addtionField == null){
                return "";
            }
            return addtionField.getText();
        }

        public String getTimeInterval() {
            if (intervalSelect == null){
                return "5";
            }
            return (String)intervalSelect.getSelectedItem();
        }


        public void initUI() {
            JFrame jFram = new JFrame("weipay拼多多回调辅助工具");
            jFram.setSize(900, 550);
            jFram.setLocationRelativeTo(null);
            jFram.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            JPanel upPanel = new JPanel(new BorderLayout());
            upPanel.setBorder(BorderFactory.createTitledBorder("数据监控"));
            //upPanel.setSize(900,200);
            JPanel downPanel = new JPanel();
            downPanel.setBorder(BorderFactory.createTitledBorder("参数设置"));
            downPanel.setSize(900,400);
            //表格
            {
                Object[] columnNames = {"交易日期", "交易时间", "金额", "财务类型", "商户订单号","交易银行", "上报情况"};
                model = new DefaultTableModel(columnNames, 0);
                table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.getViewport().setBackground(Color.WHITE);
                upPanel.add(scrollPane, BorderLayout.CENTER);
            }

            JLabel url;
            {
                url = new JLabel("上报地址:");
                //TODO 展示上报地址
                urlField = new JTextField("",40);
                urlField.setText(" http://47.106.223.127:8983"); //在地址栏显示上报地址
                downPanel.add(url);
                downPanel.add(urlField);
            }

            JLabel interval;
            {
                String[] listData = new String[]{"5", "10", "15", "20"};
                interval = new JLabel("扫描周期(秒):");
                intervalSelect = new JComboBox<>(listData);
                intervalSelect.setSelectedIndex(2);
                downPanel.add(interval);
                downPanel.add(intervalSelect);
            }



            JLabel addtion;
            {
                //TODO 展示附加参数
                addtion = new JLabel("附加参数:");
                addtionField = new JTextField("",10);

                downPanel.add(addtion);
                downPanel.add(addtionField);
            }

            {
                login = new JButton("用户登录");
                login.addActionListener(loginListener);
                start = new JButton("开始扫描");
                start.addActionListener(startScanListener);
                stop = new JButton("停止扫描");
                stop.addActionListener(stopScanListener);

                downPanel.add(login);
                downPanel.add(start);
                downPanel.add(stop);
            }


            Box box = Box.createVerticalBox();
            Box row1 = Box.createHorizontalBox();
            row1.add(url);
            row1.add(urlField);
            row1.add(interval);
            row1.add(intervalSelect);
            //附加参数，暂取消显示
            row1.add(addtion);
            row1.add(addtionField);
            Box row2 = Box.createHorizontalBox();
            row2.add(login);
            row2.add(start);
            row2.add(stop);

            Box downBox = Box.createVerticalBox();
            downBox.add(row1);
            downBox.add(row2);
            downPanel.add(downBox);

            box.add(upPanel);
            box.add(downPanel);
            jFram.setContentPane(box);
            jFram.setResizable(false);
            jFram.setVisible(true);
            //关闭窗口时，调stop()关闭Quark.exe，否则进程中多个Quark.exe
            jFram.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    ieOperator.stop();
                }
            });
        }
    }

    /**
     * 按时间升序排列
     */
    public static class TimeComparator implements Comparator<TradeEntity> {

        /**
         * 日期、时间串起来比较先后
         * @param o1
         * @param o2
         * @return
         */
        @Override
        public int compare(TradeEntity o1, TradeEntity o2) {
            char[] a = (o1.getTradeDate() + o1.getTradeTime()).toCharArray();
            char[] b = (o2.getTradeDate() + o2.getTradeTime()).toCharArray();
            int length = a.length;

            for (int i = 0; i < length; i++){
                if (a[i]>b[i]){
                    return 1;
                }else if (a[i]<b[i]){
                    return -1;
                }
            }
            return 0;
        }
    }

}
