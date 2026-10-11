package com.mielchende.product.exception;

/* Excepción base para los errores relacionados con productos */
public class ProductException extends RuntimeException {

    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }
}