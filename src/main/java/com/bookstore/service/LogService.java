package com.bookstore.service;

import com.bookstore.pojo.Log;

import java.util.List;

public interface LogService {

    void save(Log log);


    List<Log> findAll(Integer page, Integer pagesize);
}
