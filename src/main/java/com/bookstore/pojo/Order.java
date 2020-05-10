package com.bookstore.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Table(name = "tb_order")
public class Order implements Comparable<Order>{

    @Id
    private Long orderId;

    private Integer totalPay;

    private Integer postage;

    private Integer favorable;

    private Integer actualPay;

    private Integer paymentType;

    private Integer userId;

    private String buyerMessage;

    @NotBlank
    private String receiverAddress;

    @NotBlank
    private String receiverPhone;

    private String receiverZip;

    @NotBlank
    private String receiver;

    @Transient
    private OrderStatus Status;

    @Transient
    private List<OrderDetail> detailList;

    @Override
    public int compareTo(Order o) {
        return (int) (o.getStatus().getCreateTime().getTime() - this.getStatus().getCreateTime().getTime());
    }
}
