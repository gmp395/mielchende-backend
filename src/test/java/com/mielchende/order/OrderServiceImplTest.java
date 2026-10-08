package com.mielchende.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.order.dto.CreateOrderDto;
import com.mielchende.order.dto.OrderItemRequestDto;
import com.mielchende.order.dto.OrderResponseDto;
import com.mielchende.order.exception.ProductUnavailableException;
import com.mielchende.product.ProductEntity;
import com.mielchende.product.ProductRepository;
import com.mielchende.product.ProductStatus;
import com.mielchende.product.exception.ProductNotFoundException;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test unitario del servicio de solicitudes de pedido */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final UserEntity user = UserEntity.builder()
            .name("Ana").email("ana@mail.com").build();

    @Test
    void createsOrderWithItemsAndReceivedStatus() {
        /* Given: usuaria existente y dos productos disponibles */
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Miel de castaño", ProductStatus.AVAILABLE)));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product(2L, "Polen", ProductStatus.AVAILABLE)));
        /* save() devuelve la misma solicitud que recibe */
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderDto request = new CreateOrderDto(
                List.of(new OrderItemRequestDto(1L, 2), new OrderItemRequestDto(2L, 1)),
                "600000000", "Entrega por la tarde");

        /* When */
        OrderResponseDto result = orderService.create("ana@mail.com", request);

        /* Then: estado inicial RECEIVED y las dos líneas */
        assertThat(result.status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
        assertThat(result.customerEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    void throwsExceptionWhenAProductIsSoldOut() {
        /* Given: el producto está agotado */
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Polen", ProductStatus.SOLD_OUT)));

        CreateOrderDto request = new CreateOrderDto(
                List.of(new OrderItemRequestDto(1L, 1)), null, null);

        /* When + Then: excepción y no se guarda nada */
        assertThatThrownBy(() -> orderService.create("ana@mail.com", request))
                .isInstanceOf(ProductUnavailableException.class)
                .hasMessageContaining("agotado");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void throwsExceptionWhenAProductDoesNotExist() {
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderDto request = new CreateOrderDto(
                List.of(new OrderItemRequestDto(99L, 1)), null, null);

        assertThatThrownBy(() -> orderService.create("ana@mail.com", request))
                .isInstanceOf(ProductNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    /* Ayudante para crear productos de prueba */
    private ProductEntity product(Long id, String name, ProductStatus status) {
        return ProductEntity.builder()
                .id(id)
                .name(name)
                .description("Descripción de prueba")
                .format("500 g")
                .price(new BigDecimal("8.50"))
                .status(status)
                .build();
    }
}