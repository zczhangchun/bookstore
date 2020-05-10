package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_order_detail")
public class OrderDetail {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    private Long orderId;

    private String bookId;

    private Integer number;

    private String bookName;

    private String img;

    private Integer price;

}
