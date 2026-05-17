package com.example.xiaozhimed.tools;

import com.example.xiaozhimed.entity.Appointment;
import com.example.xiaozhimed.entity.DoctorSchedule;
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

    @Tool(name = "预约挂号", value = "在用户确认所有预约信息后调用此工具完成预约。如果用户没有提供具体的医生姓名，可以不填。预约前请确保已收集完整信息并让用户确认。")
    public String bookAppointment(Appointment appointment) {
        log.info("AI调用预约工具: {}", appointment);

        try {
            // 参数校验
            if (StringUtils.isBlank(appointment.getUsername())) {
                return "预约失败：姓名不能为空";
            }
            if (StringUtils.isBlank(appointment.getIdCard())) {
                return "预约失败：身份证号不能为空";
            }
            if (StringUtils.isBlank(appointment.getDepartment())) {
                return "预约失败：科室不能为空";
            }
            if (StringUtils.isBlank(appointment.getDate())) {
                return "预约失败：日期不能为空";
            }
            if (StringUtils.isBlank(appointment.getTime())) {
                return "预约失败：时间不能为空";
            }

            // 日期校验
            try {
                LocalDate appointDate = LocalDate.parse(appointment.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                if (appointDate.isBefore(LocalDate.now())) {
                    return "预约失败：不能预约过去的日期，请重新选择";
                }
            } catch (DateTimeParseException e) {
                return "预约失败：日期格式不正确，应为yyyy-MM-dd格式";
            }

            // 时间校验
            if (!"上午".equals(appointment.getTime()) && !"下午".equals(appointment.getTime())) {
                return "预约失败：时间只能是上午或下午";
            }

            // 查询重复预约
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if (appointmentDB != null) {
                return "您在" + appointment.getDepartment() + " " + appointment.getDate() + " " + appointment.getTime() + "已有预约，无需重复预约";
            }

            // 保存预约
            appointment.setId(null);
            if (appointmentService.save(appointment)) {
                return "预约成功！预约信息：科室=" + appointment.getDepartment()
                        + "，日期=" + appointment.getDate()
                        + "，时间=" + appointment.getTime()
                        + "，姓名=" + appointment.getUsername();
            } else {
                return "预约失败：系统异常，请稍后再试";
            }
        } catch (Exception e) {
            log.error("预约工具执行异常", e);
            return "预约失败：系统异常，请稍后再试";
        }
    }

    @Tool(name = "取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment) {
        log.info("AI调用取消预约工具: {}", appointment);

        try {
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if (appointmentDB != null) {
                if (appointmentService.removeById(appointmentDB.getId())) {
                    return "取消预约成功！已取消" + appointmentDB.getDepartment() + " " + appointmentDB.getDate() + " " + appointmentDB.getTime() + "的预约";
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

                // 检查该医生是否已有预约
                Appointment query = new Appointment();
                query.setDepartment(name);
                query.setDate(appointDate.toString());
                query.setTime(time);
                query.setDoctorName(doctorName);
                Appointment existing = appointmentService.getOne(query);
                if (existing != null) {
                    return "您在" + name + " " + appointDate + " " + time + " 医生【" + doctorName + "】已有预约";
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

    /**
     * 获取最近的工作日（跳过周末）
     */
    private LocalDate getNextWorkday(LocalDate date) {
        LocalDate result = date;
        while (result.getDayOfWeek().getValue() >= 6) { // 6=周六, 7=周日
            result = result.plusDays(1);
        }
        return result;
    }

    /**
     * 获取科室全部医生信息（含排班摘要）
     */
    private String getAllDoctorsInfo(String department) {
        List<DoctorSchedule> allSchedules = scheduleService.getAllSchedules(department);

        if (allSchedules.isEmpty()) {
            return "科室【" + department + "】暂无医生排班信息";
        }

        StringBuilder result = new StringBuilder();
        result.append("科室【").append(department).append("】的医生团队：\n");

        // 按医生分组，收集排班信息
        Map<String, List<DoctorSchedule>> doctorMap = allSchedules.stream()
                .collect(Collectors.groupingBy(DoctorSchedule::getDoctorName));

        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (Map.Entry<String, List<DoctorSchedule>> entry : doctorMap.entrySet()) {
            String doctorName = entry.getKey();
            List<DoctorSchedule> schedules = entry.getValue();

            // 取第一条记录的职称和擅长
            DoctorSchedule first = schedules.get(0);
            result.append("\n🔹 ").append(doctorName)
                    .append("（").append(first.getTitle()).append("）")
                    .append("\n   擅长：").append(first.getSpecialty());

            // 汇总排班时间
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