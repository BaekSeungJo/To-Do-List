package com.example.todo.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object encapsulating an optional due date for a Todo.
 */
public final class DueDate {

    private final LocalDate value;

    private DueDate(LocalDate value) {
        this.value = value;
    }

    public static DueDate of(LocalDate value) {
        Objects.requireNonNull(value, "Due date must not be null");
        return new DueDate(value);
    }

    public static DueDate none() {
        return new DueDate(null);
    }

    public Optional<LocalDate> value() {
        return Optional.ofNullable(value);
    }

    public LocalDate orElse(LocalDate fallback) {
        return value != null ? value : fallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DueDate dueDate)) {
            return false;
        }
        return Objects.equals(value, dueDate.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }
}
