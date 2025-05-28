package com.hector.eventuserms.common.aspects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.events.nats.NatsMessageEvent;
import com.hector.eventuserms.exception.ApiError;
import com.hector.eventuserms.exception.AppError;
import com.hector.eventuserms.exception.AppServiceException;

import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Component
@Slf4j
public class NatsExcepionsAspect {

    private final NatsMessageProcessor natsMessageProcessor;

    public NatsExcepionsAspect(NatsMessageProcessor natsMessageProcessor) {
        log.info("Se ha creado el aspecto de Nats Exceptions correctamente.");
        this.natsMessageProcessor = natsMessageProcessor;
    }

    // Definimos un PointCut para indicar cuando queremos interceptar métodos a lo
    // largo de la aplicación. En este caso, queremos encontrar todos los métodos
    // que tengan la anoación @NatsHandler.
    // Le especificamos que 'natsHandler' será el parámetro que se le enviará a la
    // anotación.
    @Pointcut("@annotation(com.hector.eventuserms.common.annotations.NatsHandler)")
    public void natsHandlerMethods() {
    }

    // Definimos qué queremos hacer cuando se intercepte el método
    @Around("natsHandlerMethods()")
    public Object handleNatsExceptions(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("SE HA INTERCEPTADO UNA FUNCIÓN GRACIAS AL ASPECTO MAMAGUEVO");

        // 1. Obtenemos los argumentos del método interceptado
        Object[] args = joinPoint.getArgs();
        NatsMessageEvent messageEvent = null;

        // 2. Como podemos interceptar funciones con n cantidad de argumentos, vamos a
        // recorrer el array de objetos "args"
        for (Object arg : args) {

            /*
             * Si dentro de los argumentos encontramos uno de tipo NatsMessageEvent (NATS),
             * extraemos su campo Message proveniente de NATS y lo
             * almacenamos en la variable 'msg'. Esto será útil para saber el id del cliente
             * NATS al que debo responderle con una excepción.
             */

            if (arg instanceof NatsMessageEvent natsMessageEvent) {
                messageEvent = natsMessageEvent;
                break;
            }
        }

        if (messageEvent.msg() == null) {
            throw new AppError("You can't use the @NatsHandler annotation in functions without Message args.",
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }

        try {

            // Ejecutar el método original
            return joinPoint.proceed();

        } catch (Exception e) {
            this.handleExceptions(messageEvent.msg(), e, messageEvent.subject(), joinPoint.getSignature().getName());
            return null;
        }

    }

    private void handleExceptions(Message msg, Exception ex, String subject,
            String methodName) {

        ApiError apiError;
        log.info("ERROR ENCONTRADO MAMAGUEVO");

        // Hacemos el casteo al tipo de excepción correspondiente
        if (ex instanceof AppServiceException) {
            AppServiceException appEx = (AppServiceException) ex;
            apiError = new ApiError("NATS CONTROLLER - " + subject, appEx.getMessage(),
                    appEx.getHttpStatus(), Instant.now().toString());

        } else if (ex instanceof JsonProcessingException) {
            apiError = new ApiError("NATS CONTROLLER - " + subject,
                    "Error processing JSON: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    Instant.now().toString());
        } else if (ex instanceof AppError) {
            AppError appEx = (AppError) ex;

            apiError = new ApiError("NATS CONTROLLER - " + subject, appEx.getMessage(), appEx.getStatus(),
                    Instant.now().toString());
        }

        else {
            apiError = new ApiError("NATS CONTROLLER - " + subject, ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    Instant.now().toString());
        }

        this.natsMessageProcessor.sendError(msg, apiError);
    }

}
