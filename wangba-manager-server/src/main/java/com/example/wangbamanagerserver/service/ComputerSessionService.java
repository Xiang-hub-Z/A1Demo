package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wangbamanagerserver.model.ComputerSession;

import java.math.BigDecimal;
import java.util.List;

public interface ComputerSessionService extends IService<ComputerSession> {

    /**
     * 开始上机
     */
    boolean startSession(Long userId, Long computerId);

    /**
     * 结束上机
     */
    boolean endSession(Long sessionId);

    /**
     * 根据用户ID获取当前上机会话
     */
    ComputerSession getCurrentSessionByUserId(Long userId);

    /**
     * 根据计算机ID获取当前上机会话
     */
    ComputerSession getCurrentSessionByComputerId(Long computerId);

    /**
     * 计算上机费用
     */
    Double calculateSessionAmount(Long sessionId);
    /**
     * 获取会话列表 - 新增方法
     */
    List<ComputerSession> getSessionList(Long userId, Integer status, Integer page, Integer pageSize);

    /**
     * 获取会话数量 - 新增方法
     */
    long getSessionCount(Long userId, Integer status);
    /**
     * 获取总上机收入
     */
    BigDecimal getTotalSessionRevenue();

    /**
     * 获取进行中会话数量
     */
    long getActiveSessionCount();
}