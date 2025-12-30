package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.RechargeRecord;
import com.example.wangbamanagerserver.service.RechargeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recharge-record")
public class RechargeRecordController {

    @Autowired
    private RechargeRecordService rechargeRecordService;

    // 获取所有充值记录 - 返回统一格式
    @GetMapping("/list")
    public Map<String, Object> getAllRechargeRecords(){
        Map<String, Object> result = new HashMap<>();
        try {
            List<RechargeRecord> records = rechargeRecordService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", records);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取充值记录失败");
        }
        return result;
    }

    // 根据ID查询充值记录 - 返回统一格式
    @GetMapping("/{id}")
    public Map<String, Object> getRechargeRecordById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            RechargeRecord record = rechargeRecordService.getById(id);
            if (record != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", record);
            } else {
                result.put("code", 404);
                result.put("message", "充值记录不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取充值记录失败");
        }
        return result;
    }

    // 添加充值记录 - 返回统一格式
    @PostMapping("/add")
    public Map<String, Object> addRechargeRecord(@RequestBody RechargeRecord rechargeRecord) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean saveResult = rechargeRecordService.save(rechargeRecord);
            if (saveResult) {
                result.put("code", 200);
                result.put("message", "添加成功");
            } else {
                result.put("code", 500);
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加失败");
        }
        return result;
    }

    // 修改充值记录信息 - 返回统一格式
    @PutMapping("/update")
    public Map<String, Object> updateRechargeRecord(@RequestBody RechargeRecord rechargeRecord) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean updateResult = rechargeRecordService.updateById(rechargeRecord);
            if (updateResult) {
                result.put("code", 200);
                result.put("message", "修改成功");
            } else {
                result.put("code", 500);
                result.put("message", "修改失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "修改失败");
        }
        return result;
    }

    // 删除充值记录 - 返回统一格式
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteRechargeRecord(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleteResult = rechargeRecordService.removeById(id);
            if (deleteResult) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 500);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败");
        }
        return result;
    }
}