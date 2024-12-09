package com.reliaquest.api.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * We can only delete employees by name
 * however we can create multiple employees with the same name
 * this exception will be thrown if more than 1 employee exists
 * with the name at the time of deletion
 * <p>
 * Even if an employee is created after the check
 * Mock impl find first of the list which should still work
 * not sure this would work for real server
 *
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot guarantee the deletion of the employee")
public class CannotGuaranteeEmployeeDeletionException extends RuntimeException {}
