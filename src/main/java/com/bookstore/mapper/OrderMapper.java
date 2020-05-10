package com.bookstore.mapper;

import com.bookstore.pojo.Order;
import com.bookstore.utils.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order,String> {

    @Select("select * from tb_order where order_id in (select order_id from tb_order_status where status=#{status}) and user_id = #{userId}")
    List<Order> queryOrderByStatus(@Param("status") Integer status, @Param("userId") Integer userId);

    @Select("select * from tb_order where order_id in (select order_id from tb_order_status where status=#{status})")
    List<Order> findOrderByStatus(@Param("status") Integer status);
}
