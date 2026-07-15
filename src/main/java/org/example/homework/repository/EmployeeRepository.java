package org.example.homework.repository;

import org.example.homework.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByRoleContainingIgnoreCase(String role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :id")
    Optional<Employee> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT e.employeeId, e.employeeCode, e.fullName, COALESCE(SUM(a.allocationPercent), 0) " +
           "FROM Employee e LEFT JOIN e.allocations a " +
           "GROUP BY e.employeeId, e.employeeCode, e.fullName")
    List<Object[]> getEmployeeAllocationSums();
}
