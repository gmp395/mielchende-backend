package com.mielchende.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/* Una línea de la solicitud que envía la clienta: qué producto y cuántas unidades */
public record OrderItemRequestDto(

    @NotNull
    Long productId,

    /* Al menos una unidad */
    @NotNull
    @Min(1)
    Integer quantity
) { }