package com.mielchende.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.dto.OrderSummaryDto;

/* Solicitudes de pedido vistas por la administradora.
   Rutas /api/admin/** → solo ROLE_ADMIN (SecurityConfig) */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* GET /api/admin/orders → listado, de la más reciente a la más antigua */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    /* GET /api/admin/orders/summary → contador de pendientes para el panel */
    @GetMapping("/summary")
    public ResponseEntity<OrderSummaryDto> getSummary() {
        return ResponseEntity.ok(orderService.getSummary());
    }
}