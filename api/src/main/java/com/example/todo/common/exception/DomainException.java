package com.example.todo.common.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
