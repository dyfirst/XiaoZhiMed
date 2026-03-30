package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.entity.Employee;
import com.example.xiaozhimed.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerTest {

    private MockMvc mockMvc;
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = Mockito.mock(EmployeeService.class);
        EmployeeController employeeController = new EmployeeController(employeeService);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    void findAllShouldReturnEmployeeList() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of(buildEmployee(1L, "管理员", "admin")));

        mockMvc.perform(get("/employees"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("管理员"))
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    void findByIdShouldReturnEmployee() throws Exception {
        when(employeeService.findById(1L)).thenReturn(buildEmployee(1L, "管理员", "admin"));

        mockMvc.perform(get("/employees/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.phone").value("13812312312"));
    }

    @Test
    void createShouldReturnCreatedEmployee() throws Exception {
        when(employeeService.create(any(Employee.class))).thenReturn(buildEmployee(2L, "测试员工", "test01"));

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试员工",
                                  "username": "test01",
                                  "password": "123456",
                                  "phone": "13812345678",
                                  "sex": "1",
                                  "idNumber": "110101199001010011",
                                  "status": 1,
                                  "createUser": 1,
                                  "updateUser": 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("test01"));
    }

    @Test
    void updateShouldReturnUpdatedEmployee() throws Exception {
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(buildEmployee(1L, "管理员-修改", "admin"));

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "管理员-修改",
                                  "username": "admin",
                                  "password": "e10adc3949ba59abbe56e057f20f883e",
                                  "phone": "13800000000",
                                  "sex": "1",
                                  "idNumber": "110101199001010047",
                                  "status": 1,
                                  "updateUser": 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("管理员-修改"))
                .andExpect(jsonPath("$.phone").value("13812312312"));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(employeeService).deleteById(2L);

        mockMvc.perform(delete("/employees/2"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(employeeService).deleteById(2L);
    }

    private Employee buildEmployee(Long id, String name, String username) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName(name);
        employee.setUsername(username);
        employee.setPassword("123456");
        employee.setPhone("13812312312");
        employee.setSex("1");
        employee.setIdNumber("110101199001010047");
        employee.setStatus(1);
        employee.setCreateTime(LocalDateTime.of(2022, 2, 15, 15, 51, 20));
        employee.setUpdateTime(LocalDateTime.of(2022, 2, 17, 9, 16, 20));
        employee.setCreateUser(10L);
        employee.setUpdateUser(1L);
        return employee;
    }
}
