package com.mielchende.product.dto;

import com.mielchende.product.ProductStatus;

import jakarta.validation.constraints.NotNull;

/* Cuerpo del PATCH para cambiar solo la disponibilidad.
   Un valor que no sea AVAILABLE ni SOLD_OUT da 400 (JSON no válido) */
public record ProductStatusRequestDto(
    @NotNull
    ProductStatus status
) { }