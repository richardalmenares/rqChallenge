package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.reliaquest.api.controller.exceptions.CannotGuaranteeEmployeeDeletionException;
import com.reliaquest.api.controller.exceptions.EmployeeNotDeletedException;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.EmployeeClient;
import com.reliaquest.api.service.client.model.Response;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    EmployeeClient employeeClient;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Test
    void getAllEmployees() {
        Response<List<Employee>> expected = new Response<>(
                List.of(new Employee("id", "name", 190, 20, "Mr", "email@email.com")),
                "Successfully processed request.",
                null);

        when(employeeClient.getAllEmployees()).thenReturn(expected);

        List<Employee> employees = employeeService.getAllEmployees();

        assertThat(employees).isEqualTo(expected.data());
    }

    @Test
    void getAllEmployees_fail() {
        when(employeeClient.getAllEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeService.getAllEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getEmployeesByNameSearch() {
        Response<List<Employee>> apiResponse = new Response<>(
                List.of(
                        new Employee("id", "Richard Calderin", 190, 20, "Mr", "email@email.com"),
                        new Employee("id", "Richard Almenares", 190, 20, "Mr", "email@email.com"),
                        new Employee("id", "Robert Lima", 190, 20, "Mr", "email@email.com")),
                "Successfully processed request.",
                null);

        when(employeeClient.getAllEmployees()).thenReturn(apiResponse);

        List<Employee> employees = employeeService.getEmployeesByNameSearch("Richard");

        assertThat(employees)
                .isEqualTo(List.of(
                        new Employee("id", "Richard Calderin", 190, 20, "Mr", "email@email.com"),
                        new Employee("id", "Richard Almenares", 190, 20, "Mr", "email@email.com")));
    }

    @Test
    void getEmployeesByNameSearch_fail() {
        when(employeeClient.getAllEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeService.getEmployeesByNameSearch("Richard"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getEmployee() {
        Response<Employee> expected = new Response<>(
                new Employee("id", "name", 190, 20, "Mr", "email@email.com"), "Successfully processed request.", null);

        when(employeeClient.getEmployee("id")).thenReturn(expected);

        Employee employee = employeeService.getEmployee("id");

        assertThat(employee).isEqualTo(expected.data());
    }

    @Test
    void getEmployee_fail() {
        when(employeeClient.getEmployee("id")).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeService.getEmployee("id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getHighestSalaryOfEmployees() {
        Response<List<Employee>> apiResponse = new Response<>(
                List.of(
                        new Employee("id", "Richard Calderin", 190, 20, "Mr", "email@email.com"),
                        new Employee("id", "Richard Almenares", 195, 20, "Mr", "email@email.com"),
                        new Employee("id", "Robert Lima", 180, 20, "Mr", "email@email.com")),
                "Successfully processed request.",
                null);

        when(employeeClient.getAllEmployees()).thenReturn(apiResponse);

        Integer actual = employeeService.getHighestSalaryOfEmployees();

        assertThat(actual).isEqualTo(195);
    }

    @Test
    void getHighestSalaryOfEmployees_fail() {
        when(employeeClient.getAllEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeService.getHighestSalaryOfEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = IntStream.range(0, 15)
                .mapToObj(index ->
                        new Employee("id", "Richard Calderin " + index, 190 + index, 20, "Mr", "email@email.com"))
                .toList();
        Response<List<Employee>> apiResponse = new Response<>(employees, "Successfully processed request.", null);

        when(employeeClient.getAllEmployees()).thenReturn(apiResponse);

        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();

        assertThat(names)
                .isEqualTo(List.of(
                        "Richard Calderin 14",
                        "Richard Calderin 13",
                        "Richard Calderin 12",
                        "Richard Calderin 11",
                        "Richard Calderin 10",
                        "Richard Calderin 9",
                        "Richard Calderin 8",
                        "Richard Calderin 7",
                        "Richard Calderin 6",
                        "Richard Calderin 5"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_fail() {
        when(employeeClient.getAllEmployees()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeService.getTopTenHighestEarningEmployeeNames())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void createEmployee() {
        CreateEmployee createEmployee = new CreateEmployee("Richard Test", 12345, 21, "Mr");
        Response<Employee> employeeResponse = new Response<>(
                new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                "Successfully processed request.",
                null);

        when(employeeClient.createEmployee(createEmployee)).thenReturn(employeeResponse);

        Employee employee = employeeService.createEmployee(createEmployee);

        assertThat(employee).isEqualTo(employeeResponse.data());
    }

    @Test
    void deleteEmployeeById_success() {
        Response<Employee> employeeResponse = new Response<>(
                new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                "Successfully processed request.",
                null);
        Response<List<Employee>> employeesResponse = new Response<>(
                List.of(
                        new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                        new Employee("id2", "Robert Test", 12345, 21, "Mr", "sample@email.com")),
                "Successfully processed request.",
                null);
        Response<Boolean> deleteResponse = new Response<>(true, "Successfully processed request.", null);

        when(employeeClient.getEmployee("id")).thenReturn(employeeResponse);
        when(employeeClient.getAllEmployees()).thenReturn(employeesResponse);
        when(employeeClient.deleteEmployee("Richard Test")).thenReturn(deleteResponse);

        String name = employeeService.deleteEmployeeById("id");

        assertThat(name).isEqualTo("Richard Test");
    }

    @Test
    void deleteEmployeeById_ServerSaysNotDeleted() {
        Response<Employee> employeeResponse = new Response<>(
                new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                "Successfully processed request.",
                null);
        Response<List<Employee>> employeesResponse = new Response<>(
                List.of(
                        new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                        new Employee("id2", "Robert Test", 12345, 21, "Mr", "sample@email.com")),
                "Successfully processed request.",
                null);
        Response<Boolean> deleteResponse = new Response<>(false, "Successfully processed request.", null);

        when(employeeClient.getEmployee("id")).thenReturn(employeeResponse);
        when(employeeClient.getAllEmployees()).thenReturn(employeesResponse);
        when(employeeClient.deleteEmployee("Richard Test")).thenReturn(deleteResponse);

        assertThatThrownBy(() -> employeeService.deleteEmployeeById("id"))
                .isInstanceOf(EmployeeNotDeletedException.class);
    }

    @Test
    void deleteEmployeeById_multiplePeopleSameName() {
        Response<Employee> employeeResponse = new Response<>(
                new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                "Successfully processed request.",
                null);
        Response<List<Employee>> employeesResponse = new Response<>(
                List.of(
                        new Employee("id", "Richard Test", 12345, 21, "Mr", "sample@email.com"),
                        new Employee("id2", "Richard Test", 12345, 21, "Mr", "sample@email.com")),
                "Successfully processed request.",
                null);

        when(employeeClient.getEmployee("id")).thenReturn(employeeResponse);
        when(employeeClient.getAllEmployees()).thenReturn(employeesResponse);

        assertThatThrownBy(() -> employeeService.deleteEmployeeById("id"))
                .isInstanceOf(CannotGuaranteeEmployeeDeletionException.class);
    }
}
