package com.bookstore.service;

import com.bookstore.pojo.Order;
import com.bookstore.pojo.OrderReturn;
import com.bookstore.pojo.OrderStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Order addOrder(Order order, List<String> ids, int userId);

    String createUrl(String orderId, Integer actualPay);

    Map pay(Long orderId);

    List<Order> queryOrderByStatus(Integer status,Integer userId);

    Order queryOrderById(Long orderId);

    void updateStatus(Long orderId, int i);

    List<Order> queryOrderByStatus(Integer page, Integer pagesize, Integer status);

    void updateStatus(OrderStatus status,  Integer num);

    OrderReturn refund(OrderReturn orderReturn);

    OrderReturn findRefundDetailByOrderId(Long orderId);

    List<OrderReturn> findRefundByPage(Integer page, Integer pagesize, Integer status);

    OrderReturn queryRefundById(Integer id);

    OrderReturn updateRefundStatus(Integer status, Integer id);

    int findTurnoverTotal();

    int findTurnoverToday();

    int findOrderQuantityTotal();

    int findOrderQuantityToday();

    List<Integer> queryTodayOrderTypeAll();

    List<Integer> findTurnoverDailyList();

    int getFavorable(List<String> bookIds, Integer userId);

}
