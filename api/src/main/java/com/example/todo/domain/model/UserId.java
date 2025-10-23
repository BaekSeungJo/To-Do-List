package com.example.todo.domain.model;

import com.example.todo.common.exception.ValidationException;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new ValidationException("UserId must not be blank");
        }
        value = value.trim();
    }
}
