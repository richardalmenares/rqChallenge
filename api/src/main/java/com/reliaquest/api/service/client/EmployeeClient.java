package com.reliaquest.api.service.client;

import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.model.Response;
import java.util.List;

public interface EmployeeClient {

    Response<List<Employee>> getAllEmployees();

    Response<Employee> getEmployee(String employeeId);

    Response<Employee> createEmployee(CreateEmployee createEmployee);

    Response<Boolean> deleteEmployee(String employeeName);
}
