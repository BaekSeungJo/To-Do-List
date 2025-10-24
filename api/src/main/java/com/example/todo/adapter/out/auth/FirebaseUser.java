package com.example.todo.adapter.out.auth;

public record FirebaseUser(String uid) {

    public FirebaseUser {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("uid must not be blank");
        }
    }
}
