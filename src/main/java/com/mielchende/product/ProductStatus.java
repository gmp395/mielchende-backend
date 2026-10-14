package com.mielchende.product;

/* Disponibilidad de un producto. No se lleva un stock numérico,
   porque la miel se cosecha una vez al año:
   - AVAILABLE: se puede solicitar.
   - SOLD_OUT: agotado hasta la próxima cosecha o temporada.
   - COMING_SOON: todavía no disponible (por ejemplo, antes de la cosecha
     o de la temporada de cría). Se muestra, pero no se puede solicitar */
public enum ProductStatus {
    AVAILABLE,
    SOLD_OUT,
    COMING_SOON
}