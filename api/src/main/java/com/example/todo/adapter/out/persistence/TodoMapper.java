package com.example.todo.adapter.out.persistence;

import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.UserId;

public class TodoMapper {

    public TodoEntity toEntity(Todo todo) {
        return new TodoEntity(
                todo.getId(),
                todo.getUserId().value(),
                todo.getTitle().value(),
                todo.getDueDate(),
                todo.isDone(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }

    public Todo toDomain(TodoEntity entity) {
        return Todo.reconstruct(
                entity.getId(),
                new UserId(entity.getUserId()),
                new Title(entity.getTitle()),
                entity.getDueDate(),
                entity.isDone(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
