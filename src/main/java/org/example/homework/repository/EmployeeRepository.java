package org.example.homework.repository;

import org.example.homework.entity.Employee;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByRoleContainingIgnoreCase(String role);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"skills"})
    List<Employee> findAll();

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"skills"})
    org.springframework.data.domain.Page<Employee> findAll(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT DISTINCT e FROM Employee e JOIN e.skills s WHERE LOWER(s.name) = LOWER(:skillName)")
    List<Employee> findBySkillName(@Param("skillName") String skillName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :id")
    Optional<Employee> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT e.employeeId, e.employeeCode, e.fullName, COALESCE(SUM(CASE WHEN a.status = 'ACTIVE' THEN a.allocationPercent ELSE 0 END), 0) " +
           "FROM Employee e LEFT JOIN e.allocations a " +
           "GROUP BY e.employeeId, e.employeeCode, e.fullName")
    List<Object[]> getEmployeeAllocationSums();

    @Query(value = """
        SELECT e.* FROM employee e
        WHERE e.role_embedding IS NOT NULL
        ORDER BY e.role_embedding <=> cast(:queryVector as vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Employee> findByRoleSimilarity(@Param("queryVector") String queryVector, @Param("limit") int limit);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE employee SET role_embedding = cast(:embedding as vector) WHERE employee_id = :id", nativeQuery = true)
    void updateRoleEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

    @Query(value = "SELECT employee_id, role FROM employee WHERE role_embedding IS NULL", nativeQuery = true)
    List<Object[]> findEmployeesWithoutEmbedding();
}
