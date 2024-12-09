package com.reliaquest.api.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.reliaquest.api.controller.exceptions.CannotGuaranteeEmployeeDeletionException;
import com.reliaquest.api.controller.exceptions.EmployeeNotDeletedException;
import com.reliaquest.api.controller.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.controller.exceptions.TooManyRequestException;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IEmployeeControllerImplIntegrationTest {

    @Autowired
    RestClient.Builder restclientBuilder;

    @MockBean
    EmployeeService employeeService;

    RestClient restClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        restClient = restclientBuilder
                .defaultStatusHandler(httpStatusCode -> true, (request, response) -> {
                    // no op
                })
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void getAllEmployees() {
        List<Employee> employees = List.of(new Employee("id", "Richard Test", 12345, 21, "Mr", "email@mail.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);

        String result = restClient
                .get()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThatJson(result)
                .isEqualTo(
                        """
                [
                  {
                    "id": "id",
                    "name": "Richard Test",
                    "salary": 12345,
                    "age": 21,
                    "title": "Mr",
                    "email": "email@mail.com"
                  }
                ]
                """);
    }

    @Test
    void getEmployeesByNameSearch() {
        List<Employee> employees = List.of(new Employee("id", "Richard Test", 12345, 21, "Mr", "email@mail.com"));

        when(employeeService.getEmployeesByNameSearch("Richard")).thenReturn(employees);

        String result = restClient
                .get()
                .uri("/api/v1/employee/search/{searchString}", "Richard")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThatJson(result)
                .isEqualTo(
                        """
                [
                  {
                    "id": "id",
                    "name": "Richard Test",
                    "salary": 12345,
                    "age": 21,
                    "title": "Mr",
                    "email": "email@mail.com"
                  }
                ]
                """);
    }

    @Test
    void getEmployeeById() {
        Employee employee = new Employee("id", "Richard Test", 12345, 21, "Mr", "email@mail.com");

        when(employeeService.getEmployee("id")).thenReturn(employee);

        String result = restClient
                .get()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThatJson(result)
                .isEqualTo(
                        """
                  {
                    "id": "id",
                    "name": "Richard Test",
                    "salary": 12345,
                    "age": 21,
                    "title": "Mr",
                    "email": "email@mail.com"
                  }
                """);
    }

    @Test
    void getEmployeeById_404() {
        when(employeeService.getEmployee("id")).thenThrow(new EmployeeNotFoundException());

        ResponseEntity<String> result = restClient
                .get()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
        assertThatJson(result.getBody())
                .isEqualTo(
                        """
                        {
                          "timestamp": "${json-unit.any-string}",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Employee not found",
                          "path": "/api/v1/employee/id"
                        }
                        """);
    }

    @Test
    void getHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(12345);

        Integer result = restClient
                .get()
                .uri("/api/v1/employee/highestSalary")
                .retrieve()
                .body(Integer.class);

        assertThat(result).isEqualTo(12345);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(List.of("Richard", "John"));

        String result = restClient
                .get()
                .uri("/api/v1/employee/topTenHighestEarningEmployeeNames")
                .retrieve()
                .body(String.class);

        assertThatJson(result)
                .isEqualTo(
                        """
                [
                  "Richard",
                  "John"
                ]
                """);
    }

    @Test
    void createEmployee() {
        String request =
                """
                {
                    "name": "Richard Calderin",
                    "salary": 12007,
                    "age": 32,
                    "title": "Dr"
                }
                """;

        when(employeeService.createEmployee(new CreateEmployee("Richard Calderin", 12007, 32, "Dr")))
                .thenReturn(new Employee("id", "Richard Calderin", 12007, 32, "Dr", "email@email.com"));

        String result = restClient
                .post()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);

        assertThatJson(result)
                .isEqualTo(
                        """
                {
                  "id": "id",
                  "name": "Richard Calderin",
                  "salary": 12007,
                  "age": 32,
                  "title": "Dr",
                  "email": "email@email.com"
                }
                """);
    }

    @Test
    void createEmployee_missingItems() {
        String request =
                """
                {
                  "name": "",
                  "title": ""
                }
                """;

        ResponseEntity<String> result = restClient
                .post()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThatJson(result.getBody())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Validation failed for object='createEmployee'. Error count: 4",
                  "path": "/api/v1/employee",
                  "validation": [
                    {
                      "fieldName": "name",
                      "message": "Name must be provided"
                    },
                    {
                      "fieldName": "salary",
                      "message": "Salary must be provided"
                    },
                    {
                      "fieldName": "title",
                      "message": "Title must be provided"
                    },
                    {
                      "fieldName": "age",
                      "message": "Age must be provided"
                    }
                  ]
                }
                """);
    }

    @Test
    void createEmployee_salaryNegative() {
        String request =
                """
                {
                  "name": "Richard Calderin",
                    "salary": -12007,
                    "age": 32,
                    "title": "Dr"
                }
                """;

        ResponseEntity<String> result = restClient
                .post()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThatJson(result.getBody())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Validation failed for object='createEmployee'. Error count: 1",
                  "path": "/api/v1/employee",
                  "validation": [
                    {
                      "fieldName": "salary",
                      "message": "Salary must be positive"
                    }
                  ]
                }
                """);
    }

    @Test
    void createEmployee_ageBelowMin() {
        String request =
                """
                {
                  "name": "Richard Calderin",
                    "salary": 12007,
                    "age": 15,
                    "title": "Dr"
                }
                """;

        ResponseEntity<String> result = restClient
                .post()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThatJson(result.getBody())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Validation failed for object='createEmployee'. Error count: 1",
                  "path": "/api/v1/employee",
                  "validation": [
                    {
                      "fieldName": "age",
                      "message": "Minimum age is 16"
                    }
                  ]
                }
                """);
    }

    @Test
    void createEmployee_ageAboveMax() {
        String request =
                """
                {
                  "name": "Richard Calderin",
                    "salary": 12007,
                    "age": 76,
                    "title": "Dr"
                }
                """;

        ResponseEntity<String> result = restClient
                .post()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThatJson(result.getBody())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Validation failed for object='createEmployee'. Error count: 1",
                  "path": "/api/v1/employee",
                  "validation": [
                    {
                      "fieldName": "age",
                      "message": "Maximum age is 75"
                    }
                  ]
                }
                """);
    }

    @Test
    void deleteEmployeeById() {
        when(employeeService.deleteEmployeeById("id")).thenReturn("Richard Test");

        String result = restClient
                .delete()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(result).isEqualTo("Richard Test");
    }

    @Test
    void deleteEmployeeById_404() {
        when(employeeService.deleteEmployeeById("id")).thenThrow(new EmployeeNotFoundException());

        ResponseEntity<String> result = restClient
                .delete()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
        assertThatJson(result.getBody())
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Employee not found",
                  "path": "/api/v1/employee/id"
                }
                """);
    }

    @Test
    void deleteEmployeeById_MoreThanEmployeeWithSameName() {
        when(employeeService.deleteEmployeeById("id")).thenThrow(new CannotGuaranteeEmployeeDeletionException());

        ResponseEntity<String> result = restClient
                .delete()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
        assertThatJson(result.getBody())
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "Cannot guarantee the deletion of the employee",
                  "path": "/api/v1/employee/id"
                }
                """);
    }

    @Test
    void deleteEmployeeById_ServerSaidNotDeleted() {
        when(employeeService.deleteEmployeeById("id")).thenThrow(new EmployeeNotDeletedException());

        ResponseEntity<String> result = restClient
                .delete()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThatJson(result.getBody())
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Employee was not deleted, it might have been delete by another request",
                  "path": "/api/v1/employee/id"
                }
                """);
    }

    @Test
    void test429Propagation() {
        when(employeeService.deleteEmployeeById("id")).thenThrow(new TooManyRequestException());

        ResponseEntity<String> result = restClient
                .delete()
                .uri("/api/v1/employee/{id}", "id")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(429));
        assertThatJson(result.getBody())
                .isEqualTo(
                        """
                {
                  "timestamp": "${json-unit.any-string}",
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Too many requests, try again later",
                  "path": "/api/v1/employee/id"
                }
                """);
    }
}
