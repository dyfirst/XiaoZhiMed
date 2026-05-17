package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class AppointmentFlowTest {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Autowired
    private AppointmentService appointmentService;

    private static final Long TEST_USER_ID = 888888L;
    private static final String TODAY = LocalDate.now().toString();

    /**
     * 测试完整的预约流程：聊天 → 工具调用 → 写入数据库 → 查询验证
     */
    @Test
    public void testAppointmentBookingFlow() {
        // 计算下一个周三（确保有排班）
        LocalDate nextWednesday = LocalDate.now();
        while (nextWednesday.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            nextWednesday = nextWednesday.plusDays(1);
        }
        String wednesdayStr = nextWednesday.toString();

        System.out.println("=".repeat(60));
        System.out.println("测试：通过聊天预约神经内科陈永平医生");
        System.out.println("预约日期：" + wednesdayStr + "（周三）上午");
        System.out.println("=".repeat(60));

        // 第1轮：用户描述症状，AI推荐科室
        System.out.println("\n【第1轮】用户: 我最近手总是发抖，应该挂什么科？");
        System.out.println("-".repeat(40));
        String response1 = chat("我最近手总是发抖，应该挂什么科？");
        System.out.println("AI: " + response1);

        // 第2轮：用户要求查看医生
        System.out.println("\n【第2轮】用户: 帮我看看神经内科有哪些医生");
        System.out.println("-".repeat(40));
        String response2 = chat("帮我看看神经内科有哪些医生");
        System.out.println("AI: " + response2);

        // 第3轮：用户询问排班
        System.out.println("\n【第3轮】用户: 陈永平医生" + wednesdayStr + "上午有号吗？");
        System.out.println("-".repeat(40));
        String response3 = chat("陈永平医生" + wednesdayStr + "上午有号吗？");
        System.out.println("AI: " + response3);

        // 第4轮：用户提供信息并预约
        System.out.println("\n【第4轮】用户: 帮我预约，我叫测试用户，身份证号510102199001011234");
        System.out.println("-".repeat(40));
        String response4 = chat("帮我预约，我叫测试用户，身份证号510102199001011234");
        System.out.println("AI: " + response4);

        // 第5轮：用户确认预约
        System.out.println("\n【第5轮】用户: 确认预约");
        System.out.println("-".repeat(40));
        String response5 = chat("确认预约");
        System.out.println("AI: " + response5);

        // 验证数据库
        System.out.println("\n" + "=".repeat(60));
        System.out.println("查询数据库验证预约结果");
        System.out.println("=".repeat(60));

        List<Appointment> appointments = appointmentService.list();
        System.out.println("预约表总记录数: " + appointments.size());

        for (Appointment apt : appointments) {
            System.out.println("\n预约记录:");
            System.out.println("  ID: " + apt.getId());
            System.out.println("  姓名: " + apt.getUsername());
            System.out.println("  身份证: " + apt.getIdCard());
            System.out.println("  科室: " + apt.getDepartment());
            System.out.println("  日期: " + apt.getDate());
            System.out.println("  时间: " + apt.getTime());
            System.out.println("  医生: " + apt.getDoctorName());
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试完成");
    }

    /**
     * 测试查询已有预约
     */
    @Test
    public void testQueryAppointments() {
        System.out.println("=".repeat(60));
        System.out.println("测试：查询预约情况");
        System.out.println("=".repeat(60));

        // 通过聊天查询
        System.out.println("\n【用户】: 我想查一下我的预约记录");
        System.out.println("-".repeat(40));
        String response = chat("我想查一下我的预约记录");
        System.out.println("AI: " + response);

        // 直接查数据库
        System.out.println("\n" + "-".repeat(40));
        System.out.println("数据库中的预约记录:");
        List<Appointment> appointments = appointmentService.list();

        if (appointments.isEmpty()) {
            System.out.println("  暂无预约记录");
        } else {
            for (Appointment apt : appointments) {
                System.out.println("  [" + apt.getId() + "] " + apt.getUsername()
                        + " - " + apt.getDepartment()
                        + " - " + apt.getDate() + " " + apt.getTime()
                        + " - " + apt.getDoctorName());
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试完成");
    }

    private String chat(String message) {
        List<String> response = xiaozhiAgent.chat(TEST_USER_ID, message, TODAY)
                .collectList().block();
        return String.join("", response);
    }
}
