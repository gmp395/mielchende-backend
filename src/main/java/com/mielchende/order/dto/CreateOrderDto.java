package com.mielchende.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/* Datos que envía la clienta para crear una solicitud */
public record CreateOrderDto(

    /* Al menos una línea. @Valid hace que también se validen
       las anotaciones de cada línea (productId, quantity) */
    @NotEmpty
    @Valid
    List<OrderItemRequestDto> items,

    /* Teléfono de contacto (opcional) */
    @Size(max = 20)
    String phone,

    /* Comentarios (opcional), con un límite razonable */
    @Size(max = 1000)
    String comments
) { }