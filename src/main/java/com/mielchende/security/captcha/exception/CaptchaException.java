package com.mielchende.security.captcha.exception;

/* Excepción base para los errores relacionados con el captcha */
public class CaptchaException extends RuntimeException {

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}