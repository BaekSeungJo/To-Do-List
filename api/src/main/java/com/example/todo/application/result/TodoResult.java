package com.example.todo.application.result;

import com.example.todo.domain.model.Todo;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record TodoResult(
    UUID id,
    String title,
    Optional<LocalDate> dueDate,
    boolean done,
    Instant createdAt,
    Instant updatedAt
) {

    public static TodoResult from(Todo todo) {
        return new TodoResult(
            todo.id().value(),
            todo.title().value(),
            todo.dueDate().value(),
            todo.done(),
            todo.createdAt(),
            todo.updatedAt()
        );
    }
}
