package com.example.xiaozhimed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// 恢复默认自动配置，让 Spring Boot 按 application.yaml 自动连接 MySQL 与 MongoDB。
@SpringBootApplication
public class XiaozhiMedApp {

    public static void main(String[] args) {

        SpringApplication.run(XiaozhiMedApp.class, args);
    }

}
