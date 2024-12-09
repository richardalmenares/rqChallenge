package com.reliaquest.api.service.client.model;

public record Response<T>(T data, String status, String error) {}
