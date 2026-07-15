package org.example.homework.exception;

public abstract class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }
}
