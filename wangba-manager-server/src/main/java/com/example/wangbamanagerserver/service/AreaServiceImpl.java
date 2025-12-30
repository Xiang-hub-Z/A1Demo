package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.AreaMapper;
import com.example.wangbamanagerserver.model.Area;
import com.example.wangbamanagerserver.service.AreaService;
import org.springframework.stereotype.Service;

@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {
}