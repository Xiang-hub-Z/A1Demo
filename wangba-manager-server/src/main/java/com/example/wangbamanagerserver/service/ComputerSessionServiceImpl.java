package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.ComputerMapper;
import com.example.wangbamanagerserver.mapper.ComputerSessionMapper;
import com.example.wangbamanagerserver.model.Computer;
import com.example.wangbamanagerserver.model.ComputerSession;
import com.example.wangbamanagerserver.model.User;
import com.example.wangbamanagerserver.service.ComputerSessionService;
import com.example.wangbamanagerserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ComputerSessionServiceImpl extends ServiceImpl<ComputerSessionMapper, ComputerSession> implements ComputerSessionService {

    @Autowired
    private UserService userService;

    @Autowired
    private ComputerMapper computerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startSession(Long userId, Long computerId) {
        try {
            // 1. 检查用户是否存在且余额充足
            User user = userService.getById(userId);
            if (user == null) {
                log.error("用户不存在: userId={}", userId);
                return false;
            }

            // 2. 检查计算机是否存在且空闲
            Computer computer = computerMapper.selectById(computerId);
            if (computer == null) {
                log.error("计算机不存在: computerId={}", computerId);
                return false;
            }
            if (computer.getStatus() != 1) { // 1-空闲
                log.error("计算机不可用: computerId={}, status={}", computerId, computer.getStatus());
                return false;
            }

            // 3. 检查用户是否已经有上机会话
            ComputerSession existingSession = getCurrentSessionByUserId(userId);
            if (existingSession != null) {
                log.error("用户已有上机会话: userId={}, sessionId={}", userId, existingSession.getId());
                return false;
            }

            // 4. 创建上机会话
            ComputerSession session = new ComputerSession();
            session.setUserId(userId);
            session.setComputerId(computerId);
            session.setStartTime(LocalDateTime.now());
            session.setStatus(1); // 1-进行中
            session.setTotalAmount(BigDecimal.ZERO);

            boolean saveResult = this.save(session);
            if (!saveResult) {
                throw new RuntimeException("创建上机会话失败");
            }

            // 5. 更新计算机状态为使用中
            computer.setStatus(2); // 2-使用中
            boolean updateComputerResult = computerMapper.updateById(computer) > 0;
            if (!updateComputerResult) {
                throw new RuntimeException("更新计算机状态失败");
            }

            log.info("上机成功: userId={}, computerId={}, sessionId={}", userId, computerId, session.getId());
            return true;

        } catch (Exception e) {
            log.error("开始上机失败: userId={}, computerId={}, error={}", userId, computerId, e.getMessage());
            throw new RuntimeException("开始上机失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endSession(Long sessionId) {
        try {
            // 1. 获取上机会话
            ComputerSession session = this.getById(sessionId);
            if (session == null) {
                log.error("上机会话不存在: sessionId={}", sessionId);
                return false;
            }
            if (session.getStatus() != 1) { // 1-进行中
                log.error("上机会话状态异常: sessionId={}, status={}", sessionId, session.getStatus());
                return false;
            }

            // 2. 先设置结束时间
            LocalDateTime endTime = LocalDateTime.now();
            session.setEndTime(endTime);

            // 3. 计算费用
            Double amount = calculateSessionAmount(sessionId);
            if (amount == null) {
                throw new RuntimeException("计算费用失败");
            }

            // 4. 检查用户余额
            User user = userService.getById(session.getUserId());
            if (user.getBalance() < amount) {
                log.error("用户余额不足: userId={}, balance={}, amount={}",
                        session.getUserId(), user.getBalance(), amount);
                return false;
            }

            // 5. 扣费
            Double newBalance = user.getBalance() - amount;
            user.setBalance(newBalance);
            boolean updateUserResult = userService.updateById(user);
            if (!updateUserResult) {
                throw new RuntimeException("更新用户余额失败");
            }

            // 6. 更新上机会话
            session.setTotalAmount(BigDecimal.valueOf(amount));
            session.setStatus(2); // 2-已结束
            boolean updateSessionResult = this.updateById(session);
            if (!updateSessionResult) {
                throw new RuntimeException("更新上机会话失败");
            }

            // 7. 更新计算机状态为空闲
            Computer computer = computerMapper.selectById(session.getComputerId());
            computer.setStatus(1); // 1-空闲
            boolean updateComputerResult = computerMapper.updateById(computer) > 0;
            if (!updateComputerResult) {
                throw new RuntimeException("更新计算机状态失败");
            }

            log.info("下机成功: sessionId={}, userId={}, amount={}", sessionId, session.getUserId(), amount);
            return true;

        } catch (Exception e) {
            log.error("结束上机失败: sessionId={}, error={}", sessionId, e.getMessage());
            throw new RuntimeException("结束上机失败", e);
        }
    }

    @Override
    public ComputerSession getCurrentSessionByUserId(Long userId) {
        LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComputerSession::getUserId, userId)
                .eq(ComputerSession::getStatus, 1) // 1-进行中
                .orderByDesc(ComputerSession::getStartTime)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public ComputerSession getCurrentSessionByComputerId(Long computerId) {
        LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComputerSession::getComputerId, computerId)
                .eq(ComputerSession::getStatus, 1) // 1-进行中
                .orderByDesc(ComputerSession::getStartTime)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public Double calculateSessionAmount(Long sessionId) {
        try {
            ComputerSession session = this.getById(sessionId);
            if (session == null) {
                log.error("上机会话不存在: sessionId={}", sessionId);
                return null;
            }

            Computer computer = computerMapper.selectById(session.getComputerId());
            if (computer == null) {
                log.error("计算机不存在: computerId={}", session.getComputerId());
                return null;
            }

            LocalDateTime endTime = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
            Duration duration = Duration.between(session.getStartTime(), endTime);
            long minutes = duration.toMinutes();

            // 按小时计费，不足1小时按1小时计算
            long hours = (minutes + 59) / 60; // 向上取整
            hours = Math.max(hours, 1); // 至少1小时，确保0分钟也算1小时

            Double amount = hours * computer.getPricePerHour().doubleValue();

            log.info("计算费用: sessionId={}, minutes={}, hours={}, price={}, amount={}",
                    sessionId, minutes, hours, computer.getPricePerHour(), amount);

            return amount;

        } catch (Exception e) {
            log.error("计算上机费用失败: sessionId={}, error={}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<ComputerSession> getSessionList(Long userId, Integer status, Integer page, Integer pageSize) {
        try {
            LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();

            // 按用户ID筛选
            if (userId != null) {
                queryWrapper.eq(ComputerSession::getUserId, userId);
            }

            // 按状态筛选
            if (status != null) {
                queryWrapper.eq(ComputerSession::getStatus, status);
            }

            // 按开始时间倒序排列
            queryWrapper.orderByDesc(ComputerSession::getStartTime);

            // 分页查询
            Page<ComputerSession> pageParam = new Page<>(page, pageSize);
            Page<ComputerSession> sessionPage = this.page(pageParam, queryWrapper);

            return sessionPage.getRecords();

        } catch (Exception e) {
            log.error("获取会话列表失败: userId={}, status={}", userId, status, e);
            throw new RuntimeException("获取会话列表失败", e);
        }
    }

    @Override
    public long getSessionCount(Long userId, Integer status) {
        try {
            LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();

            // 按用户ID筛选
            if (userId != null) {
                queryWrapper.eq(ComputerSession::getUserId, userId);
            }

            // 按状态筛选
            if (status != null) {
                queryWrapper.eq(ComputerSession::getStatus, status);
            }

            return this.count(queryWrapper);

        } catch (Exception e) {
            log.error("获取会话数量失败: userId={}, status={}", userId, status, e);
            throw new RuntimeException("获取会话数量失败", e);
        }
    }

    // 在 ComputerSessionServiceImpl 中优化实现
    @Override
    public BigDecimal getTotalSessionRevenue() {
        try {
            LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(ComputerSession::getTotalAmount)
                    .gt(ComputerSession::getTotalAmount, BigDecimal.ZERO);

            List<ComputerSession> sessions = this.list(queryWrapper);
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ComputerSession session : sessions) {
                totalRevenue = totalRevenue.add(session.getTotalAmount());
            }
            return totalRevenue;

        } catch (Exception e) {
            log.error("获取总上机收入失败", e);
            return BigDecimal.ZERO;
        }
    }
    @Override
    public long getActiveSessionCount() {
        LambdaQueryWrapper<ComputerSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComputerSession::getStatus, 1); // 1-进行中
        return this.count(queryWrapper);
    }
}