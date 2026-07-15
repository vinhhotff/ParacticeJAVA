package org.example.homework.repository;

import org.example.homework.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    @Query("SELECT COALESCE(SUM(a.allocationPercent), 0) FROM Allocation a WHERE a.employee.employeeId = :employeeId")
    int sumAllocationByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT COALESCE(SUM(a.allocationPercent), 0) FROM Allocation a WHERE a.employee.employeeId = :employeeId AND a.allocationId != :excludeAllocationId")
    int sumAllocationByEmployeeIdExcluding(@Param("employeeId") Long employeeId, @Param("excludeAllocationId") Long excludeAllocationId);

    @Query("SELECT a FROM Allocation a WHERE a.employee.employeeId = :employeeId " +
           "AND (:excludeAllocationId IS NULL OR a.allocationId != :excludeAllocationId) " +
           "AND (a.startDate <= :endDate OR :endDate IS NULL) " +
           "AND (a.endDate >= :startDate OR a.endDate IS NULL)")
    List<Allocation> findOverlappingAllocations(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeAllocationId") Long excludeAllocationId
    );
}
