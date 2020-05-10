package com.bookstore.service.impl;

import com.bookstore.mapper.LogMapper;
import com.bookstore.pojo.Log;
import com.bookstore.service.LogService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    @Transactional
    public void save(Log log) {

        logMapper.insert(log);

    }

    @Override
    public List<Log> findAll(Integer page, Integer pagesize) {
        Example example = new Example(Log.class);
        example.setOrderByClause("visit_time DESC");
        PageHelper.startPage(page,pagesize);
        return logMapper.selectByExample(example);

    }


}
