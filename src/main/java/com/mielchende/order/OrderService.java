package com.mielchende.order;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderResponseDto;

/* Contrato del servicio de solicitudes de pedido */
public interface OrderService {

    OrderResponseDto create(String userEmail, CreateOrderDto request);
}