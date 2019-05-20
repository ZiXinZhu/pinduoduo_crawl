package com.zzx.executor.core.Listener;


import com.zzx.executor.core.IEOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
@Qualifier(value = "loginListener")
public class LoginListener implements ActionListener {

    @Autowired
    private IEOperator ieOperator;

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null,"浏览器将在10s内启动，请勿重复点击！");
        ieOperator.openAndLogin();
    }
}
