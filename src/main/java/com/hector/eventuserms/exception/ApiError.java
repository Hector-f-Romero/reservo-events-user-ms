package com.hector.eventuserms.exception;

import org.springframework.http.HttpStatus;

public record ApiError(
                String path,
                String message,
                HttpStatus status,
                String timestamp) {

}
