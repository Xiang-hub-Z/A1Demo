package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wangbamanagerserver.model.User;

import java.math.BigDecimal;

public interface UserService extends IService<User> {
    /*** 用户登录验证*/
    User login(String username, String password);

    /*** 用户注册*/
    boolean register(User user);

    /*** 检查用户名是否存在*/
    boolean isUsernameExists(String username);
    /*** 用户充值*/
    boolean recharge(Long userId, Double amount, String orderId, String paymentMethod);
}
