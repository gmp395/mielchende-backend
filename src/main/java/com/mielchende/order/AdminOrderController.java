package com.mielchende.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.dto.OrderStatusRequestDto;
import com.mielchende.order.dto.OrderSummaryDto;

import jakarta.validation.Valid;

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

    /* PATCH /api/admin/orders/{id}/status → cambia solo el estado (200 OK).
       Cuerpo: { "status": "CONFIRMED" }
       - Solicitud inexistente → 404
       - Cambio no permitido (saltar un paso o mismo estado) → 409
       - Estado vacío o desconocido → 400 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody OrderStatusRequestDto request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}