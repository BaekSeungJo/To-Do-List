package com.example.todo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TodoApplicationTests {

    @Test
    void repositoryScaffoldLoads() {
        assertThat(TodoApplication.class.getPackageName()).isEqualTo("com.example.todo");
    }
}
