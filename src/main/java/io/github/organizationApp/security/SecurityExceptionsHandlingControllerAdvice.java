package io.github.organizationApp.security;

import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * For individual package exceptions
 */
@RestControllerAdvice(annotations = SecurityExceptionsProcessing.class)
@Order(1)
final class SecurityExceptionsHandlingControllerAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionsHandlingControllerAdvice.class);

    @ExceptionHandler(NullPointerException.class)
    public final ResponseEntity<Object> handleNullPointerException(NullPointerException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NullPointerException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
