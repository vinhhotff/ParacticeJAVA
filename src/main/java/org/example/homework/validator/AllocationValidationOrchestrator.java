package org.example.homework.validator;

import lombok.RequiredArgsConstructor;
import org.example.homework.dto.request.AllocationRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationValidationOrchestrator {

    private final List<AllocationValidator> validators;

    public void validate(AllocationRequest request, Long excludeAllocationId) {
        validators.forEach(v -> v.validate(request, excludeAllocationId));
    }
}
