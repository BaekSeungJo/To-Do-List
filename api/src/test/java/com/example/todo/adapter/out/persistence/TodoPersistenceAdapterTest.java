package com.example.todo.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.todo.application.query.TodoQuery;
import com.example.todo.domain.model.DueDate;
import com.example.todo.domain.model.Title;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TodoPersistenceAdapter.class, TodoMapper.class})
@ActiveProfiles("test")
class TodoPersistenceAdapterTest {

    @Autowired
    private TodoPersistenceAdapter adapter;

    @Test
    void saveAndLoadTodo() {
        UserId userId = UserId.from("user-1");
        Todo todo = Todo.create(userId, Title.from("Persist"), DueDate.of(LocalDate.of(2025, 1, 1)), Instant.now());

        Todo persisted = adapter.save(todo);
        assertThat(persisted.id()).isNotNull();

        assertThat(adapter.loadByIdAndUser(persisted.id(), userId)).isPresent();
    }

    @Test
    void loadTodosFiltersByStatus() {
        UserId userId = UserId.from("user-2");
        Todo active = Todo.create(userId, Title.from("Active"), DueDate.none(), Instant.now());
        Todo done = Todo.create(userId, Title.from("Done"), DueDate.none(), Instant.now()).withDone(true, Instant.now());
        adapter.save(active);
        adapter.save(done);

        List<Todo> all = adapter.loadTodos(userId, TodoQuery.Filter.ALL);
        List<Todo> activeOnly = adapter.loadTodos(userId, TodoQuery.Filter.ACTIVE);
        List<Todo> doneOnly = adapter.loadTodos(userId, TodoQuery.Filter.DONE);

        assertThat(all).hasSize(2);
        assertThat(activeOnly).extracting(Todo::done).containsOnly(false);
        assertThat(doneOnly).extracting(Todo::done).containsOnly(true);
    }
}
