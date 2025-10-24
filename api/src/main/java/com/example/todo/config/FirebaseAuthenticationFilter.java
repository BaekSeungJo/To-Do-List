package com.example.todo.config;

import com.example.todo.adapter.out.auth.FirebaseTokenVerifier;
import com.example.todo.adapter.out.auth.FirebaseUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseTokenVerifier tokenVerifier;

    public FirebaseAuthenticationFilter(FirebaseTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }

        Optional<FirebaseUser> user = tokenVerifier.verify(token);
        if (user.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token");
            return;
        }

        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(user.get().uid());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    private String resolveToken(String header) {
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
