package com.zzx.executor.core;


import com.zzx.executor.core.interfaces.IButton;
import org.eclipse.swt.browser.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IEOperator implements Runnable {
    private boolean notInterrupted = true;
    private long count=0;
    private WebDriver driver;
    private static String path = "C:\\Program Files";
    private static boolean firstTime = true;
    private long budan=1;


    @Autowired
    private XMLResolver task;

    @Autowired
    private ExecutorService threadPool;

    @Autowired
    private DataProcessor processor;

    @Autowired
    private IButton button;

    @Autowired
    private UI ui;

    public void openAndLogin() {
        if (StringUtils.isEmpty(path)) {
            System.out.println("没有找到Quark");
            return;
        }
//        System.setProperty("webdriver.ie.driver", path + File.separator + "Quark.exe");
        System.setProperty("webdriver.chrome.driver", path + File.separator + "QuarkGG.exe");
//        driver = new InternetExplorerDriver();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.get(button.getLoginPageURL());
    }

    @Override
    public void run() {
        try {
            this.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.notInterrupted = false;
    }

    public void start() throws InterruptedException {
        notInterrupted = true;
        processor.initParams(); //初始化上报地址、扫描周期

        if (driver == null) {
            return;
        }

        //TODO 附加参数
        if (firstTime) {
            driver.get("https://mms.pinduoduo.com/mallcenter/info/basic");
            Thread.sleep(2000);
            String data = driver.getPageSource();
            data = data.replaceAll(" ", "");
            data = data.replaceAll("\n", "");
            Pattern pattern = Pattern.compile("(.*?)店铺名称:</label><div>(?<names>.*?)<(.*?)");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                String name = matcher.group("names");
                if (!StringUtils.isEmpty(name)) {
                    ui.showName(name);
                    firstTime = false;
                    break;
                }
            }
        }
//        WebElement e =driver.findElement(By.className("pdd-dui-options-wrap"));
//        e.findElement(By.partialLinkText("待发货")).click();

        Thread.sleep(1000);
        driver.navigate().to("https://mms.pinduoduo.com/order.html#/orders/search/index?type=0");
        while (notInterrupted) {
            System.out.println("***************************正在抓取数据:第"+(count++)+"次*******************************");

//待发货订单
            driver.navigate().refresh();

//全部订单
            WebElement e = driver.findElement(By.className("pdd-dui-select-text"));
            e.click();

            Thread.sleep(2800);
            WebElement element = driver.findElement(By.className("pdd-dui-options-wrap"));
            element.findElement(By.partialLinkText("全部")).click();
            Thread.sleep(1000);
//帅选售后
//            WebElement we=driver.findElements(By.className("pdd-dui-options-wrap")).get(1);
//            we.findElement(By.partialLinkText("全部")).click();
//            Thread.sleep(1000);

//查找时间
            driver.findElements(By.className("pdd-dui-select-text")).get(4).click();
            Thread.sleep(2800);
            WebElement webE=driver.findElements(By.className("pdd-dui-options-wrap")).get(4);
            //webE.findElement(By.partialLinkText("最近24小时内")).click();
            webE.findElement(By.partialLinkText("最近7天内")).click();

//点击查询
            Thread.sleep(2000);
            WebElement ele = driver.findElement(By.className("ez-75"));
            ele.findElement(By.partialLinkText("查询")).click();


//   补单
//            Thread.sleep(1000);
//            WebElement webElement=driver.findElement(By.className("pdd-pagination"));
//            webElement.findElement(By.partialLinkText(String.valueOf(budan))).click();
//            if(budan<=5){
//                budan++;
//            }else {
//                budan=1;
//            }

            Thread.sleep(500);
            String data = driver.getPageSource();
            //System.out.println(data);
            task.initial(data);
            threadPool.submit(task);
            //除去上面已睡眠的3s
            Thread.sleep(DataProcessor.getTimeInterval() * 1000 - 3000);
        }
    }
}
