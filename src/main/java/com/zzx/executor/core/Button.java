package com.zzx.executor.core;


import com.zzx.executor.core.interfaces.IButton;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Alon
 * @Date 2019/3/24 19:50
 */
@Component
public class Button implements IButton {

    private String loginPage = "https://mms.pinduoduo.com/login";

    //点击查询
    private String click = "document.getElementById('btn_query').click()";
    //获取Iframe内容
    private String getIFrameData = "return document.getElementById('contentFrame')" +
            ".contentWindow.document" +
            ".getElementById('detailPageContent').innerHTML;";

    @Override
    public String getLoginPageURL() {
        return loginPage;
    }

    @Override
    public String refreshButton() {
        return click;
    }

    @Override
    public String getDataButton() {
        return getIFrameData;
    }
}
