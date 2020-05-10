package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum StatusEnums {
    UN_PAY(1,"未付款"),
    PAYED(2,"已付款,未确认"),
    UN_CONFIRM(3,"已发货,未确认"),
    SUCCESS(4,"已确认,交易成功"),
    REFUND(5,"退款")
    ;

    private Integer status;
    private String msg;
}
