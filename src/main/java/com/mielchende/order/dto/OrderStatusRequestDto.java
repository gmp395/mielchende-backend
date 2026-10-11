package com.mielchende.order.dto;

import com.mielchende.order.OrderStatus;

import jakarta.validation.constraints.NotNull;

/* Cuerpo del PATCH para cambiar el estado de una solicitud.
   - Sin "status" → 400 (@NotNull)
   - Un valor que no sea RECEIVED, CONFIRMED ni SHIPPED → 400 (JSON no válido) */
public record OrderStatusRequestDto(
    @NotNull
    OrderStatus status
) { }