package com.bookstore.pojo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_category")
@Data
public class Category {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    @Length(min = 1, max = 15, message = "名称在1～15位之间")
    private String name;

    private Boolean status;
}
