package com.mielchende.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mielchende.user.exception.UserAlreadyExistsException;

/* Captura las excepciones lanzadas en cualquier parte de la aplicación
   y devuelve respuestas JSON claras, en lugar de stacktraces. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* Email ya registrado → 409 Conflict */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    /* Datos inválidos en un DTO con @Valid (campo vacío, email mal escrito...) → 400 Bad Request.
       Devuelve el primer campo que ha fallado y el motivo */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().get(0);
        String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }

    /* Body que no es un JSON válido (falta un valor, sobra una coma...) → 400 Bad Request.
       Es un error del cliente, no del servidor */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "El cuerpo de la petición no es un JSON válido"));
    }

    /* Red de seguridad para cualquier error no previsto → 500.
       No se muestra el detalle interno al cliente */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Internal server error"));
    }
}