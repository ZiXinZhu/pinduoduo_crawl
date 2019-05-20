package com.zzx.executor.core;


import com.zzx.executor.entity.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;

@Component
public class UIController {
    @Autowired
    private UI ui;

    public void show(Collection<TradeEntity> records){
        if (ui.getCore() == null){
            return;
        }
        ui.getCore().show(records);
    }


    public String reportURL(){
        if (ui.getCore() == null){
            return "";
        }
        return ui.getCore().reportURL();
    }

    public int getTimeInterval(){
        if (ui.getCore() == null){
            return 5;
        }
        String timeInterval = ui.getCore().getTimeInterval();
        int interval = 5;
        try {
            int temp = Integer.parseInt(timeInterval);
            if (temp>=3){
                interval = temp;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return interval;
    }

    public String getUserParam(){
        if (ui.getCore() == null){
            return "";
        }
        String userParam = ui.getCore().getUserParam();
        if(StringUtils.isEmpty(userParam)){
            return "Default";
        }
        return userParam;
    }

    public void clearCache(){
        if (ui.getCore() == null){
            return;
        }
        ui.getCore().clearData();//清屏
    }
}
