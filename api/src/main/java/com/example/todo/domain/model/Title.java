package com.example.todo.domain.model;

import com.example.todo.common.exception.ValidationException;

public record Title(String value) {

    public Title {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Title must not be blank");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 100) {
            throw new ValidationException("Title must be 1 to 100 characters long");
        }
        value = trimmed;
    }
}
