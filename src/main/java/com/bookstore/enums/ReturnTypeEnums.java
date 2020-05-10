package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public enum  ReturnTypeEnums {

    UNFILLED_RETURN(0,"未发货退款"),
    SEND_OUT_RETURN(1,"已发货退款"),
    ;

    private Integer type;

    private String msg;
}
