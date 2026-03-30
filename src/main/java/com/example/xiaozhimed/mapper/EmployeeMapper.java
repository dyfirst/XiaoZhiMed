package com.example.xiaozhimed.mapper;

import com.example.xiaozhimed.entity.Employee;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("""
            select id, name, username, password, phone, sex, id_number, status,
                   create_time, update_time, create_user, update_user
            from employee
            order by id
            """)
    List<Employee> findAll();

    @Select("""
            select id, name, username, password, phone, sex, id_number, status,
                   create_time, update_time, create_user, update_user
            from employee
            where id = #{id}
            """)
    Employee findById(Long id);

    @Insert("""
            insert into employee
            (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
            values
            (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status},
             #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);

    @Update("""
            update employee
            set name = #{name},
                username = #{username},
                password = #{password},
                phone = #{phone},
                sex = #{sex},
                id_number = #{idNumber},
                status = #{status},
                update_time = #{updateTime},
                update_user = #{updateUser}
            where id = #{id}
            """)
    int update(Employee employee);

    @Delete("delete from employee where id = #{id}")
    int deleteById(Long id);
}
