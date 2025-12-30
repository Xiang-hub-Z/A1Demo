package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.UserMapper;
import com.example.wangbamanagerserver.model.RechargeRecord;
import com.example.wangbamanagerserver.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{

    @Autowired
    private RechargeRecordService rechargeRecordService;

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                .eq("password", password);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (isUsernameExists(user.getUsername())) {
            return false;
        }
        return this.save(user);
    }

    @Override
    public boolean isUsernameExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.count(queryWrapper) > 0;
    }

    /** 用户充值 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, Double amount, String orderId, String paymentMethod) {
        try {
            // 1. 查询用户当前余额
            User user = this.getById(userId);
            if (user == null) {
                log.error("用户不存在: userId={}", userId);
                return false;
            }

            // 2. 检查订单号是否已存在（防止重复充值）
            RechargeRecord existingRecord = rechargeRecordService.getByOrderId(orderId);
            if (existingRecord != null) {
                log.error("订单号已存在: orderId={}", orderId);
                return false;
            }

            // 3. 创建充值记录（状态为处理中）
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setUserId(userId);
            rechargeRecord.setOrderId(orderId);
            rechargeRecord.setAmount(BigDecimal.valueOf(amount));
            rechargeRecord.setPaymentMethod(paymentMethod);
            rechargeRecord.setStatus(3); // 处理中

            boolean recordSaved = rechargeRecordService.save(rechargeRecord);
            if (!recordSaved) {
                throw new RuntimeException("保存充值记录失败");
            }

            // 4. 计算新的余额
            Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
            Double newBalance = currentBalance + amount;

            // 5. 更新用户余额
            user.setBalance(newBalance);

            // 6. 更新数据库
            boolean updateResult = this.updateById(user);
            if (!updateResult) {
                throw new RuntimeException("更新用户余额失败");
            }

            // 7. 更新充值记录状态为成功
            rechargeRecord.setStatus(1); // 成功
            rechargeRecordService.updateById(rechargeRecord);

            log.info("用户充值成功: userId={}, amount={}, orderId={}", userId, amount, orderId);
            return true;

        } catch (Exception e) {
            log.error("用户充值失败: userId={}, amount={}, error={}", userId, amount, e.getMessage());
            // 如果充值失败，可以更新充值记录状态为失败
            try {
                RechargeRecord failedRecord = rechargeRecordService.getByOrderId(orderId);
                if (failedRecord != null) {
                    failedRecord.setStatus(2); // 失败
                    rechargeRecordService.updateById(failedRecord);
                }
            } catch (Exception ex) {
                log.error("更新充值记录状态失败: orderId={}, error={}", orderId, ex.getMessage());
            }
            return false;
        }
    }
}