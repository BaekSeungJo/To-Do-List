package com.example.todo.adapter.out.auth;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Minimal Firebase token verifier placeholder.
 * In production this should call Firebase Admin SDK to validate the token and extract the UID.
 */
@Component
public class FirebaseAuthAdapter implements FirebaseTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthAdapter.class);

    @Override
    public Optional<FirebaseUser> verify(String bearerToken) {
        if (!StringUtils.hasText(bearerToken)) {
            return Optional.empty();
        }
        // Placeholder implementation: treat the token itself as the UID.
        log.debug("Accepting Firebase token placeholder validation");
        return Optional.of(new FirebaseUser(bearerToken.trim()));
    }
}
