package com.example.wangbamanagerserver.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String realname;
    private String phone;
    private Double balance;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createtime;

}
