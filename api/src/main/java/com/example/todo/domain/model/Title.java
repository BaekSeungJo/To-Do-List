package com.example.todo.domain.model;

import java.util.Objects;

/**
 * Value object representing the title of a Todo.
 */
public final class Title {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;

    private final String value;

    private Title(String value) {
        this.value = value;
    }

    public static Title from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Title must not be null");
        }
        String trimmed = value.trim();
        int length = trimmed.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Title must be between %d and %d characters".formatted(MIN_LENGTH, MAX_LENGTH));
        }
        return new Title(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Title title)) {
            return false;
        }
        return value.equals(title.value);
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
