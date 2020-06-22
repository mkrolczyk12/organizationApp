package io.github.organizationApp.categoryExpenses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * For individual package exceptions
 */
@RestControllerAdvice(annotations = ExceptionsProcessing.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
final class CategoryExceptionsHandlingControllerAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CategoryExceptionsHandlingControllerAdvice.class);
}
