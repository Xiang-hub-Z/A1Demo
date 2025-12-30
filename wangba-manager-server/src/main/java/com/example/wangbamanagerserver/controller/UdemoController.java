package com.example.wangbamanagerserver.controller;

import com.example.wangbamanagerserver.model.Udemo;
import com.example.wangbamanagerserver.service.UdemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.List;

@RestController
@RequestMapping("/udemo")
public class UdemoController {

    @Autowired private UdemoService udemoService;

    //查询所有数据
    @GetMapping("/list")
    public List<Udemo> getAllUdemo(){
        return udemoService.list();
    }
    //根据id查数据
    @GetMapping("/list/{id}")
    public Udemo getUdemo(@PathVariable Long id){
        return udemoService.getById(id);
    }
    //增加数据
    @PostMapping("/add")
    public String addUdemo(@RequestBody Udemo udemo){
        boolean result = udemoService.save(udemo);
        return result ? "添加成功" : "添加失败";
    }
    //删除数据
    @DeleteMapping("/delete/{id}")
    public String delUdemo(@PathVariable Long id){
        boolean result = udemoService.removeById(id);
        return result ? "删除成功":"删除失败";
    }
    //修改数据
    @PutMapping("/update/{id}")
    public String updateUdemo(@PathVariable Long id,@RequestBody Udemo udemo){
        udemo.setId(id);
        boolean result = udemoService.updateById(udemo);
        return result ? "修改成功":"修改失败";
    }

}
