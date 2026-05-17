package com.example.xiaozhimed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("schedule_exception")
public class ScheduleException {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String doctorName;
    private String department;
    private LocalDate exceptionDate;
    private Integer isAvailable;    // 0=停诊, 1=加诊
    private String timeSlot;
    private String reason;
    private LocalDateTime createdAt;
}
