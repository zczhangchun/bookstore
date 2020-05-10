package com.bookstore.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdatePassword {

    private Integer userId;

    @Length(min = 5,max = 16)
    private String oldPassword;

    @Length(min = 5,max = 16)
    private String newPassword;

    @Length(min = 5,max = 16)
    private String secondNewPassword;

    @Length(min = 6, max = 6)
    private String code;

}
