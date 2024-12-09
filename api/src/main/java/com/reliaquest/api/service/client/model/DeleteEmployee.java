package com.reliaquest.api.service.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteEmployee(@JsonProperty("name") String name) {}
