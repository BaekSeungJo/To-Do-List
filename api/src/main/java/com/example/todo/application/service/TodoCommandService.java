package com.example.todo.application.service;

import com.example.todo.application.command.CreateTodoCommand;
import com.example.todo.application.command.DeleteTodoCommand;
import com.example.todo.application.command.UpdateTodoCommand;
import com.example.todo.application.port.in.AuthenticatedUser;
import com.example.todo.application.port.in.CreateTodoUseCase;
import com.example.todo.application.port.in.DeleteTodoUseCase;
import com.example.todo.application.port.in.UpdateTodoUseCase;
import com.example.todo.application.port.out.DeleteTodoPort;
import com.example.todo.application.port.out.LoadTodoPort;
import com.example.todo.application.port.out.SaveTodoPort;
import com.example.todo.application.result.TodoResult;
import com.example.todo.common.TodoNotFoundException;
import com.example.todo.domain.model.DueDate;
import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoCommandService implements CreateTodoUseCase, UpdateTodoUseCase, DeleteTodoUseCase {

    private final LoadTodoPort loadTodoPort;
    private final SaveTodoPort saveTodoPort;
    private final DeleteTodoPort deleteTodoPort;
    private final Clock clock;

    public TodoCommandService(LoadTodoPort loadTodoPort, SaveTodoPort saveTodoPort, DeleteTodoPort deleteTodoPort, Clock clock) {
        this.loadTodoPort = loadTodoPort;
        this.saveTodoPort = saveTodoPort;
        this.deleteTodoPort = deleteTodoPort;
        this.clock = clock;
    }

    @Override
    public TodoResult create(CreateTodoCommand command, AuthenticatedUser user) {
        Instant now = Instant.now(clock);
        Title title = Title.from(command.title());
        DueDate dueDate = command.dueDate().map(DueDate::of).orElse(DueDate.none());
        Todo todo = Todo.create(user.toUserId(), title, dueDate, now);
        Todo saved = saveTodoPort.save(todo);
        return TodoResult.from(saved);
    }

    @Override
    public TodoResult update(UpdateTodoCommand command, AuthenticatedUser user) {
        TodoId todoId = TodoId.from(command.todoId());
        Todo todo = loadTodoPort.loadByIdAndUser(todoId, user.toUserId())
            .orElseThrow(() -> new TodoNotFoundException("Todo not found"));
        Instant now = Instant.now(clock);
        Todo updated = todo;
        if (command.titleProvided()) {
            String newTitle = command.title()
                .orElseThrow(() -> new IllegalArgumentException("Title must not be null"));
            updated = updated.withTitle(Title.from(newTitle), now);
        }
        if (command.dueDateProvided()) {
            DueDate dueDate = command.dueDate().map(DueDate::of).orElse(DueDate.none());
            updated = updated.withDueDate(dueDate, now);
        }
        if (command.doneProvided()) {
            boolean done = command.done()
                .orElseThrow(() -> new IllegalArgumentException("Done flag must not be null"));
            updated = updated.withDone(done, now);
        }
        Todo saved = saveTodoPort.save(updated);
        return TodoResult.from(saved);
    }

    @Override
    public void delete(DeleteTodoCommand command, AuthenticatedUser user) {
        TodoId todoId = TodoId.from(command.todoId());
        deleteTodoPort.deleteByIdAndUser(todoId, user.toUserId());
    }
}
