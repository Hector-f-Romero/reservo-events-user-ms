package com.hector.eventuserms.events.nats;

public final class EventSubjects {
    // Avoid can instance this class anywhere.
    private EventSubjects() {
    }

    public static final String GET_ALL_EVENTS = "events.get.all";
    public static final String GET_UPCOMING_EVENTS = "events.get.upcoming";
    public static final String GET_UPCOMING_EVENTS_BY_DATE = "events.get.upcoming.date";
    public static final String GET_ID = "events.get.id";
    public static final String CREATE = "events.create";

}
