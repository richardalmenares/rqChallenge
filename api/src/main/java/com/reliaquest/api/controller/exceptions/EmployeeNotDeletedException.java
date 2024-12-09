package com.reliaquest.api.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        code = HttpStatus.BAD_REQUEST,
        reason = "Employee was not deleted, it might have been delete by another request")
public class EmployeeNotDeletedException extends RuntimeException {}
