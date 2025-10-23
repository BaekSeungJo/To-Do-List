package com.example.todo.application.service;

import com.example.todo.application.port.in.CreateTodoUseCase;
import com.example.todo.application.port.in.DeleteTodoUseCase;
import com.example.todo.application.port.in.ToggleTodoUseCase;
import com.example.todo.application.port.in.UpdateTodoUseCase;
import com.example.todo.application.port.in.command.CreateTodoCommand;
import com.example.todo.application.port.in.command.DeleteTodoCommand;
import com.example.todo.application.port.in.command.ToggleTodoCommand;
import com.example.todo.application.port.in.command.UpdateTodoCommand;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.application.port.out.DeleteTodoPort;
import com.example.todo.application.port.out.LoadTodoPort;
import com.example.todo.application.port.out.SaveTodoPort;
import com.example.todo.common.auth.AuthenticatedUser;
import com.example.todo.common.exception.ResourceNotFoundException;
import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoCommandService implements CreateTodoUseCase, UpdateTodoUseCase, ToggleTodoUseCase, DeleteTodoUseCase {

    private final SaveTodoPort saveTodoPort;
    private final LoadTodoPort loadTodoPort;
    private final DeleteTodoPort deleteTodoPort;
    private final Clock clock;

    public TodoCommandService(SaveTodoPort saveTodoPort,
                              LoadTodoPort loadTodoPort,
                              DeleteTodoPort deleteTodoPort,
                              Clock clock) {
        this.saveTodoPort = saveTodoPort;
        this.loadTodoPort = loadTodoPort;
        this.deleteTodoPort = deleteTodoPort;
        this.clock = clock;
    }

    @Override
    public TodoResult create(CreateTodoCommand command, AuthenticatedUser user) {
        Todo todo = Todo.create(new UserId(user.userId()), new Title(command.title()), command.dueDate(), Instant.now(clock));
        Todo saved = saveTodoPort.save(todo);
        return mapToResult(saved);
    }

    @Override
    public TodoResult update(UpdateTodoCommand command, AuthenticatedUser user) {
        Todo existing = loadTodoOrThrow(command.id(), user.userId());
        Todo updated = existing.withUpdatedDetails(new Title(command.title()), command.dueDate(), Instant.now(clock));
        Todo saved = saveTodoPort.save(updated);
        return mapToResult(saved);
    }

    @Override
    public TodoResult toggle(ToggleTodoCommand command, AuthenticatedUser user) {
        Todo existing = loadTodoOrThrow(command.id(), user.userId());
        Todo toggled = existing.toggleDone(Instant.now(clock));
        Todo saved = saveTodoPort.save(toggled);
        return mapToResult(saved);
    }

    @Override
    public void delete(DeleteTodoCommand command, AuthenticatedUser user) {
        loadTodoOrThrow(command.id(), user.userId());
        deleteTodoPort.deleteById(command.id());
    }

    private Todo loadTodoOrThrow(java.util.UUID id, String userId) {
        return loadTodoPort.loadByIdAndUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
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
