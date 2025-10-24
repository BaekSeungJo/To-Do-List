package com.example.todo.application.query;

import com.example.todo.application.query.TodoQuery.Filter;

/**
 * Query DTO for retrieving todos.
 */
public record TodoQuery(Filter filter) {

    public TodoQuery {
        if (filter == null) {
            filter = Filter.ALL;
        }
    }

    public enum Filter {
        ALL,
        ACTIVE,
        DONE
    }
}
