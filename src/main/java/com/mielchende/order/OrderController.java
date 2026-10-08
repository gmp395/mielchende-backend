package com.mielchende.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderResponseDto;

import jakarta.validation.Valid;

/* Solicitudes de pedido de las clientas. Ruta protegida: solo ROLE_USER */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* POST /api/orders → crea la solicitud (201 Created).
       authentication.getName() devuelve el "subject" del token JWT, que es el email.
       Así la solicitud se asocia a quien tiene la sesión iniciada,
       sin que la clienta pueda hacerse pasar por otra persona */
    @PostMapping
    public ResponseEntity<OrderResponseDto> create(Authentication authentication,
                                                   @Valid @RequestBody CreateOrderDto request) {
        OrderResponseDto response = orderService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}