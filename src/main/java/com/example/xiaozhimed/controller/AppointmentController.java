package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 暴露预约挂号的基础 CRUD 接口，便于直接验证 MySQL 和 MyBatis-Plus 是否已经打通。
@Tag(name = "预约管理")
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "新增预约", description = "将预约信息写入 MySQL 的 appointment 表。")
    @PostMapping
    public Appointment save(@RequestBody Appointment appointment) {
        // 保存成功后回写自增主键，便于前端或测试直接拿到数据库记录 ID。
        appointmentService.save(appointment);
        return appointment;
    }

    @Operation(summary = "查询单条预约", description = "根据主键 id 查询预约记录。")
    @GetMapping("/{id}")
    public Appointment getById(@PathVariable Long id) {
        return appointmentService.getById(id);
    }

    @Operation(summary = "查询全部预约", description = "返回 appointment 表中的所有预约记录。")
    @GetMapping
    public List<Appointment> list() {
        return appointmentService.list();
    }

    @Operation(summary = "删除预约", description = "根据主键 id 删除预约记录。")
    @DeleteMapping("/{id}")
    public boolean removeById(@PathVariable Long id) {
        return appointmentService.removeById(id);
    }
}
