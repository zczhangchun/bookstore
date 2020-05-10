package com.bookstore.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_book_stock")
@Data
public class Stock {

    @Id
    private String bookId;

    private Integer stock;

    private Integer sales;

    private String bookName;


}
