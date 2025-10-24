package com.example.todo.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TodoResponse(
    UUID id,
    String title,
    Optional<LocalDate> dueDate,
    boolean done,
    Instant createdAt,
    Instant updatedAt
) {
}
