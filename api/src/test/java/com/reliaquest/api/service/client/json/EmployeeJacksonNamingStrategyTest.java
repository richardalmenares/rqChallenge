package com.reliaquest.api.service.client.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmployeeJacksonNamingStrategyTest {

    EmployeeJacksonNamingStrategy employeeJacksonNamingStrategy = new EmployeeJacksonNamingStrategy();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "id")
    void translate_null_empty_id(String propertyName) {
        String actual = employeeJacksonNamingStrategy.translate(propertyName);

        assertThat(actual).isEqualTo(propertyName);
    }

    @Test
    void translate_other() {
        String actual = employeeJacksonNamingStrategy.translate("age");

        assertThat(actual).isEqualTo("employee_age");
    }
}
