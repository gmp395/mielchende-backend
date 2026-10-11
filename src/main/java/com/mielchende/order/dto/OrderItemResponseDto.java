package com.mielchende.order.dto;

/* Una línea de la solicitud tal como la devuelve la API */
public record OrderItemResponseDto(
    Long productId,
    String productName,
    String productFormat,
    Integer quantity
) { }