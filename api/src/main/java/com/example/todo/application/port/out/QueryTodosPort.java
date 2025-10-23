package com.example.todo.application.port.out;

import com.example.todo.domain.model.Todo;
import java.util.List;

public interface QueryTodosPort {
    List<Todo> findTodos(String userId);
}
