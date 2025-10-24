package com.example.todo.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.todo.application.command.CreateTodoCommand;
import com.example.todo.application.command.DeleteTodoCommand;
import com.example.todo.application.command.UpdateTodoCommand;
import com.example.todo.application.port.in.AuthenticatedUser;
import com.example.todo.application.port.out.DeleteTodoPort;
import com.example.todo.application.port.out.LoadTodoPort;
import com.example.todo.application.port.out.SaveTodoPort;
import com.example.todo.application.result.TodoResult;
import com.example.todo.common.TodoNotFoundException;
import com.example.todo.domain.model.DueDate;
import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TodoCommandServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private InMemoryTodoPersistence persistence;
    private TodoCommandService service;

    @BeforeEach
    void setUp() {
        persistence = new InMemoryTodoPersistence();
        service = new TodoCommandService(persistence, persistence, persistence, FIXED_CLOCK);
    }

    @Test
    void createTodoStoresAggregate() {
        CreateTodoCommand command = new CreateTodoCommand("My first todo", Optional.empty());
        AuthenticatedUser user = new AuthenticatedUser("user-1");

        TodoResult result = service.create(command, user);

        assertThat(result.title()).isEqualTo("My first todo");
        assertThat(result.done()).isFalse();
        assertThat(persistence.todos).hasSize(1);
    }

    @Test
    void updateTodoChangesTitle() {
        AuthenticatedUser user = new AuthenticatedUser("user-2");
        Todo existing = Todo.create(user.toUserId(), Title.from("Before"), DueDate.none(), Instant.now(FIXED_CLOCK));
        persistence.save(existing);

        UpdateTodoCommand command = UpdateTodoCommand.of(
            existing.id().value(),
            Optional.of("After"),
            true,
            Optional.empty(),
            false,
            Optional.empty(),
            false
        );
        TodoResult result = service.update(command, user);

        assertThat(result.title()).isEqualTo("After");
        assertThat(persistence.todos.get(existing.id().value()).title().value()).isEqualTo("After");
    }

    @Test
    void deleteTodoRemovesAggregate() {
        AuthenticatedUser user = new AuthenticatedUser("user-3");
        Todo existing = Todo.create(user.toUserId(), Title.from("Delete me"), DueDate.none(), Instant.now(FIXED_CLOCK));
        persistence.save(existing);

        service.delete(new DeleteTodoCommand(existing.id().value()), user);

        assertThat(persistence.todos).isEmpty();
    }

    private static class InMemoryTodoPersistence implements SaveTodoPort, LoadTodoPort, DeleteTodoPort {

        private final Map<UUID, Todo> todos = new ConcurrentHashMap<>();

        @Override
        public Todo save(Todo todo) {
            todos.put(todo.id().value(), todo);
            return todo;
        }

        @Override
        public Optional<Todo> loadByIdAndUser(TodoId todoId, UserId userId) {
            Todo todo = todos.get(todoId.value());
            if (todo == null || !todo.userId().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(todo);
        }

        @Override
        public void deleteByIdAndUser(TodoId todoId, UserId userId) {
            Todo removed = todos.remove(todoId.value());
            if (removed == null || !removed.userId().equals(userId)) {
                throw new TodoNotFoundException("Todo not found");
            }
        }
    }
}
