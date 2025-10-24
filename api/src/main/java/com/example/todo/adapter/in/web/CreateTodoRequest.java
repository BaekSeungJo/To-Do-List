package com.example.todo.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Optional;

public class CreateTodoRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String title;

    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Optional<LocalDate> getDueDate() {
        return Optional.ofNullable(dueDate);
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
