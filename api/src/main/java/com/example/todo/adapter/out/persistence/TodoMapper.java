package com.example.todo.adapter.out.persistence;

import com.example.todo.domain.model.DueDate;
import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class TodoMapper {

    TodoJpaEntity toEntity(Todo todo) {
        return new TodoJpaEntity(
            todo.id().value(),
            todo.userId().value(),
            todo.title().value(),
            todo.dueDate().value().orElse(null),
            todo.done(),
            todo.createdAt(),
            todo.updatedAt()
        );
    }

    Todo toDomain(TodoJpaEntity entity) {
        return Todo.restore(
            TodoId.from(entity.getId()),
            UserId.from(entity.getUserId()),
            Title.from(entity.getTitle()),
            Optional.ofNullable(entity.getDueDate()).map(DueDate::of).orElse(DueDate.none()),
            entity.isDone(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
