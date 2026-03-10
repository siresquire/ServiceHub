package com.servicehub.exception;

import com.servicehub.dto.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ServerResponse<?>> handleEnumError(MethodArgumentTypeMismatchException ex) {

    if (ex.getRequiredType().isEnum()) {
      return ResponseEntity.badRequest()
              .body(new ServerResponse<>("Invalid value for enum: " + ex.getValue()));
    }

    return ResponseEntity.badRequest().build();
  }

}
