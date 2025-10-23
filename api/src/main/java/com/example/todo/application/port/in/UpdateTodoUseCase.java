package com.example.todo.application.port.in;

import com.example.todo.application.port.in.command.UpdateTodoCommand;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.common.auth.AuthenticatedUser;

public interface UpdateTodoUseCase {
    TodoResult update(UpdateTodoCommand command, AuthenticatedUser user);
}
