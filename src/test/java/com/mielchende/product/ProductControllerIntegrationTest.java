package com.mielchende.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.mielchende.TestcontainersConfiguration;

/* Test de integración del catálogo público, contra MySQL real.
   Ninguna petición lleva token: son rutas públicas */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private Long savedId;

    /* Antes de cada test: tabla limpia con un producto disponible y otro agotado */
    @BeforeEach
    void createProducts() {
        productRepository.deleteAll();
        savedId = productRepository.save(product("Miel de castaño", ProductStatus.AVAILABLE)).getId();
        productRepository.save(product("Polen", ProductStatus.SOLD_OUT));
    }

    @Test
    void catalogIsPublicAndReturnsAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void productDetailIsPublic() throws Exception {
        mockMvc.perform(get("/api/products/" + savedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Miel de castaño"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void unknownProductReturns404WithMessage() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }

    private ProductEntity product(String name, ProductStatus status) {
        return ProductEntity.builder()
                .name(name)
                .description("Descripción de prueba")
                .format("500 g")
                .price(new BigDecimal("8.50"))
                .status(status)
                .build();
    }
}