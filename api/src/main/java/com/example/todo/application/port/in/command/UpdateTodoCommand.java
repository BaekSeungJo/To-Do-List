package com.example.todo.application.port.in.command;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTodoCommand(UUID id, String title, LocalDate dueDate) {
}
