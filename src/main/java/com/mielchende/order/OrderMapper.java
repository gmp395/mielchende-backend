package com.mielchende.order;

import com.mielchende.order.dto.OrderItemResponseDto;
import com.mielchende.order.dto.OrderResponseDto;

/* Traduce OrderEntity (con sus líneas) al DTO de respuesta */
public class OrderMapper {

    /* Clase de utilidad: solo métodos estáticos */
    private OrderMapper() { }

    public static OrderResponseDto toResponseDto(OrderEntity order) {
        /* Cada línea se convierte en su propio DTO */
        var items = order.getItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getFormat(),
                        item.getQuantity()))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getPhone(),
                order.getComments(),
                order.getStatus(),
                order.getCreatedAt(),
                items);
    }
}