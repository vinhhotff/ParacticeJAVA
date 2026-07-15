package org.example.homework.exception;

public class AllocationExceededException extends BusinessException {
    public AllocationExceededException(int current, int requested) {
        super("Employee allocation exceeds 100%%. Current: %d%%, Requested: %d%%"
            .formatted(current, requested));
    }
}
