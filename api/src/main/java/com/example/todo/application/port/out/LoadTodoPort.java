package com.example.todo.application.port.out;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.UserId;
import java.util.Optional;

public interface LoadTodoPort {

    Optional<Todo> loadByIdAndUser(TodoId todoId, UserId userId);
}
