package org.example.homework.service;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;
import java.util.List;

public interface AllocationService {
    List<AllocationResponse> getAll();
    AllocationResponse getById(Long id);
    AllocationResponse createAllocation(AllocationRequest request);
    AllocationResponse updateAllocation(Long id, AllocationRequest request);
    void removeAllocation(Long id);
}
