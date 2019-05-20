package com.zzx.executor.core.Listener;


import com.zzx.executor.core.IEOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;

@Component
@Qualifier(value = "startScanListener")
public class StartScanListener implements ActionListener {

    @Autowired
    private IEOperator ieOperator;
    @Autowired
    private ExecutorService threadPool;

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "开始扫描，请勿关闭浏览器");
        threadPool.submit(ieOperator);
    }
}
