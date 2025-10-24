package com.example.todo.domain.model;

import java.util.Objects;

/**
 * Represents the authenticated Firebase user identifier.
 */
public final class UserId {

    private final String value;

    private UserId(String value) {
        this.value = value;
    }

    public static UserId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return new UserId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserId userId)) {
            return false;
        }
        return value.equals(userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
