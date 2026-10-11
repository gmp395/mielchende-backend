package com.mielchende.order.exception;

/* Excepción base para los errores relacionados con solicitudes de pedido */
public class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}