package com.reliaquest.api.service;

import com.reliaquest.api.controller.exceptions.CannotGuaranteeEmployeeDeletionException;
import com.reliaquest.api.controller.exceptions.EmployeeNotDeletedException;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.EmployeeClient;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeClient employeeClient;

    @Override
    public List<Employee> getAllEmployees() {
        log.info("Getting all employees");
        return employeeClient.getAllEmployees().data();
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        // GDPR not including customer data in logs
        log.info("Searching for employees");
        return getAllEmployees().stream()
                .filter(employee -> StringUtils.containsAnyIgnoreCase(employee.name(), searchString))
                .toList();
    }

    @Override
    public Employee getEmployee(String employeeId) {
        log.info("Getting employee: {}", employeeId);
        return employeeClient.getEmployee(employeeId).data();
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        log.info("Getting highest salary");
        return getAllEmployees().stream()
                .map(Employee::salary)
                .max(Comparator.comparingInt(salary -> salary))
                // default lowest salary
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Getting top 10 highest salary names");
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .limit(10)
                .map(Employee::name)
                .toList();
    }

    @Override
    public Employee createEmployee(CreateEmployee employeeInput) {
        log.info("Creating employee");
        return employeeClient.createEmployee(employeeInput).data();
    }

    @Override
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee: {}", id);
        // will return 404 to client if not found
        Employee employee = getEmployee(id);
        String name = employee.name();
        long numberOfEmployees = getAllEmployees().stream()
                .filter(employeeEntry -> employeeEntry.name().equalsIgnoreCase(name))
                .count();
        log.info("Found: {} employees with the same name", numberOfEmployees);

        if (numberOfEmployees > 1) {
            // there is no lock which means any other thread can
            // create new employee with same name
            // the lock must be cluster compatible not just thread
            // we can have multiple instances of VMs
            // having said that current impl in the mock
            // is streaming over list, stream will maintain order of insertion
            // real server might not do the same
            throw new CannotGuaranteeEmployeeDeletionException();
        }

        Boolean deleted = employeeClient.deleteEmployee(name).data();
        if (deleted == null || !deleted) {
            throw new EmployeeNotDeletedException();
        }

        return name;
    }
}
