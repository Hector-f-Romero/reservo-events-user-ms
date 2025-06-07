package com.hector.eventuserms.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ApiErrorBuilder {

    public static ApiError buildApiError(Exception exception, String errorPath) {
        String timestamp = Instant.now().toString();

        /*
         * Identify the type of exception and build the corresponding ApiError
         * object, including status code, error message, timestamp, and context.
         */
        return switch (exception) {
            case JsonProcessingException jsonEx -> new ApiError(
                    errorPath,
                    "Error processing JSON: " + jsonEx.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    timestamp);
            case IllegalArgumentException ex ->
                new ApiError(errorPath, ex.getMessage(), HttpStatus.BAD_REQUEST, timestamp);
            case MethodArgumentNotValidException ex -> {

                // Create an message that have all validation errors.
                StringBuilder errorMessage = new StringBuilder("Validation failed: ");

                List<String> validationErrors = new ArrayList<>();

                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                    validationErrors.add(error.getField() + ": " + error.getDefaultMessage());
                }

                errorMessage.append(String.join(", ", validationErrors));

                yield new ApiError(
                        errorPath,
                        errorMessage.toString(),
                        HttpStatus.BAD_REQUEST,
                        Instant.now().toString());

            }
            case MethodArgumentTypeMismatchException ex -> {
                // Obtener el nombre del parámetro que causó el error
                String paramName = ex.getName();

                // Obtener el tipo requerido, manejando el caso en que sea null
                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "a valid type";

                // Construir el mensaje de error
                String errorMessage = String.format("Parameter '%s' must be %s.", paramName, typeName);

                yield new ApiError(
                        errorPath,
                        errorMessage,
                        HttpStatus.BAD_REQUEST,
                        timestamp);
            }

            case HttpMessageNotReadableException ex ->
                new ApiError(errorPath, "Request cannot be deserialized: " + ex.getMessage(), HttpStatus.BAD_REQUEST,
                        timestamp);
            case AppError ex -> new ApiError(errorPath, ex.getMessage(), ex.getStatus(),
                    timestamp);
            default -> new ApiError(
                    errorPath,
                    exception.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    timestamp);
        };
    }
}
