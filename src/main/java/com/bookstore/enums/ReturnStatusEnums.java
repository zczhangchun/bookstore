package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ReturnStatusEnums {

    PENDING(0,"待处理"),
    ACCEPTED(1,"已受理"),
    OFF_THE_STOCKS(2,"已完成"),
    TURN_DOWN(3,"已拒绝")
    ;

    private Integer status;
    private String msg;
}
