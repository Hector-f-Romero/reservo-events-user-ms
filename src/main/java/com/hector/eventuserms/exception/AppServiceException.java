package com.hector.eventuserms.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AppServiceException extends RuntimeException {

    public final HttpStatus httpStatus;

    public AppServiceException(HttpStatus status, String message) {
        super(message);
        this.httpStatus = status;
    }
}
