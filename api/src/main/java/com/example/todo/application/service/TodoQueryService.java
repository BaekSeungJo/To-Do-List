package com.example.todo.application.service;

import com.example.todo.application.port.in.AuthenticatedUser;
import com.example.todo.application.port.in.GetTodosQueryUseCase;
import com.example.todo.application.port.out.LoadTodosPort;
import com.example.todo.application.query.TodoQuery;
import com.example.todo.application.result.TodoResult;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoQueryService implements GetTodosQueryUseCase {

    private final LoadTodosPort loadTodosPort;

    public TodoQueryService(LoadTodosPort loadTodosPort) {
        this.loadTodosPort = loadTodosPort;
    }

    @Override
    public List<TodoResult> getTodos(TodoQuery query, AuthenticatedUser user) {
        return loadTodosPort.loadTodos(user.toUserId(), query.filter()).stream()
            .map(TodoResult::from)
            .collect(Collectors.toList());
    }
}
