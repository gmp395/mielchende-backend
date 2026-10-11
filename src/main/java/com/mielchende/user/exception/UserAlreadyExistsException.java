package com.mielchende.user.exception;

/* Se lanza al intentar registrar un email que ya existe */
public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}