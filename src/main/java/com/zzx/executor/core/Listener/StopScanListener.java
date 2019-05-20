package com.zzx.executor.core.Listener;


import com.zzx.executor.core.IEOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
@Qualifier(value = "stopScanListener")
public class StopScanListener implements ActionListener {

    @Autowired
    private IEOperator ieOperator;

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "扫描已停止");
        ieOperator.stop();
    }
}
