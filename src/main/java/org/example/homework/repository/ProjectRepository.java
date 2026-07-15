package org.example.homework.repository;

import org.example.homework.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectCode(String projectCode);
    Optional<Project> findByProjectCode(String projectCode);
}
