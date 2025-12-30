package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.Computer;
import com.example.wangbamanagerserver.service.ComputerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/computer")
public class ComputerController {

    @Autowired
    private ComputerService computerService;

    // 获取所有计算机
    @GetMapping("/list")
    public Map<String, Object> getAllComputers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Computer> computers = computerService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", computers);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取计算机列表失败");
        }
        return result;
    }

    // 根据区域获取计算机
    @GetMapping("/list/by-area/{areaId}")
    public Map<String, Object> getComputersByArea(@PathVariable Long areaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Computer> computers = computerService.getComputersByAreaId(areaId);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", computers);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取计算机列表失败");
        }
        return result;
    }

    // 根据ID获取计算机
    @GetMapping("/{id}")
    public Map<String, Object> getComputerById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Computer computer = computerService.getById(id);
            if (computer != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", computer);
            } else {
                result.put("code", 404);
                result.put("message", "计算机不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取计算机详情失败");
        }
        return result;
    }

    // 添加计算机
    @PostMapping("/add")
    public Map<String, Object> addComputer(@RequestBody Computer computer) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean saveResult = computerService.save(computer);
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

    // 修改计算机
    @PutMapping("/update")
    public Map<String, Object> updateComputer(@RequestBody Computer computer) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean updateResult = computerService.updateById(computer);
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

    // 删除计算机
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteComputer(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleteResult = computerService.removeById(id);
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