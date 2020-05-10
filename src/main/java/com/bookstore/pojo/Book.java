package com.bookstore.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Table(name = "tb_book")
@Data
public class Book {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")//此处加上注解
    private String id;

    @NotBlank
    private String name;
    @Min(0)
    private Integer price;
    @NotBlank
    private String author;

    private String detail;
    @NotBlank
    private String publisher;

    @DateTimeFormat(pattern = "yyyy年MM月dd日")
    private Date publishTime;

    private String img;

    private Boolean status;
    @NotNull
    private Integer categoryId;
    @NotBlank
    private String isbn;

    private Integer pagesize;

    private String packing;

    private Integer format;

    @Transient
    private Integer stock;

    @Transient
    private String categoryName;



}
