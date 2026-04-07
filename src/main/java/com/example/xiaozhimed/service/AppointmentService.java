package com.example.xiaozhimed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xiaozhimed.entity.Appointment;

// 复用 MyBatis-Plus 的通用 Service 能力，减少基础 CRUD 的重复代码。
public interface AppointmentService extends IService<Appointment> {

    Appointment getOne(Appointment appointment);
}
