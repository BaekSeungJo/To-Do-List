package com.example.todo.adapter.in.web;

import com.example.todo.application.port.in.CreateTodoUseCase;
import com.example.todo.application.port.in.DeleteTodoUseCase;
import com.example.todo.application.port.in.ToggleTodoUseCase;
import com.example.todo.application.port.in.UpdateTodoUseCase;
import com.example.todo.application.port.in.command.CreateTodoCommand;
import com.example.todo.application.port.in.command.DeleteTodoCommand;
import com.example.todo.application.port.in.command.ToggleTodoCommand;
import com.example.todo.application.port.in.command.UpdateTodoCommand;
import com.example.todo.application.port.in.query.GetTodosQuery;
import com.example.todo.application.port.in.query.criteria.TodoStatus;
import com.example.todo.application.port.in.result.TodoResult;
import com.example.todo.common.auth.AuthenticatedUser;
import com.example.todo.common.exception.ValidationException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ToggleTodoUseCase toggleTodoUseCase;
    private final DeleteTodoUseCase deleteTodoUseCase;
    private final GetTodosQuery getTodosQuery;

    public TodoController(CreateTodoUseCase createTodoUseCase,
                          UpdateTodoUseCase updateTodoUseCase,
                          ToggleTodoUseCase toggleTodoUseCase,
                          DeleteTodoUseCase deleteTodoUseCase,
                          GetTodosQuery getTodosQuery) {
        this.createTodoUseCase = createTodoUseCase;
        this.updateTodoUseCase = updateTodoUseCase;
        this.toggleTodoUseCase = toggleTodoUseCase;
        this.deleteTodoUseCase = deleteTodoUseCase;
        this.getTodosQuery = getTodosQuery;
    }

    @GetMapping
    public List<TodoResponse> listTodos(@RequestParam(defaultValue = "all") String status,
                                        Authentication authentication) {
        AuthenticatedUser user = resolveUser(authentication);
        TodoStatus todoStatus = parseStatus(status);
        return getTodosQuery.getTodos(todoStatus, user).stream()
                .map(TodoResponse::from)
                .toList();
    }

    @PostMapping
    public TodoResponse create(@Valid @RequestBody CreateTodoRequest request,
                               Authentication authentication) {
        AuthenticatedUser user = resolveUser(authentication);
        TodoResult result = createTodoUseCase.create(new CreateTodoCommand(request.title(), request.dueDate()), user);
        return TodoResponse.from(result);
    }

    @PatchMapping("/{id}")
    public TodoResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateTodoRequest request,
                               Authentication authentication) {
        AuthenticatedUser user = resolveUser(authentication);
        TodoResult result = updateTodoUseCase.update(new UpdateTodoCommand(id, request.title(), request.dueDate()), user);
        return TodoResponse.from(result);
    }

    @PatchMapping("/{id}/toggle")
    public TodoResponse toggle(@PathVariable UUID id,
                               Authentication authentication) {
        AuthenticatedUser user = resolveUser(authentication);
        TodoResult result = toggleTodoUseCase.toggle(new ToggleTodoCommand(id), user);
        return TodoResponse.from(result);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id,
                       Authentication authentication) {
        AuthenticatedUser user = resolveUser(authentication);
        deleteTodoUseCase.delete(new DeleteTodoCommand(id), user);
    }

    private TodoStatus parseStatus(String status) {
        try {
            return TodoStatus.valueOf(status.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid status: " + status);
        }
    }

    private AuthenticatedUser resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("Authentication is required");
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUser(authentication.getName(), null, roles);
    }
}
