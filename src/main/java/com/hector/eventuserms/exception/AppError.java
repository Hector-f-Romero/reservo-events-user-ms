package com.hector.eventuserms.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AppError extends RuntimeException {

    private final HttpStatus status;

    public AppError(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public AppError(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AppError(Throwable cause) {
        super(cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
