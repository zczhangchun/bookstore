package com.bookstore.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_stock_statement")
@Data
public class StockStatement {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    private String bookId;

    private Integer count;

    private Date operationTime;

    private Integer type;
}
