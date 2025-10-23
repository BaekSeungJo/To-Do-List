package com.example.todo.application.port.in.query;

import com.example.todo.application.port.in.query.criteria.TodoStatus;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.common.auth.AuthenticatedUser;
import java.util.List;

public interface GetTodosQuery {
    List<TodoResult> getTodos(TodoStatus status, AuthenticatedUser user);
}
