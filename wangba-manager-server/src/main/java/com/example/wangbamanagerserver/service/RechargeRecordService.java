package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wangbamanagerserver.model.RechargeRecord;

import java.math.BigDecimal;
import java.util.List;

public interface RechargeRecordService extends IService<RechargeRecord> {

    /**
     * 根据订单号查询充值记录
     */
    RechargeRecord getByOrderId(String orderId);

    /**
     * 根据用户ID查询充值记录列表
     */
    List<RechargeRecord> getByUserId(Long userId);
    /**
     * 获取总充值金额（只统计状态为成功的记录）
     */
    BigDecimal getTotalRechargeAmount();
}