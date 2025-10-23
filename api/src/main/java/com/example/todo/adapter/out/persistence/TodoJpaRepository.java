package com.example.todo.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoJpaRepository extends JpaRepository<TodoEntity, UUID> {
    Optional<TodoEntity> findByIdAndUserId(UUID id, String userId);
    List<TodoEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
