package com.example.todo.application.port.out;

import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.UserId;

public interface DeleteTodoPort {

    void deleteByIdAndUser(TodoId todoId, UserId userId);
}
