package com.bookstore;

import com.bookstore.mapper.StockStatementMapper;
import com.bookstore.pojo.StockStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApplicationTest {

    @Autowired
    private StockStatementMapper statementMapper;

    @Test
    public void m1(){

        Example example = new Example(StockStatement.class);

        example.createCriteria().andBetween("operationTime","2019-10-19 00:00:00","2019-10-19 23:59:59");




    }
}
