package org.example.homework.validator;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.exception.InvalidDateRangeException;
import org.springframework.stereotype.Component;

@Component
public class AllocationDateRangeValidator implements AllocationValidator {

    @Override
    public void validate(AllocationRequest request, Long excludeAllocationId) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidDateRangeException("Allocation end date cannot be before start date");
        }
    }
}
