package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.AdminMapper;
import com.example.wangbamanagerserver.model.Admin;
import com.example.wangbamanagerserver.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin login(String admin, String password) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin", admin)
                .eq("password", password);
        return getOne(queryWrapper);
    }
}