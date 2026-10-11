package com.mielchende.order;

import java.util.List;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.dto.OrderSummaryDto;

/* Contrato del servicio de solicitudes de pedido */
public interface OrderService {

    OrderResponseDto create(String userEmail, CreateOrderDto request);

    /* Solicitudes de una clienta concreta, identificada por el email del token */
    List<OrderResponseDto> findByUserEmail(String userEmail);

    List<OrderResponseDto> findAll();

    OrderSummaryDto getSummary();

    /* Cambia el estado de una solicitud, respetando la regla de un paso adelante o atrás */
    OrderResponseDto updateStatus(Long orderId, OrderStatus newStatus);
}