package com.example.todo.common.exception;

public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
