package com.hector.eventuserms.seats.nats;

public final class SeatSubjects {

    // Avoid can instance this class anywhere.
    private SeatSubjects() {
    }

    public static final String GET_ID = "seats.get.id";
    public static final String CREATE = "seats.create";
    public static final String CREATE_ALL = "seats.create.all";
    public static final String UPDATE = "seats.update";
}
