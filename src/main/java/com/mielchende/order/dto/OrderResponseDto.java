package com.mielchende.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.mielchende.order.OrderStatus;

/* Solicitud de pedido tal como la devuelve la API.
   Incluye el nombre y el email de la clienta para que la admin pueda contactarla */
public record OrderResponseDto(
    Long id,
    String customerName,
    String customerEmail,
    String phone,
    String comments,
    OrderStatus status,
    LocalDateTime createdAt,
    List<OrderItemResponseDto> items
) { }