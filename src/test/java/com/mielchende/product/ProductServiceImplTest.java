package com.mielchende.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.product.dto.ProductResponseDto;
import com.mielchende.product.exception.ProductNotFoundException;

/* Test unitario del servicio de productos */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findAllReturnsProductsAsDtos() {
        /* Given: dos productos, uno agotado */
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "Miel de castaño", ProductStatus.AVAILABLE),
                product(2L, "Polen", ProductStatus.SOLD_OUT)));

        /* When */
        List<ProductResponseDto> result = productService.findAll();

        /* Then: se devuelven los dos, con su estado */
        assertThat(result).hasSize(2);
        assertThat(result.get(1).status()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void findByIdReturnsProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Miel de castaño", ProductStatus.AVAILABLE)));

        ProductResponseDto result = productService.findById(1L);

        assertThat(result.name()).isEqualTo("Miel de castaño");
    }

    @Test
    void findByIdThrowsExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
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