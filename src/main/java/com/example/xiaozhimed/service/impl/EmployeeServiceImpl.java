package com.example.xiaozhimed.service.impl;

import com.example.xiaozhimed.entity.Employee;
import com.example.xiaozhimed.mapper.EmployeeMapper;
import com.example.xiaozhimed.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public List<Employee> findAll() {
        return employeeMapper.findAll();
    }

    @Override
    public Employee findById(Long id) {
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new ResponseStatusException(NOT_FOUND, "employee not found: " + id);
        }
        return employee;
    }

    @Override
    public Employee create(Employee employee) {
        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        if (employee.getCreateUser() == null) {
            employee.setCreateUser(1L);
        }
        if (employee.getUpdateUser() == null) {
            employee.setUpdateUser(employee.getCreateUser());
        }
        employeeMapper.insert(employee);
        return employeeMapper.findById(employee.getId());
    }

    @Override
    public Employee update(Long id, Employee employee) {
        findById(id);
        employee.setId(id);
        employee.setUpdateTime(LocalDateTime.now());
        if (employee.getUpdateUser() == null) {
            employee.setUpdateUser(1L);
        }
        int rows = employeeMapper.update(employee);
        if (rows == 0) {
            throw new ResponseStatusException(NOT_FOUND, "employee not found: " + id);
        }
        return employeeMapper.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        int rows = employeeMapper.deleteById(id);
        if (rows == 0) {
            throw new ResponseStatusException(NOT_FOUND, "employee not found: " + id);
        }
    }
}
