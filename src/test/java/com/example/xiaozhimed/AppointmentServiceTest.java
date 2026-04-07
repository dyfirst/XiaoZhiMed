package com.example.xiaozhimed;

import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.mapper.AppointmentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Test
    public void testInsert() {
        Appointment appointment = new Appointment();
        appointment.setId(99L);
        appointment.setUsername("test");
        appointment.setIdCard("test");
        appointment.setDepartment("test");
        appointment.setDate("2020-08-08");
        appointment.setTime("12:00");
        appointment.setDoctorName("张医生");

        appointmentMapper.insert(appointment);
        System.out.println("新增数据");
    }

    @Test
    public void testFindAll() {
        appointmentMapper.selectList(null).forEach(System.out::println);
    }

    @Test
    public void testFindById() {
        appointmentMapper.selectById(99L);
    }

    @Test
    public void deleteById() {
        appointmentMapper.deleteById(99L);
    }
}
