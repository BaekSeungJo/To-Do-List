package com.example.todo.adapter.in.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.todo.adapter.out.auth.FirebaseTokenVerifier;
import com.example.todo.adapter.out.auth.FirebaseUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FirebaseTokenVerifier tokenVerifier;

    @BeforeEach
    void setUp() {
        Mockito.reset(tokenVerifier);
        when(tokenVerifier.verify(eq("token"))).thenReturn(Optional.of(new FirebaseUser("user-int")));
    }

    @Test
    void createAndFetchTodoLifecycle() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("Integration Todo");

        String response = mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        TodoResponse created = objectMapper.readValue(response, TodoResponse.class);

        mockMvc.perform(get("/api/todos")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(created.id().toString()))
            .andExpect(jsonPath("$[0].title").value("Integration Todo"));

        UpdateTodoRequest updateRequest = new UpdateTodoRequest();
        updateRequest.setTitle(Optional.of("Updated"));

        mockMvc.perform(patch("/api/todos/" + created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated"));
    }
}
