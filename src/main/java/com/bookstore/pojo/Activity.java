package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_activity")
@Data
public class Activity {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    private String activityName;

    private Integer full;

    private Integer subtract;

    private String remark;

    private Boolean status;

    private Integer useType;

    private Integer discount;

}
