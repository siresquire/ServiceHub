package com.servicehub.exception;

import com.servicehub.dto.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ServerResponse<?>> handleEnumError(MethodArgumentTypeMismatchException ex) {

        log.warn("Enum conversion error: value='{}', requiredType='{}'",
                ex.getValue(),
                ex.getRequiredType());

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            return ResponseEntity.badRequest()
                    .body(new ServerResponse<>("Invalid value for enum: " + ex.getValue()));
        }

        return ResponseEntity.badRequest().body(new ServerResponse<>("Invalid request parameter"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServerResponse<?>> handleValidationError(MethodArgumentNotValidException ex) {

        log.warn("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("Field '%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();

        String message = errors.isEmpty()
                ? "Validation failed for request body"
                : String.join("; ", errors);

        return ResponseEntity
                .badRequest()
                .body(new ServerResponse<>(message));
    }

    @ExceptionHandler(InvalidServiceRequestTransition.class)
    public ResponseEntity<ServerResponse<?>> handleInvalidServiceRequestTransition(InvalidServiceRequestTransition ex) {

        log.warn("Invalid service request transition: {}", ex.getMessage());

        return ResponseEntity
                .unprocessableEntity()
                .body(new ServerResponse<>(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", 404,
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {

        log.warn("Authentication failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", 401,
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        log.error("Runtime exception occurred {} {}", ex.getClass().getName(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        log.error("Unexpected system error, {} {}", ex.getClass().getName() ,ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "message", "Something went wrong",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}