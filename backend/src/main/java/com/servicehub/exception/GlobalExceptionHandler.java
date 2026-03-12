package com.servicehub.exception;

import com.servicehub.dto.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
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

    @ExceptionHandler(InvalidServiceRequestTransition.class)
    public ResponseEntity<ServerResponse<?>> handleInvalidServiceRequestTransition(InvalidServiceRequestTransition ex) {

        log.warn("Invalid service request transition: {}", ex.getMessage());

        return ResponseEntity
                .unprocessableEntity()
                .body(new ServerResponse<>(ex.getMessage()));
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

        log.error("Runtime exception occurred", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        log.error("Unexpected system error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "message", "Something went wrong",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
