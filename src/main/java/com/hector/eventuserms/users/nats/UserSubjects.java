package com.hector.eventuserms.users.nats;

public final class UserSubjects {

    // Avoid can instance this class anywhere.
    private UserSubjects() {
    }

    public static final String GET_ALL = "users.get.all";
    public static final String GET_ID = "users.get.id";
    public static final String CREATE = "users.create";
    public static final String UPDATE = "users.update";
    public static final String DELETE = "users.delete";
    public static final String LOGIN = "auth.login";
}
