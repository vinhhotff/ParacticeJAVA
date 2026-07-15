package org.example.homework.validator;

import org.example.homework.dto.request.AllocationRequest;

@FunctionalInterface
public interface AllocationValidator {
    void validate(AllocationRequest request, Long excludeAllocationId);
}
