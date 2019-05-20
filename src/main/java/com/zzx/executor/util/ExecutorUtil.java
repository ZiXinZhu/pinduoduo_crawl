package com.zzx.executor.util;

import com.zzx.executor.entity.TradeEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExecutorUtil {

    public static boolean doPrepare(String path) {
        boolean flag = false;
        ClassPathResource classPathResource = new ClassPathResource("tool/QuarkGG.exe");
        String filename = classPathResource.getFilename();

        try {
            InputStream inputStream = classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            String fileName = new String(filename.getBytes("UTF-8"), "iso-8859-1");
            String filePath = path + File.separator + fileName;
            File file = new File(filePath);
            if (!file.exists()){
                FileOutputStream output = new FileOutputStream(file);
                output.write(bytes);
            }
            flag = true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return flag;
    }

    public static String getRecordHash(TradeEntity record){
        return record.getTradeDate()+record.getTradeTime()+record.getMoney()+record.getBankAccount();
    }
}
