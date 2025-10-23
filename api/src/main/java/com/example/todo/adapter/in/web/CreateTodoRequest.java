package com.example.todo.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateTodoRequest(
        @NotBlank
        @Size(max = 100)
        String title,
        LocalDate dueDate
) {
}
