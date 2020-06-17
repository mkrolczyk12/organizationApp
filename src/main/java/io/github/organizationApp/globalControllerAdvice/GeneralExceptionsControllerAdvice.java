package io.github.organizationApp.globalControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

@RestControllerAdvice(annotations = GeneralExceptionsProcessing.class)
public class GeneralExceptionsControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(GeneralExceptionsControllerAdvice.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        logger.warn("captured RuntimeException: " + e.getMessage());
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("captured IllegalArgumentException: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
    }
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException e) {
        logger.warn("captured handleNullPointerException: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        logger.warn("captured handleIllegalStateException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        logger.warn("captured ConstraintViolationException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        logger.warn("captured HttpMessageNotReadableException: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Message");
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        logger.warn("captured HHttpMediaTypeNotSupportedException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        logger.warn("captured HttpRequestMethodNotSupportedException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        logger.warn("captured MethodArgumentNotValidException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<?> handleUnsatisfiedServletRequestParameter(final UnsatisfiedServletRequestParameterException e) {
        logger.warn("captured UnsatisfiedServletRequestParameterException: " + e.getMessage());
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(final MissingServletRequestParameterException e) {
        logger.warn("captured MissingServletRequestParameterException: required " + e.getParameterType() + " parameter " + "'" + e.getParameterName() + "'" + " is not present");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBind(final BindException e) {
        logger.warn("captured BindException: body validation error: " + e.getFieldError());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariable(final MissingPathVariableException e) {
        logger.warn("captured MissingPathVariableException: Missing URI template variable '"+ e.getVariableName() + "' method parameter");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<?> handleClassCast(final ClassCastException e) {
        logger.warn("captured ClassCastException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> handlePropertyReference(final PropertyReferenceException e) {
        logger.warn("captured PropertyReferenceException: invalid " + e.getPropertyName() + " property");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElement(final NoSuchElementException e) {
        logger.warn("captured NoSuchElementException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}
