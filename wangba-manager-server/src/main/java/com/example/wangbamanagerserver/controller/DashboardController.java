package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.Computer;
import com.example.wangbamanagerserver.model.RechargeRecord;
import com.example.wangbamanagerserver.service.ComputerService;
import com.example.wangbamanagerserver.service.ComputerSessionService;
import com.example.wangbamanagerserver.service.RechargeRecordService;
import com.example.wangbamanagerserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ComputerService computerService;

    @Autowired
    private ComputerSessionService computerSessionService;

    @Autowired
    private RechargeRecordService rechargeRecordService;

    // 获取仪表盘统计数据
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 总用户数
            long totalUsers = userService.count();

            // 2. 电脑总数
            long totalComputers = computerService.count();

            // 3. 总充值收入（从充值记录表获取，只统计状态为成功的记录）
            BigDecimal totalRecharge = rechargeRecordService.getTotalRechargeAmount();

            // 4. 总上机收入（从会话表的总金额计算）
            BigDecimal totalSessionRevenue = computerSessionService.getTotalSessionRevenue();

            // 5. 进行中订单数（状态为1的会话）
            long activeSessions = computerSessionService.getActiveSessionCount();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalComputers", totalComputers);
            stats.put("totalRecharge", totalRecharge);
            stats.put("totalSessionRevenue", totalSessionRevenue);
            stats.put("activeSessions", activeSessions);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", stats);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取统计数据失败");
        }
        return result;
    }

    // 获取电脑状态统计
    @GetMapping("/computer-status")
    public Map<String, Object> getComputerStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有电脑
            List<Computer> computers = computerService.list();

            long freeCount = computers.stream()
                    .filter(computer -> computer.getStatus() == 1) // 1-空闲
                    .count();

            long usingCount = computers.stream()
                    .filter(computer -> computer.getStatus() == 2) // 2-使用中
                    .count();

            long maintenanceCount = computers.stream()
                    .filter(computer -> computer.getStatus() == 3) // 3-维护中
                    .count();

            Map<String, Object> status = new HashMap<>();
            status.put("free", freeCount);
            status.put("using", usingCount);
            status.put("maintenance", maintenanceCount);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", status);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取电脑状态失败");
        }
        return result;
    }
}