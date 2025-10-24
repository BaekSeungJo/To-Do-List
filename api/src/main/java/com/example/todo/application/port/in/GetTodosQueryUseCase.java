package com.example.todo.application.port.in;

import com.example.todo.application.query.TodoQuery;
import com.example.todo.application.result.TodoResult;
import java.util.List;

public interface GetTodosQueryUseCase {

    List<TodoResult> getTodos(TodoQuery query, AuthenticatedUser user);
}
