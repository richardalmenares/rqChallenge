package com.reliaquest.api.service.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "employee-client")
public record EmployeeClientProperties(Duration readTimeout, String baseUrl) {}
