package com.example.wangbamanagerserver.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String admin;        // 账号
    private String password;     // 密码
    private String name;         // 管理员名称
    private String phone;        // 手机号

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createtime; // 创建时间
}