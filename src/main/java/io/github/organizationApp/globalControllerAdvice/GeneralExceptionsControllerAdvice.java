package io.github.organizationApp.globalControllerAdvice;

import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

/**
 * General exceptions handler
 */
@RestControllerAdvice(annotations = GeneralExceptionsProcessing.class)
@Order(3)
public final class GeneralExceptionsControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(GeneralExceptionsControllerAdvice.class);

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<Object> handleRuntimeException(RuntimeException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured RuntimeException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured IllegalArgumentException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NullPointerException.class)
    public final ResponseEntity<Object> handleNullPointerException(NullPointerException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NullPointerException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(NumberFormatException.class)
    public final ResponseEntity<Object> handleNumberFormatException(NumberFormatException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NumberFormatException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IllegalStateException.class)
    public final ResponseEntity<Object> handleIllegalStateException(IllegalStateException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured IllegalStateException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured ConstraintViolationException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception, WebRequest request) {

        logger.warn("captured HttpMessageNotReadableException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "Wrong message", request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public final ResponseEntity<Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured HttpMediaTypeNotSupportedException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public final ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured HttpRequestMethodNotSupportedException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured MethodArgumentNotValidException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public final ResponseEntity<Object> handleUnsatisfiedServletRequestParameterException(UnsatisfiedServletRequestParameterException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured UnsatisfiedServletRequestParameterException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public final ResponseEntity<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured MissingServletRequestParameterException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BindException.class)
    public final ResponseEntity<Object> handleBindException(BindException exception, WebRequest request) {

        final String message = "body validation error: " + exception.getFieldError();
        logger.warn("captured BindException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MissingPathVariableException.class)
    public final ResponseEntity<Object> handleMissingPathVariableException(MissingPathVariableException exception, WebRequest request) {

        final String message = "missing URI template variable '"+ exception.getVariableName() + "' method parameter";
        logger.warn("captured MissingPathVariableException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ClassCastException.class)
    public final ResponseEntity<Object> handleClassCastException(ClassCastException exception, WebRequest request) {

        final String message = "The wrong data type was specified";
        logger.warn("captured ClassCastException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PropertyReferenceException.class)
    public final ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException exception, WebRequest request) {

        final String message = "invalid URI '" + exception.getPropertyName() + "' property";
        logger.warn("captured PropertyReferenceException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NoSuchElementException.class)
    public final ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NoSuchElementException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request) {

        final String message = exception.getMessage();
        logger.warn("captured NoSuchElementException: " + message);
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IOException.class)
    public final ResponseEntity<Object> handleIOException(IOException exception, WebRequest request) {

        final String message = "an error occurred while working with data";
        logger.warn("captured IOException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(IllegalAccessException.class)
    public final ResponseEntity<Object> handleIllegalAccessException(IllegalAccessException exception, WebRequest request) {

        final String message = "The client has not been recognized";
        logger.warn("captured IllegalAccessException: " + exception.getMessage());
        ExceptionResponse response =
                new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
