package com.bookstore.mapper;

import com.bookstore.pojo.StockStatement;
import com.bookstore.utils.BaseMapper;

public interface StockStatementMapper extends BaseMapper<StockStatement, Integer> {


//    @Select("select * from tb_stock_statement where operation_time >= ${} AND operation_time <= #{}")
//    List<StockStatement> findByTimeRange(@Param(""));
}
