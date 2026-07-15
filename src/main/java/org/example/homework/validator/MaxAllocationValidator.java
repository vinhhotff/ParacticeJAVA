package org.example.homework.validator;

import lombok.RequiredArgsConstructor;
import org.example.homework.config.AllocationProperties;
import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.entity.Allocation;
import org.example.homework.exception.AllocationExceededException;
import org.example.homework.repository.AllocationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MaxAllocationValidator implements AllocationValidator {

    private final AllocationRepository allocationRepository;
    private final AllocationProperties allocationProperties;

    @Override
    public void validate(AllocationRequest request, Long excludeAllocationId) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Allocation> overlaps = allocationRepository.findOverlappingAllocations(
            request.getEmployeeId(), startDate, endDate, excludeAllocationId
        );

        // Gather all distinct dates that we need to test
        Set<LocalDate> datesToCheck = new HashSet<>();
        datesToCheck.add(startDate);
        if (endDate != null) {
            datesToCheck.add(endDate);
        }

        for (Allocation a : overlaps) {
            if (a.getStartDate().isAfter(startDate) && (endDate == null || !a.getStartDate().isAfter(endDate))) {
                datesToCheck.add(a.getStartDate());
            }
            if (a.getEndDate() != null && !a.getEndDate().isBefore(startDate) && (endDate == null || !a.getEndDate().isAfter(endDate))) {
                datesToCheck.add(a.getEndDate());
            }
        }

        int maxPercent = allocationProperties.getMaxPercent();

        // Check total percent on each date
        for (LocalDate date : datesToCheck) {
            int currentSum = 0;
            for (Allocation a : overlaps) {
                if (!date.isBefore(a.getStartDate()) && (a.getEndDate() == null || !date.isAfter(a.getEndDate()))) {
                    currentSum += a.getAllocationPercent();
                }
            }

            if (currentSum + request.getAllocationPercent() > maxPercent) {
                throw new AllocationExceededException(currentSum, request.getAllocationPercent());
            }
        }
    }
}
