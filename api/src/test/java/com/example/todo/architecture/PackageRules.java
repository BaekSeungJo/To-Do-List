package com.example.todo.architecture;

public final class PackageRules {
    private PackageRules() {
    }

    public static final String ROOT = "com.example.todo..";
    public static final String DOMAIN = "com.example.todo.domain..";
    public static final String APPLICATION = "com.example.todo.application..";
    public static final String APPLICATION_SERVICE = "com.example.todo.application.service..";
    public static final String APPLICATION_PORT_IN = "com.example.todo.application.port.in..";
    public static final String APPLICATION_PORT_OUT = "com.example.todo.application.port.out..";
    public static final String ADAPTER_IN_WEB = "com.example.todo.adapter.in.web..";
    public static final String ADAPTER_OUT_PERSISTENCE = "com.example.todo.adapter.out.persistence..";
    public static final String ADAPTER_OUT_AUTH = "com.example.todo.adapter.out.auth..";
}
