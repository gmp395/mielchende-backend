package com.mielchende.order;

import java.util.List;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.dto.OrderSummaryDto;

/* Contrato del servicio de solicitudes de pedido */
public interface OrderService {

    OrderResponseDto create(String userEmail, CreateOrderDto request);

    List<OrderResponseDto> findAll();

    OrderSummaryDto getSummary();
}