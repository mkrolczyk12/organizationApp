package io.github.organizationApp.globalControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("captured IllegalArgumentException");
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException e) {
        logger.warn("captured handleNullPointerException");
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        logger.warn("captured handleIllegalStateException");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        logger.warn("captured ConstraintViolationException");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        logger.warn("captured HttpMessageNotReadableException");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        logger.warn("captured HHttpMediaTypeNotSupportedException: request content type not supported");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        logger.warn("captured HttpRequestMethodNotSupportedException: given request method is not implemented");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        logger.warn("captured MethodArgumentNotValidException: failed request validation");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<?> handleUnsatisfiedServletRequestParameter(final UnsatisfiedServletRequestParameterException e) {
        logger.warn("captured UnsatisfiedServletRequestParameterException: Parameter conditions not met for actual request parameters");
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
        logger.warn("captured ClassCastException: request body validation failed");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> handlePropertyReference(final PropertyReferenceException e) {
        logger.warn("captured PropertyReferenceException: invalid " + e.getPropertyName() + " property");
        return ResponseEntity.badRequest().build();
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElement(final NoSuchElementException e) {
        logger.warn("captured NoSuchElementException: no value present");
        return ResponseEntity.badRequest().build();
    }
}
