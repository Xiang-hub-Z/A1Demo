package com.example.wangbamanagerserver.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("computer")
public class Computer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;
    private String computerNo;
    private String name;
    private String ipAddress;
    private String configuration;
    private BigDecimal pricePerHour;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}