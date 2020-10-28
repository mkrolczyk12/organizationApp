package io.github.organizationApp;

import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.security.SecurityExceptionsProcessing;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@CrossOrigin
@GeneralExceptionsProcessing
@SecurityExceptionsProcessing
public class DefaultErrorController implements ErrorController {
    @RequestMapping("/error")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExceptionResponse> handleError(HttpServletRequest request) {
        final String message = "service does not support given URI";
        var response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "given request type: " + request.getMethod());
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getErrorPath() {return null;}
}
