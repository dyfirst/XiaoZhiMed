package com.example.xiaozhimed.tools;

import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.entity.DoctorSchedule;
import com.example.xiaozhimed.entity.User;
import com.example.xiaozhimed.mapper.UserMapper;
import com.example.xiaozhimed.service.AppointmentService;
import com.example.xiaozhimed.service.ScheduleService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AppointmentTools {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserMapper userMapper;

    @Tool(name = "预约挂号", value = "在用户确认预约信息后调用此工具完成预约。用户信息（姓名、身份证号）已自动获取，不需要用户提供。只需要科室、日期、时间，医生可选。")
    public String bookAppointment(
            @P(value = "用户ID") Long userId,
            @P(value = "预约科室") String department,
            @P(value = "预约日期，格式yyyy-MM-dd") String date,
            @P(value = "预约时间，上午或下午") String time,
            @P(value = "预约医生姓名，可选", required = false) String doctorName
    ) {
        log.info("AI调用预约工具: userId={}, department={}, date={}, time={}, doctor={}", userId, department, date, time, doctorName);

        try {
            // 获取用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                return "预约失败：用户信息不存在，请重新登录";
            }

            // 参数校验
            if (StringUtils.isBlank(department)) {
                return "预约失败：科室不能为空";
            }
            if (StringUtils.isBlank(date)) {
                return "预约失败：日期不能为空";
            }
            if (StringUtils.isBlank(time)) {
                return "预约失败：时间不能为空";
            }

            // 日期校验
            try {
                LocalDate appointDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                if (appointDate.isBefore(LocalDate.now())) {
                    return "预约失败：不能预约过去的日期，请重新选择";
                }
            } catch (DateTimeParseException e) {
                return "预约失败：日期格式不正确，应为yyyy-MM-dd格式";
            }

            // 时间校验
            if (!"上午".equals(time) && !"下午".equals(time)) {
                return "预约失败：时间只能是上午或下午";
            }

            // 构建预约对象
            Appointment appointment = new Appointment();
            appointment.setUsername(user.getName());
            appointment.setIdCard(user.getIdCard());
            appointment.setDepartment(department);
            appointment.setDate(date);
            appointment.setTime(time);
            appointment.setDoctorName(doctorName);

            // 查询重复预约
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if (appointmentDB != null) {
                return "您在" + department + " " + date + " " + time + "已有预约，无需重复预约";
            }

            // 保存预约
            appointment.setId(null);
            if (appointmentService.save(appointment)) {
                String doctorPart = StringUtils.isBlank(doctorName) ? "未指定医生" : doctorName;
                return "预约成功！预约信息：科室=" + department
                        + "，日期=" + date
                        + "，时间=" + time
                        + "，医生=" + doctorPart
                        + "，姓名=" + user.getName();
            } else {
                return "预约失败：系统异常，请稍后再试";
            }
        } catch (Exception e) {
            log.error("预约工具执行异常", e);
            return "预约失败：系统异常，请稍后再试";
        }
    }

    @Tool(name = "取消预约挂号", value = "根据用户ID和预约信息，查询并取消预约。用户信息已自动获取。")
    public String cancelAppointment(
            @P(value = "用户ID") Long userId,
            @P(value = "预约科室") String department,
            @P(value = "预约日期，格式yyyy-MM-dd") String date,
            @P(value = "预约时间，上午或下午") String time
    ) {
        log.info("AI调用取消预约工具: userId={}, department={}, date={}, time={}", userId, department, date, time);

        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return "取消预约失败：用户信息不存在";
            }

            Appointment query = new Appointment();
            query.setUsername(user.getName());
            query.setIdCard(user.getIdCard());
            query.setDepartment(department);
            query.setDate(date);
            query.setTime(time);

            Appointment appointmentDB = appointmentService.getOne(query);
            if (appointmentDB != null) {
                if (appointmentService.removeById(appointmentDB.getId())) {
                    return "取消预约成功！已取消" + department + " " + date + " " + time + "的预约";
                } else {
                    return "取消预约失败：系统异常，请稍后再试";
                }
            }
            return "未找到匹配的预约记录，请核对科室和时间后重试";
        } catch (Exception e) {
            log.error("取消预约工具执行异常", e);
            return "取消预约失败：系统异常，请稍后再试";
        }
    }

    @Tool(name = "查询我的预约记录", value = "查询当前用户的预约记录。用户信息已自动获取，不需要提供姓名和身份证号。")
    public String queryMyAppointments(@P(value = "用户ID") Long userId) {
        log.info("AI调用预约记录查询工具: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return "查询失败：用户信息不存在";
        }

        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getUsername, user.getName())
                .eq(Appointment::getIdCard, user.getIdCard())
                .orderByAsc(Appointment::getDate)
                .orderByAsc(Appointment::getTime)
                .list();

        if (appointments.isEmpty()) {
            return "当前未查询到您的预约记录。";
        }

        List<String> lines = new ArrayList<>();
        lines.add("已为您查询到以下预约记录：");
        for (Appointment appointment : appointments) {
            String doctorPart = StringUtils.isBlank(appointment.getDoctorName()) ? "未指定医生" : appointment.getDoctorName();
            lines.add("- " + appointment.getDate() + " " + appointment.getTime()
                    + "｜" + appointment.getDepartment()
                    + "｜" + doctorPart);
        }
        return String.join("\n", lines);
    }

    @Tool(name = "查询排班与号源", value = "查询指定科室的医生排班和号源情况。当用户问'有哪些医生'、'哪个医生好'、'医生排班'、'有号吗'等问题时，都应调用此工具。如果用户未指定日期，默认查询最近的工作日。如果用户未指定医生，返回该科室所有排班医生列表。注意：不要编造医生姓名。")
    public String queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期，格式yyyy-MM-dd，可选", required = false) String date,
            @P(value = "时间，可选值：上午、下午，可选", required = false) String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        log.info("查询号源: 科室={}, 日期={}, 时间={}, 医生={}", name, date, time, doctorName);

        try {
            // 科室校验
            if (StringUtils.isBlank(name)) {
                return "查询失败：科室名称不能为空";
            }

            // 检查科室是否存在
            if (!scheduleService.departmentExists(name)) {
                return "查询失败：科室【" + name + "】不存在，请确认科室名称";
            }

            // 如果日期和时间都没指定，返回该科室全部医生信息
            if (StringUtils.isBlank(date) && StringUtils.isBlank(time)) {
                return getAllDoctorsInfo(name);
            }

            // 日期处理：未指定则查最近的工作日
            LocalDate appointDate;
            if (StringUtils.isBlank(date)) {
                appointDate = getNextWorkday(LocalDate.now());
            } else {
                try {
                    appointDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                    if (appointDate.isBefore(LocalDate.now())) {
                        return "查询失败：不能查询过去的日期";
                    }
                } catch (DateTimeParseException e) {
                    return "查询失败：日期格式不正确，应为yyyy-MM-dd格式";
                }
            }

            // 时间处理：未指定则查上午和下午
            boolean queryBothSlots = StringUtils.isBlank(time);
            if (!queryBothSlots && !"上午".equals(time) && !"下午".equals(time)) {
                return "查询失败：时间只能是上午或下午";
            }

            // 如果未指定时间，查询两个时段
            if (queryBothSlots) {
                StringBuilder result = new StringBuilder();
                result.append("科室【").append(name).append("】在 ").append(appointDate).append("（")
                        .append(getDayOfWeekName(appointDate)).append("）的排班：\n");

                List<DoctorSchedule> morningDoctors = scheduleService.getAvailableDoctors(name, appointDate, "上午");
                List<DoctorSchedule> afternoonDoctors = scheduleService.getAvailableDoctors(name, appointDate, "下午");

                if (morningDoctors.isEmpty() && afternoonDoctors.isEmpty()) {
                    return "科室【" + name + "】在 " + appointDate + "（" + getDayOfWeekName(appointDate) + "）没有排班";
                }

                if (!morningDoctors.isEmpty()) {
                    result.append("\n【上午】");
                    for (DoctorSchedule doc : morningDoctors) {
                        result.append("\n- ").append(doc.getDoctorName())
                                .append("（").append(doc.getTitle()).append("）")
                                .append(" - ").append(doc.getSpecialty());
                    }
                }

                if (!afternoonDoctors.isEmpty()) {
                    result.append("\n\n【下午】");
                    for (DoctorSchedule doc : afternoonDoctors) {
                        result.append("\n- ").append(doc.getDoctorName())
                                .append("（").append(doc.getTitle()).append("）")
                                .append(" - ").append(doc.getSpecialty());
                    }
                }

                result.append("\n\n如需预约，请告诉我您想预约哪位医生和时段。");
                return result.toString();
            }

            // 查询排班
            List<DoctorSchedule> availableDoctors = scheduleService.getAvailableDoctors(name, appointDate, time);

            if (availableDoctors.isEmpty()) {
                String dayOfWeek = getDayOfWeekName(appointDate);
                return "科室【" + name + "】在 " + date + "（" + dayOfWeek + "）" + time + " 没有排班，建议选择其他日期";
            }

            // 如果指定了医生，检查该医生是否有排班
            if (StringUtils.isNotBlank(doctorName)) {
                boolean doctorAvailable = availableDoctors.stream()
                        .anyMatch(doc -> doc.getDoctorName().equals(doctorName));

                if (!doctorAvailable) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("医生【").append(doctorName).append("】在 ").append(appointDate).append(" ").append(time).append(" 没有排班。\n");
                    sb.append("该时段有排班的医生：");
                    for (DoctorSchedule doc : availableDoctors) {
                        sb.append("\n- ").append(doc.getDoctorName()).append("（").append(doc.getTitle()).append("）- ").append(doc.getSpecialty());
                    }
                    return sb.toString();
                }

                return "医生【" + doctorName + "】在 " + appointDate + " " + time + " 有号源可预约。\n擅长领域：" + getDoctorSpecialty(availableDoctors, doctorName);
            }

            // 未指定医生，返回该时段所有可用医生
            StringBuilder result = new StringBuilder();
            result.append("科室【").append(name).append("】在 ").append(appointDate).append(" ").append(time).append(" 有以下医生可预约：\n");
            for (DoctorSchedule doc : availableDoctors) {
                result.append("\n- ").append(doc.getDoctorName())
                        .append("（").append(doc.getTitle()).append("）")
                        .append(" - 擅长：").append(doc.getSpecialty());
            }
            result.append("\n\n如需预约，请告诉我您想预约哪位医生。");

            return result.toString();
        } catch (Exception e) {
            log.error("查询号源工具执行异常", e);
            return "查询失败：系统异常，请稍后再试";
        }
    }

    private String getDayOfWeekName(LocalDate date) {
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return days[date.getDayOfWeek().getValue() - 1];
    }

    private String getDoctorSpecialty(List<DoctorSchedule> doctors, String doctorName) {
        return doctors.stream()
                .filter(doc -> doc.getDoctorName().equals(doctorName))
                .map(DoctorSchedule::getSpecialty)
                .findFirst()
                .orElse("暂无信息");
    }

    private LocalDate getNextWorkday(LocalDate date) {
        LocalDate result = date;
        while (result.getDayOfWeek().getValue() >= 6) {
            result = result.plusDays(1);
        }
        return result;
    }

    private String getAllDoctorsInfo(String department) {
        List<DoctorSchedule> allSchedules = scheduleService.getAllSchedules(department);

        if (allSchedules.isEmpty()) {
            return "科室【" + department + "】暂无医生排班信息";
        }

        StringBuilder result = new StringBuilder();
        result.append("科室【").append(department).append("】的医生团队：\n");

        Map<String, List<DoctorSchedule>> doctorMap = allSchedules.stream()
                .collect(Collectors.groupingBy(DoctorSchedule::getDoctorName));

        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (Map.Entry<String, List<DoctorSchedule>> entry : doctorMap.entrySet()) {
            String doctorName = entry.getKey();
            List<DoctorSchedule> schedules = entry.getValue();

            DoctorSchedule first = schedules.get(0);
            result.append("\n🔹 ").append(doctorName)
                    .append("（").append(first.getTitle()).append("）")
                    .append("\n   擅长：").append(first.getSpecialty());

            StringBuilder scheduleStr = new StringBuilder();
            for (DoctorSchedule s : schedules) {
                if (scheduleStr.length() > 0) scheduleStr.append("、");
                scheduleStr.append(dayNames[s.getDayOfWeek()]).append(s.getTimeSlot());
            }
            result.append("\n   出诊：").append(scheduleStr);
        }

        result.append("\n\n如需预约，请告诉我您想预约哪位医生和日期时段。");
        return result.toString();
    }
}
