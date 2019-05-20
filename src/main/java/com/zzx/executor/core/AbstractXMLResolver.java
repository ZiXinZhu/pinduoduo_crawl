package com.zzx.executor.core;



import com.zzx.executor.core.interfaces.IxmlResolver;
import com.zzx.executor.entity.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public abstract class AbstractXMLResolver implements IxmlResolver {

    protected String xmlData = "";
    protected boolean isname =true;

    @Autowired
    protected DataProcessor dataProcessor;


    public void initial(String xmlData) {
        this.xmlData = xmlData;
    }

    @Override
    public void run() {
        xmlData = preProcess(xmlData);
        execute(xmlData);
    }

    private String preProcess(String xmlData){
        xmlData = xmlData.replaceAll("\n", "");
        xmlData = xmlData.replaceAll("\r", "");
        xmlData = xmlData.replaceAll("\t", "");
        xmlData = xmlData.replaceAll("\"", "");
        xmlData = xmlData.replaceAll(" ", "");
        return xmlData;
    }

    @Override
    public void execute(String xmlData) {

        List<TradeEntity> records = messageResolve(xmlData);
        if (records.size() == 0) {
            return;
        }
        dataProcessor.addData(records);
    }
}
