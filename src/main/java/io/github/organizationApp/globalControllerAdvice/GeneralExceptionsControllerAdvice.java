package io.github.organizationApp.globalControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice(annotations = GeneralExceptionsProcessing.class)
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
}
