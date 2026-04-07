package com.example.xiaozhimed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;

// 当前只验证 Web + AI 链路，临时跳过 MySQL/JDBC/MyBatis 自动配置。
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        MybatisAutoConfiguration.class
})
public class XiaozhiMedApp {

    public static void main(String[] args) {

        SpringApplication.run(XiaozhiMedApp.class, args);
    }

}
