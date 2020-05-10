package com.bookstore.mapper;

import com.bookstore.pojo.Stock;
import com.bookstore.utils.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock,String> {

    @Update("update tb_book_stock set stock = stock - #{num},sales = sales + #{num} where book_id = #{id} and stock >= #{num}")
    int decreaseStock(@Param("id")String Id,@Param("num")Integer num );

}
