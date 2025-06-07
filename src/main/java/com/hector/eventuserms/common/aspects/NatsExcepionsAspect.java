package com.hector.eventuserms.common.aspects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.hector.eventuserms.common.nats.NatsMessage;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.exception.ApiError;
import com.hector.eventuserms.exception.ApiErrorBuilder;
import com.hector.eventuserms.exception.AppError;

import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/* Aspect responsible for intercepting functions annotated with @NatsHandler
 * to centrally handle exceptions thrown during NATS-related logic execution.
 */
@Aspect
@Component
@Slf4j
public class NatsExcepionsAspect {

    private final NatsMessageProcessor natsMessageProcessor;

    public NatsExcepionsAspect(NatsMessageProcessor natsMessageProcessor) {
        log.info("Se ha creado el aspecto de Nats Exceptions correctamente.");
        this.natsMessageProcessor = natsMessageProcessor;
    }

    /**
     * Defines a Pointcut to intercept methods annotated with @NatsHandler across
     * the app.
     */
    @Pointcut("@annotation(com.hector.eventuserms.common.annotations.NatsHandler)")
    public void natsHandlerMethods() {
    }

    /**
     * Around the Pointcut, defines logic to catch exceptions,
     * extract the original NATS message, and delegate error handling.
     */
    @Around("natsHandlerMethods()")
    public Object handleNatsExceptions(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. Get the arguments for the function intercepted.
        Object[] args = joinPoint.getArgs();
        NatsMessage messageEvent = null;

        /*
         * 2. If we find an argument of type NatsMessageEvent, we assign it to
         * 'messageEvent'.
         * This gives us access to the original NATS Message, which includes metadata
         * like
         * the reply subject or client ID. This is crucial for sending error responses
         * back to the appropriate client.
         */
        for (Object arg : args) {
            if (arg instanceof NatsMessage natsMessage) {
                messageEvent = natsMessage;
                break;
            }
        }

        // 3. If the event is not present or invalid, throw a critical internal
        // exception.
        if (messageEvent == null || messageEvent.msg == null) {
            throw new AppError(
                    "You can't use the @NatsHandler annotation on methods that not receive a NatsMessageEvent.",
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }
        try {

            // 4. Proceed with the original method execution.
            return joinPoint.proceed();

        } catch (Exception e) {
            // 5. If an exception occurs, handle and respond through NATS.
            this.handleExceptions(messageEvent.msg, e, messageEvent.subject,
                    joinPoint.getSignature().getName());
            return null;
        }

    }

    private void handleExceptions(Message msg, Exception ex, String subject,
            String methodName) {

        log.info("An error was intercepted and is being processed by the NATS exception handler.");

        log.info(ex.getClass().getName());

        // 1. Create a standard object to show error information.
        ApiError apiError = ApiErrorBuilder.buildApiError(ex, "NATS CONTROLLER - Error in subject: " + subject);

        // 2. Send the formatted error response back to the NATS client.
        this.natsMessageProcessor.sendError(msg, apiError);
    }

}
