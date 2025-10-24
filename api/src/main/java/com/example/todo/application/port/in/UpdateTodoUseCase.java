package com.example.todo.application.port.in;

import com.example.todo.application.command.UpdateTodoCommand;
import com.example.todo.application.result.TodoResult;

public interface UpdateTodoUseCase {

    TodoResult update(UpdateTodoCommand command, AuthenticatedUser user);
}
