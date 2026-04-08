package com.example.xiaozhimed;

import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Test
    public void testInsert() {
        Appointment appointment = new Appointment();
        appointment.setId(101L);
        appointment.setUsername("张三");
        appointment.setIdCard("360502199911114123");
        appointment.setDepartment("神经内科");
        appointment.setDate("2020-08-08");
        appointment.setTime("12:00");
        appointment.setDoctorName("张医生");

        appointmentService.save(appointment);
        System.out.println("新增数据");
    }

    @Test
    public void testGetOne() {
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("360502199911114123");
        appointment.setDepartment("神经内科");
        appointment.setDate("2020-08-08");
        appointment.setTime("12:00");
        System.out.println(appointmentService.getOne(appointment));
    }

    @Test
    public void testUpdate() {
        Appointment appointment = new Appointment();
        appointment.setId(99L);
        appointment.setUsername("lisi");
        appointment.setIdCard("360502199911114123");
        appointment.setDepartment("骨科");
        appointmentService.updateById(appointment);
        System.out.println("更新主键为"+appointment.getId()+"的内容");
    }

    @Test
    public void testFindById() {
        appointmentService.getById(99L);
    }

    @Test
    public void testRemoveById() {
        appointmentService.removeById(1L);
    }

}

