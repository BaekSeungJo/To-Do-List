package com.example.todo.adapter.in.web;

import com.example.todo.application.command.CreateTodoCommand;
import com.example.todo.application.command.DeleteTodoCommand;
import com.example.todo.application.command.UpdateTodoCommand;
import com.example.todo.application.port.in.AuthenticatedUser;
import com.example.todo.application.port.in.CreateTodoUseCase;
import com.example.todo.application.port.in.DeleteTodoUseCase;
import com.example.todo.application.port.in.GetTodosQueryUseCase;
import com.example.todo.application.port.in.UpdateTodoUseCase;
import com.example.todo.application.query.TodoQuery;
import com.example.todo.application.result.TodoResult;
import com.example.todo.common.InvalidTodoRequestException;
import com.example.todo.config.FirebaseUserPrincipal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final CreateTodoUseCase createTodoUseCase;
    private final UpdateTodoUseCase updateTodoUseCase;
    private final DeleteTodoUseCase deleteTodoUseCase;
    private final GetTodosQueryUseCase getTodosQueryUseCase;

    public TodoController(
        CreateTodoUseCase createTodoUseCase,
        UpdateTodoUseCase updateTodoUseCase,
        DeleteTodoUseCase deleteTodoUseCase,
        GetTodosQueryUseCase getTodosQueryUseCase
    ) {
        this.createTodoUseCase = createTodoUseCase;
        this.updateTodoUseCase = updateTodoUseCase;
        this.deleteTodoUseCase = deleteTodoUseCase;
        this.getTodosQueryUseCase = getTodosQueryUseCase;
    }

    @GetMapping
    public List<TodoResponse> getTodos(@RequestParam(name = "status", defaultValue = "all") String status,
                                       @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        TodoQuery.Filter filter = parseFilter(status);
        AuthenticatedUser user = new AuthenticatedUser(principal.uid());
        return getTodosQueryUseCase.getTodos(new TodoQuery(filter), user).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Validated @RequestBody CreateTodoRequest request,
                                                   @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        AuthenticatedUser user = new AuthenticatedUser(principal.uid());
        TodoResult result = createTodoUseCase.create(new CreateTodoCommand(request.getTitle(), request.getDueDate()), user);
        URI location = URI.create("/api/todos/" + result.id());
        return ResponseEntity.created(location).body(toResponse(result));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(@PathVariable("id") UUID id,
                                                   @RequestBody UpdateTodoRequest request,
                                                   @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        AuthenticatedUser user = new AuthenticatedUser(principal.uid());
        if (request.isTitleProvided() && request.getTitle().isEmpty()) {
            throw new InvalidTodoRequestException("Title must not be null");
        }
        if (request.isDoneProvided() && request.getDone().isEmpty()) {
            throw new InvalidTodoRequestException("Done flag must not be null");
        }
        UpdateTodoCommand command = UpdateTodoCommand.of(
            id,
            request.getTitle(),
            request.isTitleProvided(),
            request.getDueDate(),
            request.isDueDateProvided(),
            request.getDone(),
            request.isDoneProvided()
        );
        TodoResult result = updateTodoUseCase.update(command, user);
        return ResponseEntity.ok(toResponse(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") UUID id,
                                           @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        AuthenticatedUser user = new AuthenticatedUser(principal.uid());
        deleteTodoUseCase.delete(new DeleteTodoCommand(id), user);
        return ResponseEntity.noContent().build();
    }

    private TodoResponse toResponse(TodoResult result) {
        return new TodoResponse(result.id(), result.title(), result.dueDate(), result.done(), result.createdAt(), result.updatedAt());
    }

    private TodoQuery.Filter parseFilter(String value) {
        if (value == null) {
            return TodoQuery.Filter.ALL;
        }
        return switch (value.toLowerCase()) {
            case "active" -> TodoQuery.Filter.ACTIVE;
            case "done", "completed" -> TodoQuery.Filter.DONE;
            case "all" -> TodoQuery.Filter.ALL;
            default -> throw new InvalidTodoRequestException("Unsupported status filter: " + value);
        };
    }
}
