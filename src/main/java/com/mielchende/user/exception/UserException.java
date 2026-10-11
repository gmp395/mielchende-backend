package com.mielchende.user.exception;

/* Excepción base para los errores relacionados con usuarios.
   Extiende RuntimeException, como en los apuntes (sección 9). */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}