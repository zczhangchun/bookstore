package com.bookstore.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "tb_log")
public class Log {

    @Id
    private String id;

    private Date visitTime;

    private Integer aid;

    private String url;

    private Long executionTime;

    private String method;

    private String ip;



}
