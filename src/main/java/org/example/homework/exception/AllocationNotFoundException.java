package org.example.homework.exception;

public class AllocationNotFoundException extends ResourceNotFoundException {
    public AllocationNotFoundException(Long id) {
        super("Allocation", id);
    }
}
