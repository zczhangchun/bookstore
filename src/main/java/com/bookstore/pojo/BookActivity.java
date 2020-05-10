package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "tb_activity_book_relation")
@Data
public class BookActivity {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    private Integer activityId;

    private String bookId;

    private String bookName;

    @Transient
    private Activity activity;

}
