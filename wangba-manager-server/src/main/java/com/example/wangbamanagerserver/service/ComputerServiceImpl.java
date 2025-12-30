package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wangbamanagerserver.mapper.ComputerMapper;
import com.example.wangbamanagerserver.model.Computer;
import com.example.wangbamanagerserver.service.ComputerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ComputerServiceImpl extends ServiceImpl<ComputerMapper, Computer> implements ComputerService {

    @Override
    public List<Computer> getComputersByAreaId(Long areaId) {
        LambdaQueryWrapper<Computer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Computer::getAreaId, areaId)
                .orderByAsc(Computer::getComputerNo);
        return this.list(queryWrapper);
    }

    @Override
    public boolean isComputerNoExists(String computerNo) {
        LambdaQueryWrapper<Computer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Computer::getComputerNo, computerNo);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean updateComputerStatus(Long computerId, Integer status) {
        try {
            Computer computer = this.getById(computerId);
            if (computer == null) {
                log.error("计算机不存在: computerId={}", computerId);
                return false;
            }

            computer.setStatus(status);
            return this.updateById(computer);
        } catch (Exception e) {
            log.error("更新计算机状态失败: computerId={}, status={}", computerId, status, e);
            return false;
        }
    }


}