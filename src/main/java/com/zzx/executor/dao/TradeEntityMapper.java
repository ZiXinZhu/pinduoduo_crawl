package com.zzx.executor.dao;


import com.zzx.executor.entity.TradeEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeEntityMapper {

    @Insert("insert into t_trade (id, trade_date, trade_time,money, trade_type, remark, \n" +
            "      identity, bank, report, bank_account)\n" +
            "    values (#{id}, #{tradeDate}, #{tradeTime},#{money}, #{tradeType}, #{remark}, \n" +
            "      #{identity}, #{bank}, #{report},#{bankAccount})")
    int insert(TradeEntity record);

    @Select("SELECT t.id,t.trade_date,t.trade_time,t.money,t.trade_type,t.remark,t.identity,t.bank,t.report," +
            "t.bank_account FROM t_trade t WHERE t.trade_date = #{tradeDate} AND t.trade_time = #{tradeTime} " +
            "AND t.money = #{money} AND t.bank_account = #{bankAccount};")
    List<TradeEntity> checkExist(@Param("tradeDate") String tradeDate,
                                 @Param("tradeTime") String tradeTime,
                                 @Param("money") String money,
                                 @Param("bankAccount") String bankAccount);
}