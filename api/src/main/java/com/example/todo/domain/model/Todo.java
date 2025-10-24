package com.example.todo.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a Todo item.
 */
public final class Todo {

    private final TodoId id;
    private final UserId userId;
    private final Title title;
    private final DueDate dueDate;
    private final boolean done;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Todo(TodoId id, UserId userId, Title title, DueDate dueDate, boolean done, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.title = Objects.requireNonNull(title, "title");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
        this.done = done;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Todo create(UserId userId, Title title, DueDate dueDate, Instant now) {
        return new Todo(TodoId.newId(), userId, title, dueDate, false, now, now);
    }

    public static Todo restore(TodoId id, UserId userId, Title title, DueDate dueDate, boolean done, Instant createdAt, Instant updatedAt) {
        return new Todo(id, userId, title, dueDate, done, createdAt, updatedAt);
    }

    public Todo withTitle(Title title, Instant now) {
        if (this.title.equals(title)) {
            return this;
        }
        return new Todo(id, userId, title, dueDate, done, createdAt, now);
    }

    public Todo withDueDate(DueDate dueDate, Instant now) {
        if (this.dueDate.equals(dueDate)) {
            return this;
        }
        return new Todo(id, userId, title, dueDate, done, createdAt, now);
    }

    public Todo withDone(boolean done, Instant now) {
        if (this.done == done) {
            return this;
        }
        return new Todo(id, userId, title, dueDate, done, createdAt, now);
    }

    public TodoId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public Title title() {
        return title;
    }

    public DueDate dueDate() {
        return dueDate;
    }

    public boolean done() {
        return done;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
