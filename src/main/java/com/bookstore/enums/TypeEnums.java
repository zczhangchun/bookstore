package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum  TypeEnums {

    USER_ERROR("user"),
    USERNAME_ERROR("username"),
    PASSWORD_ERROR("password"),
    PHONE_ERROR("phone"),
    CODE_ERROR("code"),

    ;
    private String type;

}
