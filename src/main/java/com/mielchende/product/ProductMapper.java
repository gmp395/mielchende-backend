package com.mielchende.product;

import com.mielchende.product.dto.ProductResponseDto;

/* Traduce ProductEntity al DTO de respuesta */
public class ProductMapper {

    /* Clase de utilidad: solo métodos estáticos */
    private ProductMapper() { }

    public static ProductResponseDto toResponseDto(ProductEntity entity) {
        return new ProductResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getFormat(),
                entity.getPrice(),
                entity.getImageUrl(),
                entity.getSeasonInfo(),
                entity.getStatus());
    }
}