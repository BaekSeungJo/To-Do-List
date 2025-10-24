package com.example.todo.config;

import com.example.todo.adapter.out.auth.FirebaseTokenVerifier;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final FirebaseTokenVerifier tokenVerifier;

    public SecurityConfig(FirebaseTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        FirebaseAuthenticationFilter authenticationFilter = new FirebaseAuthenticationFilter(tokenVerifier);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(registry -> registry
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
