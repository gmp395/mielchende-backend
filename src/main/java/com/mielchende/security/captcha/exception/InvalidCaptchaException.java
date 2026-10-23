package com.mielchende.security.captcha.exception;

/* Se lanza cuando el token del captcha no es válido o no se pudo verificar */
public class InvalidCaptchaException extends CaptchaException {

    public InvalidCaptchaException(String message) {
        super(message);
    }

    public InvalidCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}