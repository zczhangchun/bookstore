package com.bookstore.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

@Data
@Table(name = "tb_user")
public class User {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    @Length(min = 5, max = 16, message = "用户名只能在5～16位之间")
    private String username;

    @Length(min = 5, max = 16, message = "密码只能在5～16位之间")
    @JsonIgnore
    private String password;

    @Pattern(regexp = "^1[35678]\\d{9}$", message = "手机号格式不正确")
    private String phone;


    @JsonIgnore
    private String salt;

    @Transient
    private String code;

    @Transient
    private String captcha;


}
