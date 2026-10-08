package com.mielchende.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderItemRequestDto;
import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.dto.OrderSummaryDto;
import com.mielchende.order.exception.ProductUnavailableException;
import com.mielchende.product.ProductEntity;
import com.mielchende.product.ProductRepository;
import com.mielchende.product.ProductStatus;
import com.mielchende.product.exception.ProductNotFoundException;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Lógica de las solicitudes de pedido */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /* Crea una solicitud con todas sus líneas.
       @Transactional: si alguna línea falla, no se guarda nada */
    @Override
    @Transactional
    public OrderResponseDto create(String userEmail, CreateOrderDto request) {

        /* 1. La usuaria sale del token, así que debería existir siempre */
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        /* 2. La solicitud nace siempre como RECEIVED (Recibido) */
        OrderEntity order = OrderEntity.builder()
                .user(user)
                .phone(request.phone())
                .comments(request.comments())
                .status(OrderStatus.RECEIVED)
                .build();

        /* 3. Cada línea: el producto debe existir y estar disponible */
        for (OrderItemRequestDto line : request.items()) {
            ProductEntity product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

            if (product.getStatus() == ProductStatus.SOLD_OUT) {
                throw new ProductUnavailableException(
                        product.getName() + " (" + product.getFormat()
                        + ") está agotado hasta la próxima cosecha");
            }

            order.addItem(OrderItemEntity.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .build());
        }

        /* 4. Al guardar la solicitud, las líneas se guardan en cascada */
        return OrderMapper.toResponseDto(orderRepository.save(order));
    }

    /* Todas las solicitudes, de la más reciente a la más antigua (MC-31).
       readOnly: solo lectura; mantiene la conexión abierta para cargar las líneas */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderMapper::toResponseDto)
                .toList();
    }

    /* Resumen del panel: número de solicitudes pendientes (RECEIVED) */
    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDto getSummary() {
        return new OrderSummaryDto(orderRepository.countByStatus(OrderStatus.RECEIVED));
    }
}