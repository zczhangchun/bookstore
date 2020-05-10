package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Data
@Table(name = "tb_receiver")
public class Address {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Integer userId;

    @NotBlank
    private String detail;

    @NotBlank
    private String phone;

    private String zip;

    @NotBlank
    private String name;


}
