package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.ComputerSession;
import com.example.wangbamanagerserver.service.ComputerSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/computer-session")
public class ComputerSessionController {

    @Autowired
    private ComputerSessionService computerSessionService;

    // 开始上机
    @PostMapping("/start")
    public Map<String, Object> startSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = Long.valueOf(sessionData.get("userId").toString());
            Long computerId = Long.valueOf(sessionData.get("computerId").toString());

            boolean startResult = computerSessionService.startSession(userId, computerId);
            if (startResult) {
                result.put("code", 200);
                result.put("message", "上机成功");
            } else {
                result.put("code", 500);
                result.put("message", "上机失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "上机失败: " + e.getMessage());
        }
        return result;
    }

    // 结束上机
    @PostMapping("/end")
    public Map<String, Object> endSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long sessionId = Long.valueOf(sessionData.get("sessionId").toString());

            boolean endResult = computerSessionService.endSession(sessionId);
            if (endResult) {
                result.put("code", 200);
                result.put("message", "下机成功");
            } else {
                result.put("code", 500);
                result.put("message", "下机失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "下机失败: " + e.getMessage());
        }
        return result;
    }

    // 获取用户当前上机状态
    @GetMapping("/user/{userId}/current")
    public Map<String, Object> getCurrentSession(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            ComputerSession session = computerSessionService.getCurrentSessionByUserId(userId);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", session);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取上机状态失败");
        }
        return result;
    }

    // 获取会话列表 - 新增接口
    @GetMapping("/list")
    public Map<String, Object> getSessionList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ComputerSession> sessions = computerSessionService.getSessionList(userId, status, page, pageSize);
            long total = computerSessionService.getSessionCount(userId, status);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", sessions);

            // 添加分页信息
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", page);
            pagination.put("pageSize", pageSize);
            pagination.put("total", total);
            pagination.put("totalPages", (int) Math.ceil((double) total / pageSize));
            result.put("pagination", pagination);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取会话列表失败: " + e.getMessage());
        }
        return result;
    }

    // 根据ID获取会话详情
    @GetMapping("/{id}")
    public Map<String, Object> getSessionById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            ComputerSession session = computerSessionService.getById(id);
            if (session != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", session);
            } else {
                result.put("code", 404);
                result.put("message", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取会话详情失败");
        }
        return result;
    }
}