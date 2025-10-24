package com.example.todo.adapter.out.persistence;

import com.example.todo.application.port.out.DeleteTodoPort;
import com.example.todo.application.port.out.LoadTodoPort;
import com.example.todo.application.port.out.LoadTodosPort;
import com.example.todo.application.port.out.SaveTodoPort;
import com.example.todo.application.query.TodoQuery;
import com.example.todo.common.TodoNotFoundException;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.UserId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class TodoPersistenceAdapter implements SaveTodoPort, LoadTodoPort, DeleteTodoPort, LoadTodosPort {

    private final TodoJpaRepository repository;
    private final TodoMapper mapper;

    TodoPersistenceAdapter(TodoJpaRepository repository, TodoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Todo save(Todo todo) {
        TodoJpaEntity saved = repository.save(mapper.toEntity(todo));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Todo> loadByIdAndUser(TodoId todoId, UserId userId) {
        return repository.findByIdAndUserId(todoId.value(), userId.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteByIdAndUser(TodoId todoId, UserId userId) {
        long removed = repository.deleteByIdAndUserId(todoId.value(), userId.value());
        if (removed == 0) {
            throw new TodoNotFoundException("Todo not found");
        }
    }

    @Override
    public List<Todo> loadTodos(UserId userId, TodoQuery.Filter filter) {
        List<TodoJpaEntity> entities;
        if (filter == TodoQuery.Filter.DONE) {
            entities = repository.findAllByUserIdAndDoneOrderByCreatedAtDesc(userId.value(), true);
        } else if (filter == TodoQuery.Filter.ACTIVE) {
            entities = repository.findAllByUserIdAndDoneOrderByCreatedAtDesc(userId.value(), false);
        } else {
            entities = repository.findAllByUserIdOrderByCreatedAtDesc(userId.value());
        }
        return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
