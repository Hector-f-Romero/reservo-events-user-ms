package com.hector.eventuserms.events.nats;

/* Por facilidad, no uso un enum para no tener problemas en las condiciones de la anotación @EventListener, pues tendría que declarar el path completo donde se encuentra esta clase para así asegurar que solo escuchará un evento con el subject solicitado */
// import lombok.Getter;
// import lombok.RequiredArgsConstructor;

// @Getter
// @RequiredArgsConstructor
// public enum EventSubjects {
//     GET_ALL_EVENTS("events.get.all"),
//     GET_UPCOMING_EVENTS("events.get.upcoming"),
//     GET_UPCOMING_EVENTS_BY_DATE("get.events.upcoming.date"),
//     GET_ID("events.get.id"),
//     CREATE("events.create");

//     private final String value;
// }

public final class EventSubjects {

    // Evita que pueda instanciarse esta clase desde otro lado.
    private EventSubjects() {
    }

    public static final String GET_ALL_EVENTS = "events.get.all";
    public static final String GET_UPCOMING_EVENTS = "events.get.upcoming";
    public static final String GET_UPCOMING_EVENTS_BY_DATE = "events.get.upcoming.date";
    public static final String GET_ID = "events.get.id";
    public static final String CREATE = "events.create";

}
