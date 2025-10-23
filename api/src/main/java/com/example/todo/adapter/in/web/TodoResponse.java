package com.example.todo.adapter.in.web;

import com.example.todo.application.port.in.result.TodoResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TodoResponse(
        UUID id,
        String title,
        LocalDate dueDate,
        boolean done,
        Instant createdAt,
        Instant updatedAt
) {
    public static TodoResponse from(TodoResult result) {
        return new TodoResponse(
                result.id(),
                result.title(),
                result.dueDate(),
                result.done(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
