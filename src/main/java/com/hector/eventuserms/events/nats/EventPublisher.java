package com.hector.eventuserms.events.nats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class EventPublisher {

    private final Connection natsConnection;
    private final ApplicationEventPublisher eventPublisher;

    public EventPublisher(Connection natsConnection, ApplicationEventPublisher eventPublisher) {
        this.natsConnection = natsConnection;
        this.eventPublisher = eventPublisher;
    }

    // Si quieremos usar AOP para interceptar los errores que puedan provocarse
    // durante la lógica de negocio o serialización de información usando
    // comunicación de NATS sin repetir bloques try/catch en cada función, debemos
    // usar un enfoque de publicación de eventos de Spring en el distpacher, pues
    // los callbacks omiten el Spring Proxy, haciendo que los aspectos nunca puedan
    // interceptar las funciones que quieran interceptar.
    // Gracias a esto, se separó la lógica de comunicación de NATS en varios
    // archivos:
    // EventPublisher -> publica el evento.
    // EventNatsController -> Escucha los eventos asociados y ejecuta la función
    // requerida.
    // EventSubjects -> Clase con los nombres de los subjects NATS de forma
    // centralizada (No usé ENUM por comodidad al usar el @EventListener con el
    // argumento de condition)
    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = natsConnection.createDispatcher();

        // Register all subscribes and publish its corresponding event.
        dispatcher.subscribe(EventSubjects.GET_ID,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_ID));

        dispatcher.subscribe(EventSubjects.GET_ALL_EVENTS,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_ALL_EVENTS));

        dispatcher.subscribe(EventSubjects.GET_UPCOMING_EVENTS,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_UPCOMING_EVENTS));

        dispatcher.subscribe(EventSubjects.GET_UPCOMING_EVENTS_BY_DATE,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_UPCOMING_EVENTS_BY_DATE));

        dispatcher.subscribe(EventSubjects.CREATE,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.CREATE));

    }

    // Centraliza la creqación de Eventos para ser escuchados y los publica.
    private void publishNatsEvent(Message msg, String subject) {
        NatsMessageEvent newNatsMessage = new NatsMessageEvent(msg, subject);
        this.eventPublisher.publishEvent(newNatsMessage);
    }
}
