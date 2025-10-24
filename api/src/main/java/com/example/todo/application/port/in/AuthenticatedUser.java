package com.example.todo.application.port.in;

import com.example.todo.domain.model.UserId;

/**
 * Represents the authenticated user passed from the adapter layer into the application layer.
 */
public record AuthenticatedUser(String uid) {

    public AuthenticatedUser {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("uid must not be blank");
        }
    }

    public UserId toUserId() {
        return UserId.from(uid);
    }
}
