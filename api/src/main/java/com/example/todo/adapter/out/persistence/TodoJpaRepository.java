package com.example.todo.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TodoJpaRepository extends JpaRepository<TodoJpaEntity, UUID> {

    List<TodoJpaEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<TodoJpaEntity> findAllByUserIdAndDoneOrderByCreatedAtDesc(String userId, boolean done);

    Optional<TodoJpaEntity> findByIdAndUserId(UUID id, String userId);

    long deleteByIdAndUserId(UUID id, String userId);
}
