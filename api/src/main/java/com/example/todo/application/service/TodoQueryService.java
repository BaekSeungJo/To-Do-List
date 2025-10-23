package com.example.todo.application.service;

import com.example.todo.application.port.in.query.GetTodosQuery;
import com.example.todo.application.port.in.query.criteria.TodoStatus;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.application.port.out.QueryTodosPort;
import com.example.todo.common.auth.AuthenticatedUser;
import com.example.todo.domain.model.Todo;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoQueryService implements GetTodosQuery {

    private final QueryTodosPort queryTodosPort;

    public TodoQueryService(QueryTodosPort queryTodosPort) {
        this.queryTodosPort = queryTodosPort;
    }

    @Override
    public List<TodoResult> getTodos(TodoStatus status, AuthenticatedUser user) {
        return queryTodosPort.findTodos(user.userId()).stream()
                .filter(todo -> filterByStatus(todo, status))
                .map(this::mapToResult)
                .collect(Collectors.toList());
    }

    private boolean filterByStatus(Todo todo, TodoStatus status) {
        return switch (status) {
            case ALL -> true;
            case ACTIVE -> !todo.isDone();
            case DONE -> todo.isDone();
        };
    }

    private TodoResult mapToResult(Todo todo) {
        return new TodoResult(
                todo.getId(),
                todo.getTitle().value(),
                todo.getDueDate(),
                todo.isDone(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
