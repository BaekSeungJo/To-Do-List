package com.example.todo.application.port.out;

import com.example.todo.domain.model.Todo;
import java.util.Optional;
import java.util.UUID;

public interface LoadTodoPort {
    Optional<Todo> loadByIdAndUser(UUID id, String userId);
}
