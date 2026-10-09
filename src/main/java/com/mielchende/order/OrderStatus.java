package com.mielchende.order;

/* Ciclo de vida de una solicitud de pedido:
   RECEIVED (Recibido) → CONFIRMED (Confirmado / en preparación) → SHIPPED (Enviado)

   IMPORTANTE: el orden en que se declaran los valores define el ciclo.
   canChangeTo() se basa en ese orden, así que no hay que reordenarlos */
public enum OrderStatus {
    RECEIVED,
    CONFIRMED,
    SHIPPED;

    /* Regla de negocio: solo se puede avanzar o retroceder UN paso.
       - Avanzar: RECEIVED → CONFIRMED → SHIPPED
       - Retroceder para corregir un error: SHIPPED → CONFIRMED → RECEIVED
       - No se permite saltar (RECEIVED ↔ SHIPPED) ni "cambiar" al mismo estado.

       ordinal() devuelve la posición del valor en la declaración (0, 1, 2).
       Math.abs() da la distancia sin importar la dirección:
       un cambio es válido si la distancia es exactamente 1 */
    public boolean canChangeTo(OrderStatus target) {
        return Math.abs(this.ordinal() - target.ordinal()) == 1;
    }
}