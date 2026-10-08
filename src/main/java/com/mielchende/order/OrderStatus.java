package com.mielchende.order;

/* Ciclo de vida de una solicitud de pedido:
   RECEIVED (Recibido) → CONFIRMED (Confirmado / en preparación) → SHIPPED (Enviado) */
public enum OrderStatus {
    RECEIVED,
    CONFIRMED,
    SHIPPED
}