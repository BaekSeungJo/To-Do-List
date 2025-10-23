package com.example.todo.application.port.in.command;

import java.util.UUID;

public record ToggleTodoCommand(UUID id) {
}
