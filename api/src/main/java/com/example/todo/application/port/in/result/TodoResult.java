package com.example.todo.application.port.in.result;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TodoResult(
        UUID id,
        String title,
        LocalDate dueDate,
        boolean done,
        Instant createdAt,
        Instant updatedAt
) {
}
