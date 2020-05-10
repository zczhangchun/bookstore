package com.bookstore.pojo;

import lombok.Data;

@Data
public class Cart {

    private String bookId;

    private String bookName;

    private String img;

    private Integer price;

    private Integer number;


}
