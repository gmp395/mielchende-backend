package com.mielchende.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/* Test unitario de la regla de cambio de estado (OrderStatus.canChangeTo).
   No necesita Spring ni base de datos: solo prueba la lógica del enum */
class OrderStatusTest {

    /* Avanzar un paso: permitido */
    @Test
    void canMoveOneStepForward() {
        assertThat(OrderStatus.RECEIVED.canChangeTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canChangeTo(OrderStatus.SHIPPED)).isTrue();
    }

    /* Retroceder un paso para corregir un error: permitido */
    @Test
    void canMoveOneStepBack() {
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canChangeTo(OrderStatus.RECEIVED)).isTrue();
    }

    /* Saltar un paso, en cualquier dirección: no permitido */
    @Test
    void cannotSkipAStep() {
        assertThat(OrderStatus.RECEIVED.canChangeTo(OrderStatus.SHIPPED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.RECEIVED)).isFalse();
    }

    /* "Cambiar" al mismo estado: no permitido, porque no es un cambio */
    @Test
    void cannotChangeToSameStatus() {
        assertThat(OrderStatus.RECEIVED.canChangeTo(OrderStatus.RECEIVED)).isFalse();
        assertThat(OrderStatus.CONFIRMED.canChangeTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.SHIPPED)).isFalse();
    }
}