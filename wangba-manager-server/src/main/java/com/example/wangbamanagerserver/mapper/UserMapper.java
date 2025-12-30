package com.example.wangbamanagerserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wangbamanagerserver.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
