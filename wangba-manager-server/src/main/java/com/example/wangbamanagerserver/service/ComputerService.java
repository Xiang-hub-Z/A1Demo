package com.example.wangbamanagerserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wangbamanagerserver.model.Computer;
import java.util.List;

public interface ComputerService extends IService<Computer> {

    /**
     * 根据区域ID获取计算机列表
     */
    List<Computer> getComputersByAreaId(Long areaId);

    /**
     * 检查计算机编号是否已存在
     */
    boolean isComputerNoExists(String computerNo);

    /**
     * 更新计算机状态
     */
    boolean updateComputerStatus(Long computerId, Integer status);

}