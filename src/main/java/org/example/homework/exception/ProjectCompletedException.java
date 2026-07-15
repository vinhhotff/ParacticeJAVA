package org.example.homework.exception;

public class ProjectCompletedException extends BusinessException {
    public ProjectCompletedException(String projectCode) {
        super("Cannot allocate to completed project: " + projectCode);
    }
}
