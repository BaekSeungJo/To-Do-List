package com.example.todo.application.port.out;

import com.example.todo.domain.model.Todo;

public interface SaveTodoPort {
    Todo save(Todo todo);
}
