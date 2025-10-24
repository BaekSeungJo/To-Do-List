package com.example.todo.common;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String message) {
        super(message);
    }
}
