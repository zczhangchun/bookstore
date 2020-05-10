package com.bookstore.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_admin")
@Data
public class Admin {

    @Id
    private Integer id;

    private String username;

    private String password;

    private Integer level;

}
