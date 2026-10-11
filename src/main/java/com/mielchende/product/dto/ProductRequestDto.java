package com.mielchende.product.dto;

import java.math.BigDecimal;

import com.mielchende.product.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/* Datos que envía la admin para crear o editar un producto.
   imageUrl y seasonInfo son opcionales */
public record ProductRequestDto(

    @NotBlank
    String name,

    @NotBlank
    String description,

    @NotBlank
    String format,

    /* Obligatorio y mayor que cero */
    @NotNull
    @Positive
    BigDecimal price,

    String imageUrl,

    String seasonInfo,

    @NotNull
    ProductStatus status
) { }