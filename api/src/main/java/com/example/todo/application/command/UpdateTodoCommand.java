package com.example.todo.application.command;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Command describing partial updates for a todo resource including field presence information.
 */
public final class UpdateTodoCommand {

    private final UUID todoId;
    private final Optional<String> title;
    private final Optional<LocalDate> dueDate;
    private final Optional<Boolean> done;
    private final boolean titleProvided;
    private final boolean dueDateProvided;
    private final boolean doneProvided;

    private UpdateTodoCommand(UUID todoId,
                              Optional<String> title,
                              boolean titleProvided,
                              Optional<LocalDate> dueDate,
                              boolean dueDateProvided,
                              Optional<Boolean> done,
                              boolean doneProvided) {
        this.todoId = Objects.requireNonNull(todoId, "todoId");
        this.title = Objects.requireNonNull(title, "title");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
        this.done = Objects.requireNonNull(done, "done");
        this.titleProvided = titleProvided;
        this.dueDateProvided = dueDateProvided;
        this.doneProvided = doneProvided;
        if (!titleProvided && !dueDateProvided && !doneProvided) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }

    public static UpdateTodoCommand of(UUID todoId,
                                       Optional<String> title,
                                       boolean titleProvided,
                                       Optional<LocalDate> dueDate,
                                       boolean dueDateProvided,
                                       Optional<Boolean> done,
                                       boolean doneProvided) {
        Optional<String> safeTitle = title == null ? Optional.empty() : title.map(String::trim);
        Optional<LocalDate> safeDueDate = dueDate == null ? Optional.empty() : dueDate;
        Optional<Boolean> safeDone = done == null ? Optional.empty() : done;
        return new UpdateTodoCommand(todoId, safeTitle, titleProvided, safeDueDate, dueDateProvided, safeDone, doneProvided);
    }

    public UUID todoId() {
        return todoId;
    }

    public Optional<String> title() {
        return title;
    }

    public Optional<LocalDate> dueDate() {
        return dueDate;
    }

    public Optional<Boolean> done() {
        return done;
    }

    public boolean titleProvided() {
        return titleProvided;
    }

    public boolean dueDateProvided() {
        return dueDateProvided;
    }

    public boolean doneProvided() {
        return doneProvided;
    }
}
