package com.reliaquest.api.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.reliaquest.api.controller.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.controller.exceptions.TooManyRequestException;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.model.Response;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integ")
@AutoConfigureWireMock(port = 0)
class EmployeeClientImplIntegrationTest {

    @Autowired
    EmployeeClientImpl employeeClient;

    @Test
    void getAllEmployees() {
        String response =
                """
                {
                    "data": [
                        {
                            "id": "fa73472f-6a51-42fd-9fd8-80bc07232075",
                            "employee_name": "Martin Okuneva",
                            "employee_salary": 366039,
                            "employee_age": 28,
                            "employee_title": "Investor Retail Technician",
                            "employee_email": "lotstring@company.com"
                        }
                    ],
                    "status": "Successfully processed request."
                }
                """;
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/employee"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(200)
                        .withBody(response)
                        .withHeader("Content-Type", "application/json")));

        Response<List<Employee>> allEmployees = employeeClient.getAllEmployees();

        assertThat(allEmployees.data())
                .isEqualTo(List.of(new Employee(
                        "fa73472f-6a51-42fd-9fd8-80bc07232075",
                        "Martin Okuneva",
                        366039,
                        28,
                        "Investor Retail Technician",
                        "lotstring@company.com")));
        assertThat(allEmployees.status()).isEqualTo("Successfully processed request.");
        assertThat(allEmployees.error()).isNull();
    }

    @Test
    void getEmployee() {
        String response =
                """
                {
                    "data": {
                            "id": "fa73472f-6a51-42fd-9fd8-80bc07232075",
                            "employee_name": "Martin Okuneva",
                            "employee_salary": 366039,
                            "employee_age": 28,
                            "employee_title": "Investor Retail Technician",
                            "employee_email": "lotstring@company.com"
                        },
                    "status": "Successfully processed request."
                }
                """;

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/employee/fa73472f-6a51-42fd-9fd8-80bc07232075"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(200)
                        .withBody(response)
                        .withHeader("Content-Type", "application/json")));

        Response<Employee> employee = employeeClient.getEmployee("fa73472f-6a51-42fd-9fd8-80bc07232075");

        assertThat(employee.data())
                .isEqualTo(new Employee(
                        "fa73472f-6a51-42fd-9fd8-80bc07232075",
                        "Martin Okuneva",
                        366039,
                        28,
                        "Investor Retail Technician",
                        "lotstring@company.com"));
        assertThat(employee.status()).isEqualTo("Successfully processed request.");
        assertThat(employee.error()).isNull();
    }

    @Test
    void getEmployee_404() {
        String response =
                """
                {
                    "status": "Successfully processed request."
                }
                """;

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/employee/fa73472f-6a51-42fd-9fd8-80bc07232075"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(404)
                        .withBody(response)
                        .withHeader("Content-Type", "application/json")));

        assertThatThrownBy(() -> employeeClient.getEmployee("fa73472f-6a51-42fd-9fd8-80bc07232075"))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void getEmployee_404_proxy() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/employee/fa73472f-6a51-42fd-9fd8-80bc07232075"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(404)
                        .withBody("{}")
                        .withHeader("Content-Type", "application/json")));

        assertThatThrownBy(() -> employeeClient.getEmployee("fa73472f-6a51-42fd-9fd8-80bc07232075"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle http code: 404");
    }

    @Test
    void createEmployee() {
        String response =
                """
                {
                    "data": {
                            "id": "fa73472f-6a51-42fd-9fd8-80bc07232075",
                            "employee_name": "Martin Okuneva",
                            "employee_salary": 366039,
                            "employee_age": 28,
                            "employee_title": "Investor Retail Technician",
                            "employee_email": "lotstring@company.com"
                        },
                    "status": "Successfully processed request."
                }
                """;
        CreateEmployee createEmployee = new CreateEmployee("Martin Okuneva", 366039, 28, "Investor Retail Technician");

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(new EqualToJsonPattern(
                        """
                                        {
                                            "name": "Martin Okuneva",
                                            "salary": 366039,
                                            "age": 28,
                                            "title": "Investor Retail Technician"
                                        }
                                        """,
                        true,
                        false))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(200)
                        .withResponseBody(Body.fromJsonBytes(response.getBytes(StandardCharsets.UTF_8)))
                        .withHeader("Content-Type", "application/json")));

        Response<Employee> employee = employeeClient.createEmployee(createEmployee);

        assertThat(employee.data())
                .isEqualTo(new Employee(
                        "fa73472f-6a51-42fd-9fd8-80bc07232075",
                        "Martin Okuneva",
                        366039,
                        28,
                        "Investor Retail Technician",
                        "lotstring@company.com"));
        assertThat(employee.status()).isEqualTo("Successfully processed request.");
        assertThat(employee.error()).isNull();
    }

    @Test
    void deleteEmployee() {
        String response =
                """
                {
                    "data": true,
                    "status": "Successfully processed request."
                }
                """;

        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(new EqualToJsonPattern(
                        """
                                        {
                                            "name": "Richard Test"
                                        }
                                        """,
                        true,
                        false))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(200)
                        .withResponseBody(Body.fromJsonBytes(response.getBytes(StandardCharsets.UTF_8)))
                        .withHeader("Content-Type", "application/json")));

        Response<Boolean> result = employeeClient.deleteEmployee("Richard Test");

        assertThat(result.data()).isTrue();
        assertThat(result.status()).isEqualTo("Successfully processed request.");
        assertThat(result.error()).isNull();
    }

    @Test
    void test429Handling() {
        WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(new EqualToJsonPattern(
                        """
                                        {
                                            "name": "Richard Test"
                                        }
                                        """,
                        true,
                        false))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(429)));

        assertThatThrownBy(() -> employeeClient.deleteEmployee("Richard Test"))
                .isInstanceOf(TooManyRequestException.class);
    }
}
