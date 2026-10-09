package com.mielchende.order.exception;

/* Se lanza cuando se busca una solicitud de pedido que no existe → 404 Not Found */
public class OrderNotFoundException extends OrderException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}