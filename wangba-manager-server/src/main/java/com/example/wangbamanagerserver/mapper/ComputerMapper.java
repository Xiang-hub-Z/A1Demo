package com.example.wangbamanagerserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wangbamanagerserver.model.Computer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComputerMapper extends BaseMapper<Computer> {
}