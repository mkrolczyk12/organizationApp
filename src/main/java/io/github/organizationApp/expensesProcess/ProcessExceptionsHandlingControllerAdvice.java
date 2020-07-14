package io.github.organizationApp.expensesProcess;

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
import java.util.NoSuchElementException;

/**
 * For individual package exceptions
 */
@RestControllerAdvice(annotations = ProcessExceptionsProcessing.class)
@Order(2)
final class ProcessExceptionsHandlingControllerAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProcessExceptionsHandlingControllerAdvice.class);

    @ExceptionHandler(NoSuchElementException.class)
    public final ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NumberFormatException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
