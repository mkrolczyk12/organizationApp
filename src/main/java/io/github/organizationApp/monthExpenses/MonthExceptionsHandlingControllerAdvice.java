package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
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
@RestControllerAdvice(annotations = MonthExceptionsProcessing.class)
@Order(2)
final class MonthExceptionsHandlingControllerAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MonthExceptionsHandlingControllerAdvice.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException exception, WebRequest request) {

        final String message = "the data provided is incorrect";
        logger.warn("captured NumberFormatException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
