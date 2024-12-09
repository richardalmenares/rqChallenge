package com.reliaquest.api.service;

import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Employee getEmployee(String employeeId);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Employee createEmployee(CreateEmployee employeeInput);

    String deleteEmployeeById(String id);
}
