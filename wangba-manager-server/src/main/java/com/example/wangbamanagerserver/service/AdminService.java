package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wangbamanagerserver.model.Admin;

public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     */
    Admin login(String admin, String password);
}