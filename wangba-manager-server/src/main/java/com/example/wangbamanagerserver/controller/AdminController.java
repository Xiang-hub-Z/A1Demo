package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.Admin;
import com.example.wangbamanagerserver.model.User;
import com.example.wangbamanagerserver.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Map<String, Object> result = new HashMap<>();

        // 简单验证
        if (username == null || password == null) {
            result.put("success", false);
            result.put("message", "账号密码不能为空");
            return result;
        }

        Admin admin = adminService.login(username, password);

        if (admin != null) {
            // 登录成功
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", "admin_token_" + admin.getId());

            // 返回管理员信息（隐藏密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", admin.getId());
            userInfo.put("username", admin.getAdmin());
            userInfo.put("name", admin.getName());
            userInfo.put("phone", admin.getPhone());
            result.put("user", userInfo);
        } else {
            result.put("success", false);
            result.put("message", "账号或密码错误");
        }

        return result;
    }

    // 获取所有用户信息
    @GetMapping("/list")
    public List<Admin> getAllAdmins(){
        return adminService.list();
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public Admin getAdminById(@PathVariable Long id) {
        return adminService.getById(id);
    }

    // 添加用户
    @PostMapping("/add")
    public String addAdmin(@RequestBody Admin admin) {
        boolean result = adminService.save(admin);
        return result ? "添加成功" : "添加失败";
    }

    // 修改用户信息
    @PutMapping("/update")
    public String updateAdmin(@RequestBody Admin admin) {
        boolean result = adminService.updateById(admin);
        return result ? "修改成功" : "修改失败";
    }

    // 删除用户
    @DeleteMapping("/delete/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        boolean result = adminService.removeById(id);
        return result ? "删除成功" : "删除失败";
    }


}