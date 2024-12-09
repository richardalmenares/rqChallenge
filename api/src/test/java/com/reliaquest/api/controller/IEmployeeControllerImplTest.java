package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class IEmployeeControllerImplTest {

    @Mock
    EmployeeService employeeService;

    @InjectMocks
    IEmployeeControllerImpl iEmployeeController;

    @Test
    void getAllEmployees() {
        List<Employee> employees = List.of(new Employee("id", "Richard Test", 12345, 21, "Mr", "email@email.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> result = iEmployeeController.getAllEmployees();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(employees);
    }

    @Test
    void getAllEmployees_fail() {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.getAllEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getEmployeesByNameSearch() {
        List<Employee> employees = List.of(new Employee("id", "Richard Test", 12345, 21, "Mr", "email@email.com"));

        when(employeeService.getEmployeesByNameSearch("Richard")).thenReturn(employees);

        ResponseEntity<List<Employee>> result = iEmployeeController.getEmployeesByNameSearch("Richard");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(employees);
    }

    @Test
    void getEmployeesByNameSearch_fail() {
        when(employeeService.getEmployeesByNameSearch("Richard")).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.getEmployeesByNameSearch("Richard"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getEmployeeById() {
        Employee employee = new Employee("id", "Richard Test", 12345, 21, "Mr", "email@email.com");

        when(employeeService.getEmployee("id")).thenReturn(employee);

        ResponseEntity<Employee> result = iEmployeeController.getEmployeeById("id");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(employee);
    }

    @Test
    void getEmployeeById_fail() {
        when(employeeService.getEmployee("id")).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.getEmployeeById("id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(12345);

        ResponseEntity<Integer> result = iEmployeeController.getHighestSalaryOfEmployees();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(12345);
    }

    @Test
    void getHighestSalaryOfEmployees_fail() {
        when(employeeService.getHighestSalaryOfEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.getHighestSalaryOfEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(List.of("Richard Test"));

        ResponseEntity<List<String>> result = iEmployeeController.getTopTenHighestEarningEmployeeNames();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(List.of("Richard Test"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_fail() {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.getTopTenHighestEarningEmployeeNames())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void createEmployee() {
        CreateEmployee createEmployee = new CreateEmployee("Richard Test", 12345, 21, "Mr");
        Employee employee = new Employee("id", "Richard Test", 12345, 21, "Mr", "email@email.com");

        when(employeeService.createEmployee(createEmployee)).thenReturn(employee);

        ResponseEntity<Employee> result = iEmployeeController.createEmployee(createEmployee);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(result.getBody()).isEqualTo(employee);
    }

    @Test
    void createEmployee_fail() {
        CreateEmployee createEmployee = new CreateEmployee("Richard Test", 12345, 21, "Mr");

        when(employeeService.createEmployee(createEmployee)).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.createEmployee(createEmployee))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void deleteEmployeeById() {
        when(employeeService.deleteEmployeeById("id")).thenReturn("Richard");

        ResponseEntity<String> result = iEmployeeController.deleteEmployeeById("id");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo("Richard");
    }

    @Test
    void deleteEmployeeById_fail() {
        when(employeeService.deleteEmployeeById("id")).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> iEmployeeController.deleteEmployeeById("id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }
}
