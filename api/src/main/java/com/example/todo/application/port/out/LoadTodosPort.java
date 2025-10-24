package com.example.todo.application.port.out;

import com.example.todo.application.query.TodoQuery;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.UserId;
import java.util.List;

public interface LoadTodosPort {

    List<Todo> loadTodos(UserId userId, TodoQuery.Filter filter);
}
