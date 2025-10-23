package com.example.todo.application.port.in;

import com.example.todo.application.port.in.command.DeleteTodoCommand;
import com.example.todo.common.auth.AuthenticatedUser;

public interface DeleteTodoUseCase {
    void delete(DeleteTodoCommand command, AuthenticatedUser user);
}
