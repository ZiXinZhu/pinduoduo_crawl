package com.zzx.executor.core.interfaces;



import com.zzx.executor.entity.TradeEntity;

import java.util.List;

/**
 * @Description
 * @Author Alon
 * @Date 2019/3/24 18:33
 */
public interface IxmlResolver extends Runnable {

    void execute(String xmlData);

    List<TradeEntity> messageResolve(String xmlData);
}
