package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.bean.Result;
import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.exception.BizException;
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

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "预约管理")
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "新增预约", description = "将预约信息写入 MySQL 的 appointment 表。")
    @PostMapping
    public Result<Appointment> save(@Valid @RequestBody Appointment appointment) {
        appointmentService.save(appointment);
        return Result.success(appointment);
    }

    @Operation(summary = "查询单条预约", description = "根据主键 id 查询预约记录。")
    @GetMapping("/{id}")
    public Result<Appointment> getById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            throw new BizException(404, "预约记录不存在");
        }
        return Result.success(appointment);
    }

    @Operation(summary = "查询全部预约", description = "返回 appointment 表中的所有预约记录。")
    @GetMapping
    public Result<List<Appointment>> list() {
        return Result.success(appointmentService.list());
    }

    @Operation(summary = "删除预约", description = "根据主键 id 删除预约记录。")
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@PathVariable Long id) {
        if (appointmentService.getById(id) == null) {
            throw new BizException(404, "预约记录不存在");
        }
        return Result.success(appointmentService.removeById(id));
    }
}
