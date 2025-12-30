package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.Area;
import com.example.wangbamanagerserver.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/area")
public class AreaController {

    @Autowired
    private AreaService areaService;

    // 获取所有区域
    @GetMapping("/list")
    public Map<String, Object> getAllAreas() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Area> areas = areaService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", areas);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取区域列表失败");
        }
        return result;
    }

    // 添加区域
    @PostMapping("/add")
    public Map<String, Object> addArea(@RequestBody Area area) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean saveResult = areaService.save(area);
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

    // 修改区域
    @PutMapping("/update")
    public Map<String, Object> updateArea(@RequestBody Area area) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean updateResult = areaService.updateById(area);
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

    // 删除区域
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteArea(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleteResult = areaService.removeById(id);
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