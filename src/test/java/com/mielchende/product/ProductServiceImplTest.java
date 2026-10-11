package com.mielchende.product;

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

import com.mielchende.product.dto.ProductRequestDto;
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
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "Miel de castaño", ProductStatus.AVAILABLE),
                product(2L, "Polen", ProductStatus.SOLD_OUT)));

        List<ProductResponseDto> result = productService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).status()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void findByIdReturnsProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Miel de castaño", ProductStatus.AVAILABLE)));

        assertThat(productService.findById(1L).name()).isEqualTo("Miel de castaño");
    }

    @Test
    void findByIdThrowsExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createSavesAndReturnsProduct() {
        /* Given: save() devuelve el producto con un id asignado */
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity entity = invocation.getArgument(0);
            entity.setId(5L);
            return entity;
        });

        /* When */
        ProductResponseDto result = productService.create(request("Cera", ProductStatus.AVAILABLE));

        /* Then */
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Cera");
    }

    @Test
    void updateThrowsExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, request("Cera", ProductStatus.AVAILABLE)))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStatusChangesOnlyTheStatus() {
        /* Given: producto disponible */
        ProductEntity existing = product(1L, "Miel de castaño", ProductStatus.AVAILABLE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        /* When: se marca como agotado */
        ProductResponseDto result = productService.updateStatus(1L, ProductStatus.SOLD_OUT);

        /* Then: cambia el estado y el resto de datos se mantiene */
        assertThat(result.status()).isEqualTo(ProductStatus.SOLD_OUT);
        assertThat(result.name()).isEqualTo("Miel de castaño");
    }

    @Test
    void deleteThrowsExceptionWhenProductDoesNotExist() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).deleteById(any());
    }

    /* Ayudantes para crear datos de prueba */
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

    private ProductRequestDto request(String name, ProductStatus status) {
        return new ProductRequestDto(name, "Descripción de prueba", "500 g",
                new BigDecimal("8.50"), null, null, status);
    }
}