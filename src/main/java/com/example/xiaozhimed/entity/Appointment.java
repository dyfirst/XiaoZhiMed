package com.example.xiaozhimed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 显式指定数据库表名，避免实体类名和真实表名不一致时映射失败。
@TableName("appointment")
public class Appointment {

    // 主键使用数据库自增，插入时由 MySQL 自动生成。
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String idCard;
    private String department;
    private String date;
    private String time;
    private String doctorName;
}
