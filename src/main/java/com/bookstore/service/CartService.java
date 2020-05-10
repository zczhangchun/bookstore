package com.bookstore.service;

import com.bookstore.pojo.Cart;

import java.util.List;

public interface CartService {
    void addCart(Integer userId, Cart cart);

    List<Cart> queryCartList(Integer userId);

    void deleteCart(int userId, String id);

    int updateCart(int userId, String bookId, Integer number);

    List<Cart> queryCartByIds(Integer userId, List<String> ids);

    void deleteCarts(int userId, List<String> ids);

}
