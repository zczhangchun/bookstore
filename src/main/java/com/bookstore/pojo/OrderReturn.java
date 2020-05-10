package com.bookstore.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_order_return")
@Data
public class OrderReturn {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long orderId;

    private Integer userId;

    private Date applyTime;

    private String returnName;

    private String returnPhone;

    private Integer returnAmount;

    private Integer status;

    private Integer type;

    private String reason;

    private String shippingName;

    private String shippingCode;


}
