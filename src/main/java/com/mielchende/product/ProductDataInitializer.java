package com.mielchende.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/* Carga el catálogo de Apícola Chende al arrancar, SOLO si la tabla está vacía.
   Así no se pisan los cambios que haga la admin desde el panel.
   PROVISIONAL: precios y algunos formatos pendientes de confirmar con José Antonio */
@Component
@Order(3)
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(initialProducts());
    }

    /* Catálogo inicial: cada formato de miel es un producto distinto */
    List<ProductEntity> initialProducts() {
        return List.of(
            product("Miel de castaño", "Miel de castaño de nuestra cosecha anual.", "500 g", "8.00", null),
            product("Miel de castaño", "Miel de castaño de nuestra cosecha anual.", "1 kg", "15.00", null),
            product("Miel milflores", "Miel milflores de nuestra cosecha anual.", "500 g", "7.00", null),
            product("Miel milflores", "Miel milflores de nuestra cosecha anual.", "1 kg", "13.00", null),
            product("Polen", "Polen recogido por nuestras abejas.", "250 g", "8.00", null),
            product("Propóleo", "Propóleo de nuestras colmenas.", "Unidad", "6.00", null),
            product("Cera", "Cera de abeja de nuestras colmenas.", "Unidad", "5.00", null),
            product("Enjambre", "Enjambre de abejas.", "Unidad", "120.00",
                    "Consultar disponibilidad en temporada"),
            product("Reina fecundada", "Reina fecundada.", "Unidad", "30.00",
                    "Consultar disponibilidad en temporada"),
            product("Reina virgen", "Reina virgen.", "Unidad", "15.00",
                    "Consultar disponibilidad en temporada"),
            product("Núcleo", "Núcleo de abejas.", "Unidad", "90.00",
                    "Consultar disponibilidad en temporada")
        );
    }

    /* Ayudante para no repetir el Builder once veces */
    private ProductEntity product(String name, String description, String format,
                                  String price, String seasonInfo) {
        return ProductEntity.builder()
                .name(name)
                .description(description)
                .format(format)
                .price(new BigDecimal(price))
                .seasonInfo(seasonInfo)
                .status(ProductStatus.AVAILABLE)
                .build();
    }
}