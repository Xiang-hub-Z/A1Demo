package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.RechargeRecordMapper;
import com.example.wangbamanagerserver.model.RechargeRecord;
import com.example.wangbamanagerserver.service.RechargeRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeRecordService {

    @Override
    public RechargeRecord getByOrderId(String orderId) {
        LambdaQueryWrapper<RechargeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RechargeRecord::getOrderId, orderId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<RechargeRecord> getByUserId(Long userId) {
        LambdaQueryWrapper<RechargeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RechargeRecord::getUserId, userId)
                .orderByDesc(RechargeRecord::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public BigDecimal getTotalRechargeAmount() {
        try {
            LambdaQueryWrapper<RechargeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RechargeRecord::getStatus, 1); // 1-成功

            List<RechargeRecord> rechargeRecords = this.list(queryWrapper);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (RechargeRecord record : rechargeRecords) {
                if (record.getAmount() != null) {
                    totalAmount = totalAmount.add(record.getAmount());
                }
            }

            return totalAmount;

        } catch (Exception e) {
            log.error("获取总充值金额失败", e);
            return BigDecimal.ZERO;
        }
    }
}