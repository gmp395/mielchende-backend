package com.mielchende.product;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Producto del catálogo. Cada formato es un producto distinto
   (p. ej. "Miel de castaño" de 500 g y de 1 kg) */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nombre visible en el catálogo */
    @Column(nullable = false)
    private String name;

    /* Descripción larga para la ficha. TEXT admite textos de más de 255 caracteres */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /* Formato o presentación: "500 g", "1 kg", "Unidad"... */
    @Column(nullable = false)
    private String format;

    /* Precio orientativo. BigDecimal evita los errores de redondeo de double.
       precision 8, scale 2 → hasta 999999.99 */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    /* Ruta o URL de la imagen del producto */
    private String imageUrl;

    /* Información de temporada, solo para productos vivos (opcional) */
    private String seasonInfo;

    /* Disponibilidad. EnumType.STRING guarda el texto ("AVAILABLE"),
       no la posición del enum: así añadir valores en el futuro no rompe los datos */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
}