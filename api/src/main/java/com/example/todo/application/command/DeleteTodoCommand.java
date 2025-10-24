package com.example.todo.application.command;

import java.util.Objects;
import java.util.UUID;

/**
 * Command representing a delete request for a todo.
 */
public record DeleteTodoCommand(UUID todoId) {

    public DeleteTodoCommand {
        Objects.requireNonNull(todoId, "todoId");
    }
}
