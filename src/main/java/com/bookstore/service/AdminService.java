package com.bookstore.service;

import com.bookstore.pojo.Admin;

import java.util.List;

public interface AdminService {


    Admin login(Admin admin);

    List<Admin> findAll();

    void updateLevel(Integer id);

    int findPVTotal();

    int findPVToday();

    List<Integer> queryPVDailyList();

    void addAdmin(Admin admin);
}
