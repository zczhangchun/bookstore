package com.bookstore.service;

import com.bookstore.dto.UpdatePassword;
import com.bookstore.pojo.Address;
import com.bookstore.pojo.User;

import java.util.List;

public interface UserService {

    public User login(User user);

    User register(User user);

    void sms(String phone);

    void updatePassword(UpdatePassword form);

    Boolean verify(String data,String data2, Integer type);

    void addReceiver(Address address);

    List<Address> queryAddressByUserId(Integer userId);

    void updateAddress(Address address);
}
