package com.mielchende.product;

/* Categoría del producto, usada para agrupar y filtrar el catálogo.
   Se guarda como texto (EnumType.STRING) igual que ProductStatus */
public enum ProductCategory {
    /* Mieles */
    HONEY,
    /* Otros productos de la colmena: polen, propóleo, cera... */
    HIVE_PRODUCTS,
    /* Productos para el apicultor: reinas, enjambres... */
    BEEKEEPER_PRODUCTS
}