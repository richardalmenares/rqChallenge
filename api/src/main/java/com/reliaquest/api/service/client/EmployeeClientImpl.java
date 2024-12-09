package com.reliaquest.api.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.error.EmployeeErrorHandler;
import com.reliaquest.api.service.client.json.EmployeeJacksonNamingStrategy;
import com.reliaquest.api.service.client.model.DeleteEmployee;
import com.reliaquest.api.service.client.model.Response;
import java.net.http.HttpClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class EmployeeClientImpl implements EmployeeClient {

    private final RestClient restClient;

    public EmployeeClientImpl(
            EmployeeClientProperties employeeClientProperties,
            RestClient.Builder restClientBuilder,
            Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        JdkClientHttpRequestFactory jdkClientHttpRequestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build());
        jdkClientHttpRequestFactory.setReadTimeout(employeeClientProperties.readTimeout());
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder
                .createXmlMapper(false)
                .propertyNamingStrategy(new EmployeeJacksonNamingStrategy())
                .build();
        this.restClient = restClientBuilder
                .messageConverters(httpMessageConverters -> {
                    httpMessageConverters.removeIf(httpMessageConverter ->
                            httpMessageConverter instanceof MappingJackson2HttpMessageConverter);
                    httpMessageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                })
                .defaultStatusHandler(EmployeeErrorHandler.statusPredicate(), new EmployeeErrorHandler(objectMapper))
                .requestFactory(jdkClientHttpRequestFactory)
                .baseUrl(employeeClientProperties.baseUrl())
                .build();
    }

    @Override
    public Response<List<Employee>> getAllEmployees() {
        log.info("Getting all employees");
        return restClient
                .get()
                .uri("/api/v1/employee")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Response<Employee> getEmployee(String employeeId) {
        log.info("Getting employee with id: {}", employeeId);
        return restClient
                .get()
                .uri("/api/v1/employee/{employeeId}", employeeId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Response<Employee> createEmployee(CreateEmployee createEmployee) {
        log.info("Creating employee");
        return restClient
                .post()
                .uri("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(createEmployee)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Response<Boolean> deleteEmployee(String employeeName) {
        log.info("Deleting employee");
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(new DeleteEmployee(employeeName))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
