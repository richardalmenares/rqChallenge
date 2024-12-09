package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * Using JsonProperty to bypass inconsistencies
 * with the request and response object of the server
 * we are integrating to
 */
public record CreateEmployee(
        @JsonProperty("name") @NotBlank(message = "Name must be provided") String name,
        @JsonProperty("salary")
                @Positive(message = "Salary must be positive") @NotNull(message = "Salary must be provided") Integer salary,
        @JsonProperty("age")
                @Min(value = 16, message = "Minimum age is 16")
                @Max(value = 75, message = "Maximum age is 75")
                @NotNull(message = "Age must be provided") Integer age,
        @JsonProperty("title") @NotBlank(message = "Title must be provided") String title) {}
