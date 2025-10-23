package com.example.todo.application.port.in.command;

import java.time.LocalDate;

public record CreateTodoCommand(String title, LocalDate dueDate) {
}
