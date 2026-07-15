package org.example.homework.service;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;

public interface AllocationService {
    AllocationResponse createAllocation(AllocationRequest request);
    AllocationResponse updateAllocation(Long id, AllocationRequest request);
    void removeAllocation(Long id);
}
