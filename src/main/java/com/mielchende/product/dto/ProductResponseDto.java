package com.mielchende.product.dto;

import java.math.BigDecimal;

import com.mielchende.product.ProductStatus;

/* Datos de un producto que devuelve la API (catálogo y ficha) */
public record ProductResponseDto(
    Long id,
    String name,
    String description,
    String format,
    BigDecimal price,
    String imageUrl,
    String seasonInfo,
    ProductStatus status
) { }