package com.example.todo.application.port.in.command;

import java.util.UUID;

public record DeleteTodoCommand(UUID id) {
}
