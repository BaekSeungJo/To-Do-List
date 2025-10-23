package com.example.todo.common.auth;

import java.util.Collections;
import java.util.Set;

public record AuthenticatedUser(String userId, String email, Set<String> roles) {

    public AuthenticatedUser {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
