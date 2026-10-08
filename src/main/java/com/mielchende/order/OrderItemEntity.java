package com.mielchende.order;

import com.mielchende.product.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Una línea de la solicitud: un producto y su cantidad */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Solicitud a la que pertenece (la columna order_id guarda la clave foránea) */
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    /* Producto solicitado */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    /* Cantidad (mínimo 1, se valida en el DTO) */
    @Column(nullable = false)
    private Integer quantity;
}