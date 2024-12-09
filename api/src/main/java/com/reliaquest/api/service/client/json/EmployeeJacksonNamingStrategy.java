package com.reliaquest.api.service.client.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.reliaquest.api.model.Employee;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmployeeJacksonNamingStrategy extends PropertyNamingStrategies.NamingBase {

    private static final String PREFIX = "employee_";
    private static final Set<String> SUPPORTED_FIELDS = getSupportedFields();

    @Override
    public String translate(String propertyName) {
        if (propertyName == null) {
            return null;
        }

        if (SUPPORTED_FIELDS.contains(propertyName)) {
            String result = PREFIX + propertyName;
            log.debug("Translated {} to {}", propertyName, result);
            return result;
        }

        return propertyName;
    }

    private static Set<String> getSupportedFields() {
        Field[] fields = Employee.class.getDeclaredFields();
        return Arrays.stream(fields)
                .map(Field::getName)
                .filter(name -> !name.equals("id"))
                .collect(Collectors.toSet());
    }
}
