package com.example.todo.application.port.in;

import com.example.todo.application.port.in.command.ToggleTodoCommand;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.common.auth.AuthenticatedUser;

public interface ToggleTodoUseCase {
    TodoResult toggle(ToggleTodoCommand command, AuthenticatedUser user);
}
