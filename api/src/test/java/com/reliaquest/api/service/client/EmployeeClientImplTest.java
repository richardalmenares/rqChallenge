package com.reliaquest.api.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.error.EmployeeErrorHandler;
import com.reliaquest.api.service.client.json.EmployeeJacksonNamingStrategy;
import com.reliaquest.api.service.client.model.DeleteEmployee;
import com.reliaquest.api.service.client.model.Response;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class EmployeeClientImplTest {

    @Mock
    RestClient.Builder restClientBuilder;

    @Mock
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    RestClient restClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Captor
    ArgumentCaptor<ClientHttpRequestFactory> clientHttpRequestFactoryCaptor;

    @Captor
    ArgumentCaptor<Consumer<List<HttpMessageConverter<?>>>> configurerCaptor;

    EmployeeClientImpl employeeClient;

    @BeforeEach
    public void setUp() {
        when(jackson2ObjectMapperBuilder.createXmlMapper(false)).thenReturn(jackson2ObjectMapperBuilder);
        when(jackson2ObjectMapperBuilder.propertyNamingStrategy(any(EmployeeJacksonNamingStrategy.class)))
                .thenReturn(jackson2ObjectMapperBuilder);
        when(jackson2ObjectMapperBuilder.build()).thenReturn(objectMapper);
        when(restClientBuilder.messageConverters(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultStatusHandler(any(), any(EmployeeErrorHandler.class)))
                .thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl("http://localhost:8080")).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        EmployeeClientProperties employeeClientProperties =
                new EmployeeClientProperties(Duration.ofSeconds(10), "http://localhost:8080");
        employeeClient =
                new EmployeeClientImpl(employeeClientProperties, restClientBuilder, jackson2ObjectMapperBuilder);
    }

    @AfterEach
    void finalise() {
        verify(restClientBuilder).requestFactory(clientHttpRequestFactoryCaptor.capture());
        verify(restClientBuilder).messageConverters(configurerCaptor.capture());

        JdkClientHttpRequestFactory jdkClientHttpRequestFactory =
                (JdkClientHttpRequestFactory) clientHttpRequestFactoryCaptor.getValue();
        assertThat(ReflectionTestUtils.getField(jdkClientHttpRequestFactory, "readTimeout"))
                .isEqualTo(Duration.ofSeconds(10));

        Consumer<List<HttpMessageConverter<?>>> configurer = configurerCaptor.getValue();
        List<HttpMessageConverter<?>> converterList = new ArrayList<>();
        converterList.add(new MappingJackson2HttpMessageConverter());
        converterList.add(new MappingJackson2HttpMessageConverter());
        configurer.accept(converterList);
        assertThat(converterList).hasSize(1);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
                (MappingJackson2HttpMessageConverter) converterList.get(0);
        assertThat(mappingJackson2HttpMessageConverter.getObjectMapper()).isEqualTo(objectMapper);
    }

    @Test
    void getAllEmployees_success() {
        Response<List<Employee>> expected = new Response<>(
                List.of(new Employee("id", "name", 190, 20, "Mr", "email@email.com")),
                "Successfully processed request.",
                null);

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/v1/employee");
        doReturn(requestHeadersSpec).when(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        Response<List<Employee>> actual = employeeClient.getAllEmployees();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getAllEmployees_failure() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/v1/employee");
        doReturn(requestHeadersSpec).when(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeClient.getAllEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void getEmployee() {
        Response<Employee> expected = new Response<>(
                new Employee("employeeId", "name", 190, 20, "Mr", "email@email.com"),
                "Successfully processed request.",
                null);

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/v1/employee/{employeeId}", "employeeId");
        doReturn(requestHeadersSpec).when(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        Response<Employee> actual = employeeClient.getEmployee("employeeId");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getEmployee_failure() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/api/v1/employee/{employeeId}", "employeeId");
        doReturn(requestHeadersSpec).when(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeClient.getEmployee("employeeId"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void createEmployee() {
        CreateEmployee request = new CreateEmployee("name", 12345, 18, "Mr");
        Response<Employee> expected = new Response<>(
                new Employee("employeeId", "name", 12345, 18, "Mr", "email@email.com"),
                "Successfully processed request.",
                null);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/v1/employee")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(request)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        Response<Employee> actual = employeeClient.createEmployee(request);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createEmployee_failure() {
        CreateEmployee request = new CreateEmployee("name", 12345, 18, "Mr");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/v1/employee")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(request)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeClient.createEmployee(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    void deleteEmployee() {
        Response<Boolean> expected = new Response<>(true, "Successfully processed request.", null);

        when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/v1/employee")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(new DeleteEmployee("Richard Test"))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        Response<Boolean> actual = employeeClient.deleteEmployee("Richard Test");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void deleteEmployee_failure() {
        when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/v1/employee")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(new DeleteEmployee("Richard Test"))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> employeeClient.deleteEmployee("Richard Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }
}
