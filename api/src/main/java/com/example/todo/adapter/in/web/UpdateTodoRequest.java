package com.example.todo.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.LocalDate;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UpdateTodoRequest {

    private Optional<String> title = Optional.empty();
    private boolean titleProvided;
    private Optional<LocalDate> dueDate = Optional.empty();
    private boolean dueDateProvided;
    private Optional<Boolean> done = Optional.empty();
    private boolean doneProvided;

    public Optional<String> getTitle() {
        return title;
    }

    @JsonSetter
    public void setTitle(Optional<String> title) {
        this.titleProvided = true;
        this.title = title == null ? Optional.empty() : title;
    }

    public Optional<LocalDate> getDueDate() {
        return dueDate;
    }

    @JsonSetter
    public void setDueDate(Optional<LocalDate> dueDate) {
        this.dueDateProvided = true;
        this.dueDate = dueDate == null ? Optional.empty() : dueDate;
    }

    public Optional<Boolean> getDone() {
        return done;
    }

    @JsonSetter
    public void setDone(Optional<Boolean> done) {
        this.doneProvided = true;
        this.done = done == null ? Optional.empty() : done;
    }

    public boolean isTitleProvided() {
        return titleProvided;
    }

    public boolean isDueDateProvided() {
        return dueDateProvided;
    }

    public boolean isDoneProvided() {
        return doneProvided;
    }
}
