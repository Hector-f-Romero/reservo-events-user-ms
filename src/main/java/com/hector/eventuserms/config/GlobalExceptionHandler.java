package com.hector.eventuserms.config;

import java.sql.SQLException;

import org.hibernate.TypeMismatchException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.hector.eventuserms.exception.ApiError;
import com.hector.eventuserms.exception.ApiErrorBuilder;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);

        }

        // Handles MethodArgumentTypeMismatchException which occurs when a request
        // parameter cannot be converted to the required type (e.g., when a UUID is
        // invalid).
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatchException(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handles HttpMessageNotReadableException which occurs when the request body
        // cannot be deserialized to the expected Java type.
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handles TypeMismatchException which is the base class for data type
        // conversion errors in Spring.
        @ExceptionHandler(TypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatchException(TypeNotPresentException ex,
                        HttpServletRequest request) {

                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);

        }

        @ExceptionHandler({
                        ConstraintViolationException.class,
                        DataAccessException.class,
                        SQLException.class,
                        PersistenceException.class })
        public ResponseEntity<ApiError> handleDatabaseErrors(Exception ex, HttpServletRequest request) {
                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handles any exception not caught by other specific handlers. This is the last
        // resort for unexpected errors.
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
                ApiError apiError = ApiErrorBuilder.buildApiError(ex, request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

}
