package org.example.homework.exception;

public class EmployeeNotFoundException extends ResourceNotFoundException {
    public EmployeeNotFoundException(Long id) {
        super("Employee", id);
    }
}
