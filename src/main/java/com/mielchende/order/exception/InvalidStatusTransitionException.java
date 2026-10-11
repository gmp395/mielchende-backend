package com.mielchende.order.exception;

/* Se lanza cuando se intenta un cambio de estado no permitido
   (saltar un paso o "cambiar" al mismo estado) → 409 Conflict.
   "Transition" (transición) es el nombre habitual para el paso de un estado a otro */
public class InvalidStatusTransitionException extends OrderException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}