package synq_backend.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import synq_backend.common.exception.InvalidMessageOperationException;


import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles requests for resources that do not exist.
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handlerResourceNotFound(ResourceNotFoundException ex) {
        return Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        );
    }

    // Handles duplicate username or email errors.
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return Map.of(
                "error", "CONFLICT",
                "message", "Username or email already exists"
        );
    }

    // Handles invalid login credentials.
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException ex){
        return Map.of(
              "error", "UNAUTHORIZED",
              "message", ex.getMessage()
        );
    }

    // Handles invalid operations performed on messages.
    @ExceptionHandler(InvalidMessageOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidMessageOperation(
            InvalidMessageOperationException ex) {

        return Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        );
    }
}