package com.mielchende.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/* Test unitario de la carga inicial del catálogo */
@ExtendWith(MockitoExtension.class)
class ProductDataInitializerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductDataInitializer initializer;

    @Test
    void loadsCatalogWhenTableIsEmpty() {
        /* Given: tabla vacía */
        when(productRepository.count()).thenReturn(0L);

        initializer.run();

        /* Then: se guarda el catálogo */
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void doesNotLoadCatalogWhenProductsAlreadyExist() {
        /* Given: ya hay productos (p. ej., los que gestionó la admin) */
        when(productRepository.count()).thenReturn(5L);

        initializer.run();

        /* Then: no se toca nada */
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void initialCatalogIsCompleteAndValid() {
        /* When: se genera el catálogo inicial */
        List<ProductEntity> products = initializer.initialProducts();

        /* Then: los 9 productos reales */
        assertThat(products).hasSize(9);

        /* Todos tienen los datos obligatorios de la entidad, y su foto */
        assertThat(products).allSatisfy(product -> {
            assertThat(product.getName()).isNotBlank();
            assertThat(product.getDescription()).isNotBlank();
            assertThat(product.getFormat()).isNotBlank();
            assertThat(product.getCategory()).isNotNull();
            assertThat(product.getStatus()).isNotNull();
            assertThat(product.getImageUrl()).startsWith("/images/products/");
        });

        /* Las mieles van primero (4 formatos) */
        assertThat(products.subList(0, 4))
                .allSatisfy(product -> assertThat(product.getCategory()).isEqualTo(ProductCategory.HONEY));

        /* Los precios "a consultar" son exactamente propóleo, reina y enjambre */
        assertThat(products)
                .filteredOn(product -> product.getPrice() == null)
                .extracting(ProductEntity::getName)
                .containsExactlyInAnyOrder("Tintura de propóleo", "Reina", "Enjambre");
    }
}