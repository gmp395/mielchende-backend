package com.mielchende.product;

import com.mielchende.product.dto.ProductRequestDto;
import com.mielchende.product.dto.ProductResponseDto;

/* Traduce entre ProductEntity y sus DTOs */
public class ProductMapper {

    /* Clase de utilidad: solo métodos estáticos */
    private ProductMapper() { }

    /* Entidad → DTO de respuesta */
    public static ProductResponseDto toResponseDto(ProductEntity entity) {
        return new ProductResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getFormat(),
                entity.getPrice(),
                entity.getImageUrl(),
                entity.getSeasonInfo(),
                entity.getCategory(),
                entity.getStatus());
    }

    /* DTO de petición → entidad nueva (para crear) */
    public static ProductEntity toEntity(ProductRequestDto dto) {
        return ProductEntity.builder()
                .name(dto.name())
                .description(dto.description())
                .format(dto.format())
                .price(dto.price())
                .imageUrl(dto.imageUrl())
                .seasonInfo(dto.seasonInfo())
                .category(dto.category())
                .status(dto.status())
                .build();
    }

    /* Copia los datos del DTO sobre una entidad existente (para editar) */
    public static void updateEntity(ProductEntity entity, ProductRequestDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setFormat(dto.format());
        entity.setPrice(dto.price());
        entity.setImageUrl(dto.imageUrl());
        entity.setSeasonInfo(dto.seasonInfo());
        entity.setCategory(dto.category());
        entity.setStatus(dto.status());
    }
}