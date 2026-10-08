package com.mielchende.order.exception;

/* Se lanza al solicitar un producto agotado → 409 Conflict */
public class ProductUnavailableException extends OrderException {

    public ProductUnavailableException(String message) {
        super(message);
    }

    public ProductUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}