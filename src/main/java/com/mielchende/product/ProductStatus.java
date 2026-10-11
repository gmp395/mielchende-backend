package com.mielchende.product;

/* Disponibilidad de un producto. Solo dos estados:
   la miel se cosecha una vez al año, así que no se lleva un stock numérico */
public enum ProductStatus {
    AVAILABLE,
    SOLD_OUT
}