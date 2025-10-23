package com.example.todo.adapter.out.persistence;

import com.example.todo.application.port.out.DeleteTodoPort;
import com.example.todo.application.port.out.LoadTodoPort;
import com.example.todo.application.port.out.QueryTodosPort;
import com.example.todo.application.port.out.SaveTodoPort;
import com.example.todo.domain.model.Todo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TodoPersistenceAdapter implements SaveTodoPort, LoadTodoPort, DeleteTodoPort, QueryTodosPort {

    private final TodoJpaRepository repository;
    private final TodoMapper mapper;

    public TodoPersistenceAdapter(TodoJpaRepository repository) {
        this.repository = repository;
        this.mapper = new TodoMapper();
    }

    @Override
    public Todo save(Todo todo) {
        TodoEntity entity = mapper.toEntity(todo);
        TodoEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Todo> loadByIdAndUser(UUID id, String userId) {
        return repository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> findTodos(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
