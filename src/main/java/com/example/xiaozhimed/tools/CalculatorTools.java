package com.example.xiaozhimed.tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CalculatorTools {

    @Tool(name = "加法运算", value = "将两个参数a和b相加并返回运算结果")
    double sum(@ToolMemoryId int memoryId, double a, double b){
        log.info("调用加法运算");
        System.out.println("memoryId为：" + memoryId);    //测试获取memoryId
        return a+b;
    }

    @Tool(name = "平方根运算")
    double squareRoot(double x){
        log.info("调用求平方根运算");
        return Math.sqrt(x);
    }
}
