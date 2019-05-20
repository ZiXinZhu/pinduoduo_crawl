package com.zzx.executor.config;

import com.zzx.executor.core.DataProcessor;
import com.zzx.executor.core.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(value = "com.zzx.executor")
public class LifeCycleConfiguration {
    @Autowired
    private DataSourceProperties properties;

    @Autowired
    private ExecutorService threadPool;
    @Autowired
    private UI ui;
    @Autowired
    private DataProcessor processor;


    @Bean
    public RestTemplate restTemplate(){

//                拼多多 13252083691
//                密码   13252083691
        //TODO 生产环境数据库
        properties.setUrl("jdbc:mysql://rm-wz901zpz986sa75ecoo.mysql.rds.aliyuncs.com:3306/tradedata?serverTimezone=GMT%2B8&characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
        properties.setUsername("rootcmb");
        properties.setPassword("Cmb123456");
        //TODO 测试数据库
//        properties.setUrl("jdbc:mysql://123.207.231.159:3306/tradedata?serverTimezone=GMT%2B8&characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull&autoReconnect=true");
//        properties.setUsername("root");
//        properties.setPassword("1101648204");
        //TODO mysql驱动
        properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
        //TODO Other Properties
        return new RestTemplate();
    }

    @Bean
    public ExecutorService getThreadPool(){
        return Executors.newFixedThreadPool(5);
    }

    @Bean
    @Order(2111111111)
    public void initial() {
        threadPool.submit(ui);
        threadPool.submit(processor);
    }
}
