-- 医生排班模板表
CREATE TABLE IF NOT EXISTS doctor_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department VARCHAR(50) NOT NULL COMMENT '科室',
    doctor_name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(200) COMMENT '擅长领域',
    day_of_week TINYINT NOT NULL COMMENT '星期几 1=周一 7=周日',
    time_slot VARCHAR(10) NOT NULL COMMENT '时段：上午/下午',
    max_patients INT DEFAULT 15 COMMENT '每时段最大接诊数',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_doctor_slot (doctor_name, department, day_of_week, time_slot)
) COMMENT '医生排班模板表';

-- 排班例外表（节假日、临时停诊）
CREATE TABLE IF NOT EXISTS schedule_exception (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    department VARCHAR(50) NOT NULL COMMENT '科室',
    exception_date DATE NOT NULL COMMENT '例外日期',
    is_available TINYINT DEFAULT 0 COMMENT '0=停诊 1=加诊',
    time_slot VARCHAR(10) COMMENT '时段：上午/下午，NULL表示全天',
    reason VARCHAR(200) COMMENT '原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exception (doctor_name, department, exception_date, time_slot)
) COMMENT '排班例外表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 模拟用户数据
INSERT INTO `user` (`phone`, `name`, `id_card`) VALUES
('13800138000', '张三', '510101199001011234'),
('13900139000', '李四', '510101199202022345'),
('15000150000', '王五', '510101199303033456');

-- 预约挂号表
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '患者姓名',
    id_card VARCHAR(18) NOT NULL COMMENT '身份证号',
    department VARCHAR(50) NOT NULL COMMENT '预约科室',
    date VARCHAR(10) NOT NULL COMMENT '预约日期 yyyy-MM-dd',
    time VARCHAR(10) NOT NULL COMMENT '预约时段：上午/下午',
    doctor_name VARCHAR(50) COMMENT '预约医生',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '预约挂号表';
