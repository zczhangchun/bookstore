package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ExceptionEnums {

    USER_EXIT("用户名已存在"),
    PHONE_EXIT("手机号已存在");
    ;

    private String msg;
}
