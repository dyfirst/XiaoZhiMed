package com.example.xiaozhimed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiaozhimed.entity.DoctorSchedule;
import com.example.xiaozhimed.entity.ScheduleException;
import com.example.xiaozhimed.mapper.DoctorScheduleMapper;
import com.example.xiaozhimed.mapper.ScheduleExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private DoctorScheduleMapper scheduleMapper;

    @Autowired
    private ScheduleExceptionMapper exceptionMapper;

    /**
     * 查询指定科室、日期、时间的可用医生
     */
    public List<DoctorSchedule> getAvailableDoctors(String department, LocalDate date, String timeSlot) {
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=周一, 7=周日

        // 1. 查询排班模板
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDepartment, department)
                .eq(DoctorSchedule::getDayOfWeek, dayOfWeek)
                .eq(DoctorSchedule::getTimeSlot, timeSlot)
                .eq(DoctorSchedule::getIsActive, 1);

        List<DoctorSchedule> scheduledDoctors = scheduleMapper.selectList(wrapper);

        if (scheduledDoctors.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 排除当天停诊的医生
        LambdaQueryWrapper<ScheduleException> exceptionWrapper = new LambdaQueryWrapper<>();
        exceptionWrapper.eq(ScheduleException::getDepartment, department)
                .eq(ScheduleException::getExceptionDate, date)
                .eq(ScheduleException::getIsAvailable, 0);

        List<ScheduleException> exceptions = exceptionMapper.selectList(exceptionWrapper);
        Set<String> unavailableDoctors = exceptions.stream()
                .filter(e -> e.getTimeSlot() == null || e.getTimeSlot().equals(timeSlot))
                .map(ScheduleException::getDoctorName)
                .collect(Collectors.toSet());

        return scheduledDoctors.stream()
                .filter(doc -> !unavailableDoctors.contains(doc.getDoctorName()))
                .collect(Collectors.toList());
    }

    /**
     * 查询指定医生的排班信息
     */
    public List<DoctorSchedule> getDoctorSchedules(String department, String doctorName) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDepartment, department)
                .eq(DoctorSchedule::getDoctorName, doctorName)
                .eq(DoctorSchedule::getIsActive, 1);
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 查询指定科室的所有排班记录
     */
    public List<DoctorSchedule> getAllSchedules(String department) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDepartment, department)
                .eq(DoctorSchedule::getIsActive, 1);
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 检查指定科室是否存在
     */
    public boolean departmentExists(String department) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDepartment, department)
                .eq(DoctorSchedule::getIsActive, 1);
        return scheduleMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取所有科室名称
     */
    public Set<String> getAllDepartments() {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getIsActive, 1)
                .select(DoctorSchedule::getDepartment)
                .groupBy(DoctorSchedule::getDepartment);

        return scheduleMapper.selectList(wrapper).stream()
                .map(DoctorSchedule::getDepartment)
                .collect(Collectors.toSet());
    }

    /**
     * 查询医生的最大接诊数
     */
    public int getMaxPatients(String department, String doctorName, int dayOfWeek, String timeSlot) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDepartment, department)
                .eq(DoctorSchedule::getDoctorName, doctorName)
                .eq(DoctorSchedule::getDayOfWeek, dayOfWeek)
                .eq(DoctorSchedule::getTimeSlot, timeSlot)
                .eq(DoctorSchedule::getIsActive, 1);

        DoctorSchedule schedule = scheduleMapper.selectOne(wrapper);
        return schedule != null ? schedule.getMaxPatients() : 15;
    }
}
