package com.reliaquest.api.controller.error;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

/**
 * Add validation errors to standard
 * spring boot error attributes object
 * default "errors" attribute contains too much data
 */
@Component
@Slf4j
public class EmployeeErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

        Throwable error = getError(webRequest);
        if (error instanceof BindingResult bindingResult) {
            List<ValidationError> validationErrors = bindingResult.getAllErrors().stream()
                    .map(objectError -> new ValidationError(
                            objectError instanceof FieldError fieldError
                                    ? fieldError.getField()
                                    : objectError.getObjectName(),
                            objectError.getDefaultMessage()))
                    .toList();
            errorAttributes.put("validation", validationErrors);
            log.info("Adding validation errors to error response: [{}]", validationErrors);
        }

        return errorAttributes;
    }

    record ValidationError(String fieldName, String message) {}
}
