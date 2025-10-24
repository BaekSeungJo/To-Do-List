package com.example.todo.adapter.out.auth;

import java.util.Optional;

public interface FirebaseTokenVerifier {

    Optional<FirebaseUser> verify(String bearerToken);
}
