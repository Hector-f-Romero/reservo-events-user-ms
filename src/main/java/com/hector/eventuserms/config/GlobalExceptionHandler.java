package com.hector.eventuserms.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.hector.eventuserms.exception.ApiError;
import com.hector.eventuserms.exception.AppServiceException;
import com.hector.eventuserms.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AppServiceException.class)
        public ResponseEntity<ApiError> hanldeServiceException(AppServiceException ex, HttpServletRequest request) {

                ApiError apiError = new ApiError(request.getRequestURI(), ex.getMessage(), ex.getHttpStatus().value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, ex.getHttpStatus());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                // Crear un mensaje que contenga todos los errores de validación
                StringBuilder errorMessage = new StringBuilder("Validation failed: ");

                List<String> validationErrors = new ArrayList<>();

                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        validationErrors.add(error.getField() + ": " + error.getDefaultMessage());
                }

                errorMessage.append(String.join(", ", validationErrors));

                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                errorMessage.toString(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {

                ApiError apiError = new ApiError(request.getRequestURI(), ex.getMessage(), HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                ApiError apiError = new ApiError(request.getRequestURI(), ex.getMessage(), HttpStatus.CONFLICT.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);

        }

        // Handles MethodArgumentTypeMismatchException which occurs when a request
        // parameter cannot be converted to the required type (e.g., when a UUID is
        // invalid).
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatchException(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                // Obtener el nombre del parámetro que causó el error
                String paramName = ex.getName();

                // Obtener el tipo requerido, manejando el caso en que sea null
                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "a valid type";

                // Construir el mensaje de error
                String errorMessage = String.format("Parameter '%s' must be %s.", paramName, typeName);

                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                errorMessage,
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handles HttpMessageNotReadableException which occurs when the request body
        // cannot be deserialized to the expected Java type.
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                String errorMessage = "Error procesing input JSON: " + ex.getMessage();

                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                errorMessage,
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handles TypeMismatchException which is the base class for data type
        // conversion errors in Spring.
        @ExceptionHandler(TypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatchException(TypeNotPresentException ex,
                        HttpServletRequest request) {
                String errorMessage = "Type error: " + ex.getMessage();

                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                errorMessage,
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);

        }

        // This handler processes exceptions explicitly thrown with a custom HTTP code
        // and message.
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex,
                        HttpServletRequest request) {
                ApiError apiError = new ApiError(request.getRequestURI(), ex.getReason(), ex.getStatusCode().value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, ex.getStatusCode());
        }

        // Handles any exception not caught by other specific handlers. This is the last
        // resort for unexpected errors.
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                "Server error: " + ex.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now());

                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

}
