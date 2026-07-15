package org.example.homework.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.entity.Employee;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.embedding.EmbeddingServiceClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingInitializer {

    private final EmployeeRepository employeeRepository;
    private final EmbeddingServiceClient embeddingServiceClient;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeEmbeddings() {
        log.info("Checking for employees without role embeddings...");
        try {
            List<Object[]> uninitialized = employeeRepository.findEmployeesWithoutEmbedding();
            int count = 0;
            for (Object[] row : uninitialized) {
                Long id = ((Number) row[0]).longValue();
                String role = (String) row[1];
                if (role != null && !role.trim().isEmpty()) {
                    log.info("Generating role embedding for employee ID {}: ({})", id, role);
                    float[] embedding = embeddingServiceClient.generateEmbedding(role);
                    employeeRepository.updateRoleEmbedding(id, java.util.Arrays.toString(embedding));
                    count++;
                }
            }
            if (count > 0) {
                log.info("Successfully initialized {} employee embeddings.", count);
            } else {
                log.info("All employee embeddings are already initialized.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize embeddings on startup: {}", e.getMessage());
        }
    }
}
