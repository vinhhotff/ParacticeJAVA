package org.example.homework.repository;

import org.example.homework.entity.Project;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectCode(String projectCode);
    Optional<Project> findByProjectCode(String projectCode);

    @Query(value = """
        SELECT p.* FROM project p
        WHERE p.description_embedding IS NOT NULL
        ORDER BY p.description_embedding <=> cast(:queryVector as vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Project> findByDescriptionSimilarity(@Param("queryVector") String queryVector, @Param("limit") int limit);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE project SET description_embedding = cast(:embedding as vector) WHERE project_id = :id", nativeQuery = true)
    void updateDescriptionEmbedding(@Param("id") Long id, @Param("embedding") String embedding);
}
