package com.mielchende.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}