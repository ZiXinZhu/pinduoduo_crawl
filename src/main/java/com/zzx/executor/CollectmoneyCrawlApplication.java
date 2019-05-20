package com.zzx.executor;

import com.zzx.executor.config.LifeCycleConfiguration;
import com.zzx.executor.util.ExecutorUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class CollectmoneyCrawlApplication {

    private static final String path = "C:\\Program Files";

    public static void main(String[] args) {
        System.setProperty("java.awt.headless","false");
        ExecutorUtil.doPrepare(path);
        new AnnotationConfigApplicationContext(LifeCycleConfiguration.class);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
