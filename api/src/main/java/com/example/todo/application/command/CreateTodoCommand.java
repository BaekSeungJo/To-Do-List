package com.example.todo.application.command;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Command describing a request to create a new todo.
 */
public record CreateTodoCommand(String title, Optional<LocalDate> dueDate) {

    public CreateTodoCommand {
        dueDate = dueDate == null ? Optional.empty() : dueDate;
    }

    public static CreateTodoCommand of(String title, LocalDate dueDate) {
        return new CreateTodoCommand(title, Optional.ofNullable(dueDate));
    }
}
