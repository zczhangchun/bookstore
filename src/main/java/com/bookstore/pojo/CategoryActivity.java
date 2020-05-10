package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "tb_activity_category_relation")
@Data
public class CategoryActivity {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    private Integer activityId;

    private Integer categoryId;

    private String categoryName;

    @Transient
    private Activity activity;

}
