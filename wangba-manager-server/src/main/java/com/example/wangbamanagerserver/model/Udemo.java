package com.example.wangbamanagerserver.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("udemo")
public class Udemo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String  name;
    private String  zh;
    private String  pwd;
}
