package com.example.todo.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier value object for Todo aggregate.
 */
public final class TodoId {

    private final UUID value;

    private TodoId(UUID value) {
        this.value = value;
    }

    public static TodoId newId() {
        return new TodoId(UUID.randomUUID());
    }

    public static TodoId from(UUID value) {
        Objects.requireNonNull(value, "Todo id must not be null");
        return new TodoId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TodoId todoId)) {
            return false;
        }
        return value.equals(todoId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
