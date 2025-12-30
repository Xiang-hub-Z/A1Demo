package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.User;
import com.example.wangbamanagerserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (password == null || password.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }

        User user = userService.login(username, password);
        if (user != null) {
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
        } else {
            result.put("code", 400);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    // 用户注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }
        if (user.getRealname() == null || user.getRealname().trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "真实姓名不能为空");
            return result;
        }

        // 检查用户名是否已存在
        if (userService.isUsernameExists(user.getUsername())) {
            result.put("code", 400);
            result.put("message", "用户名已存在");
            return result;
        }

        // 设置默认余额为0
        if (user.getBalance() == null) {
            user.setBalance(0.0);
        }

        boolean registerResult = userService.register(user);
        if (registerResult) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 500);
            result.put("message", "注册失败");
        }
        return result;
    }

    /**
     * 用户充值
     */
    @PostMapping("/recharge")
    public Map<String, Object> recharge(@RequestBody Map<String, Object> rechargeData) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.valueOf(rechargeData.get("userId").toString());
            Double amount = Double.valueOf(rechargeData.get("amount").toString());
            String orderId = (String) rechargeData.get("orderId");
            String paymentMethod = (String) rechargeData.get("paymentMethod"); // 获取支付方式

            // 参数验证
            if (userId == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                return result;
            }
            if (amount == null || amount <= 0) {
                result.put("code", 400);
                result.put("message", "充值金额必须大于0");
                return result;
            }
            if (orderId == null || orderId.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "订单号不能为空");
                return result;
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "支付方式不能为空");
                return result;
            }

            // 调用服务层进行充值（现在传递四个参数）
            boolean rechargeResult = userService.recharge(userId, amount, orderId, paymentMethod);
            if (rechargeResult) {
                // 获取更新后的用户信息
                User updatedUser = userService.getById(userId);
                result.put("code", 200);
                result.put("message", "充值成功");
                result.put("data", updatedUser);
            } else {
                result.put("code", 500);
                result.put("message", "充值失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "充值失败: " + e.getMessage());
        }

        return result;
    }

    // 获取所有用户信息
    @GetMapping("/list")
    public List<User> getAllUsers(){
        return userService.list();
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // 添加用户
    @PostMapping("/add")
    public String addUser(@RequestBody User user) {
        boolean result = userService.save(user);
        return result ? "添加成功" : "添加失败";
    }

    // 修改用户信息
    @PutMapping("/update")
    public String updateUser(@RequestBody User user) {
        boolean result = userService.updateById(user);
        return result ? "修改成功" : "修改失败";
    }

    // 删除用户
    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        boolean result = userService.removeById(id);
        return result ? "删除成功" : "删除失败";
    }
}