package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.UdemoMapper;
import com.example.wangbamanagerserver.model.Udemo;
import org.springframework.stereotype.Service;

@Service
public class UdemoServiceImpl extends ServiceImpl<UdemoMapper, Udemo> implements UdemoService{
}
