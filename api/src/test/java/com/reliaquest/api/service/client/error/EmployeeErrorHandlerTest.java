package com.reliaquest.api.service.client.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.exceptions.TooManyRequestException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class EmployeeErrorHandlerTest {

    @Mock
    HttpRequest request;

    @Mock
    ClientHttpResponse response;

    ObjectMapper objectMapper = new ObjectMapper();
    EmployeeErrorHandler employeeErrorHandler = new EmployeeErrorHandler(objectMapper);

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class)
    void statusPredicate(HttpStatus httpStatus) {
        boolean expected = httpStatus == HttpStatus.TOO_MANY_REQUESTS || httpStatus == HttpStatus.NOT_FOUND;

        boolean actual = EmployeeErrorHandler.statusPredicate().test(httpStatus);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void handle_429() throws Exception {
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(429));

        assertThatThrownBy(() -> employeeErrorHandler.handle(request, response))
                .isInstanceOf(TooManyRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"</html>", "{}"})
    void handle_404_proxyLevel(String responseString) throws Exception {
        byte[] proxyResponse = responseString.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream is = new ByteArrayInputStream(proxyResponse);

        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(404));
        when(response.getBody()).thenReturn(is);

        boolean isJson = responseString.startsWith("{");
        Class<? extends Throwable> expectedThrowable = isJson ? IllegalStateException.class : JsonParseException.class;
        String msg = isJson ? "Cannot handle http code: 404" : "Unexpected character ('<'";
        assertThatThrownBy(() -> employeeErrorHandler.handle(request, response))
                .isInstanceOf(expectedThrowable)
                .hasMessageContaining(msg);
    }
}
