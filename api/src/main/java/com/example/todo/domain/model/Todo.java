package com.example.todo.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Todo {

    private final UUID id;
    private final UserId userId;
    private final Title title;
    private final LocalDate dueDate;
    private final boolean done;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Todo(UUID id,
                 UserId userId,
                 Title title,
                 LocalDate dueDate,
                 boolean done,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.title = Objects.requireNonNull(title, "title");
        this.dueDate = dueDate;
        this.done = done;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Todo create(UserId userId, Title title, LocalDate dueDate, Instant now) {
        return new Todo(UUID.randomUUID(), userId, title, dueDate, false, now, now);
    }

    public static Todo reconstruct(UUID id, UserId userId, Title title, LocalDate dueDate, boolean done, Instant createdAt, Instant updatedAt) {
        return new Todo(id, userId, title, dueDate, done, createdAt, updatedAt);
    }

    public Todo withUpdatedDetails(Title newTitle, LocalDate newDueDate, Instant now) {
        return new Todo(id, userId, newTitle, newDueDate, done, createdAt, now);
    }

    public Todo toggleDone(Instant now) {
        return new Todo(id, userId, title, dueDate, !done, createdAt, now);
    }

    public UUID getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public Title getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
