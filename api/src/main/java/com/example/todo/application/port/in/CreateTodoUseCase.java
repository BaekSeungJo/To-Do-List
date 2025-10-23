package com.example.todo.application.port.in;

import com.example.todo.application.port.in.command.CreateTodoCommand;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.common.auth.AuthenticatedUser;

public interface CreateTodoUseCase {
    TodoResult create(CreateTodoCommand command, AuthenticatedUser user);
}
