package com.example.xiaozhimed.service;

import com.example.xiaozhimed.entity.Employee;

import java.util.List;

public interface EmployeeService {

    List<Employee> findAll();

    Employee findById(Long id);

    Employee create(Employee employee);

    Employee update(Long id, Employee employee);

    void deleteById(Long id);
}
