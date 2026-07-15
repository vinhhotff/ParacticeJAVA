package org.example.homework.exception;

public class ProjectNotFoundException extends ResourceNotFoundException {
    public ProjectNotFoundException(Long id) {
        super("Project", id);
    }
}
