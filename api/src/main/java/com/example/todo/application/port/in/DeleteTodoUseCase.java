package com.example.todo.application.port.in;

import com.example.todo.application.command.DeleteTodoCommand;

public interface DeleteTodoUseCase {

    void delete(DeleteTodoCommand command, AuthenticatedUser user);
}
