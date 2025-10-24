package com.example.todo.application.port.in;

import com.example.todo.application.command.CreateTodoCommand;
import com.example.todo.application.result.TodoResult;

public interface CreateTodoUseCase {

    TodoResult create(CreateTodoCommand command, AuthenticatedUser user);
}
