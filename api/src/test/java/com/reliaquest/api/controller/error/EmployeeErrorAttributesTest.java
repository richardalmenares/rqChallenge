package com.reliaquest.api.controller.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
class EmployeeErrorAttributesTest {

    @Mock
    WebRequest webRequest;

    @Mock
    BindException bindException;

    ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.MESSAGE,
            ErrorAttributeOptions.Include.STATUS,
            ErrorAttributeOptions.Include.ERROR);
    EmployeeErrorAttributes employeeErrorAttributes = new EmployeeErrorAttributes();

    @Test
    void getErrorAttributes_bindException() {
        List<EmployeeErrorAttributes.ValidationError> expected = List.of(
                new EmployeeErrorAttributes.ValidationError("createEmployee", "Some Error Occurred"),
                new EmployeeErrorAttributes.ValidationError("age", "Min age is 16"));

        when(webRequest.getAttribute("jakarta.servlet.error.exception", 0)).thenReturn(bindException);
        when(webRequest.getAttribute("jakarta.servlet.error.status_code", 0)).thenReturn(400);
        when(webRequest.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR", 0))
                .thenReturn(null);
        when(bindException.getObjectName()).thenReturn("createEmployee");
        when(bindException.getAllErrors())
                .thenReturn(List.of(
                        new ObjectError("createEmployee", "Some Error Occurred"),
                        new FieldError("createEmployee", "age", "Min age is 16")));

        Map<String, Object> result = employeeErrorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
        assertThat(result)
                .containsKey("timestamp")
                .containsEntry("status", 400)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "Validation failed for object='createEmployee'. Error count: 2")
                .containsEntry("validation", expected)
                .hasSize(5);
    }

    @Test
    void getErrorAttributes_otherException() {
        when(webRequest.getAttribute("jakarta.servlet.error.exception", 0)).thenReturn(new RuntimeException("test"));
        when(webRequest.getAttribute("jakarta.servlet.error.status_code", 0)).thenReturn(500);
        when(webRequest.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR", 0))
                .thenReturn(null);

        Map<String, Object> result = employeeErrorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
        assertThat(result)
                .containsKey("timestamp")
                .containsEntry("status", 500)
                .containsEntry("error", "Internal Server Error")
                .containsEntry("message", "test")
                .hasSize(4);
    }
}
