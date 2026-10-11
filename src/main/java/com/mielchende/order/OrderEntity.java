package com.mielchende.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.mielchende.user.UserEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Solicitud de pedido: una clienta pide uno o varios productos.
   Tabla "order_requests" (ORDER es palabra reservada de SQL) */
@Entity
@Table(name = "order_requests")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Quién la envía. Muchas solicitudes pueden ser de la misma usuaria */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /* Líneas de la solicitud (producto + cantidad).
       mappedBy = "order": la relación la guarda el campo "order" de OrderItemEntity.
       cascade = ALL: al guardar o borrar la solicitud, se guardan o borran sus líneas.
       orphanRemoval: si una línea se quita de la lista, se borra de la base de datos.
       @Builder.Default: sin esto, el Builder de Lombok dejaría la lista en null */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    /* Teléfono de contacto (opcional) */
    private String phone;

    /* Comentarios de la clienta: dirección, dudas... (opcional) */
    @Column(columnDefinition = "TEXT")
    private String comments;

    /* Estado de la solicitud, guardado como texto */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /* Fecha y hora de creación, rellenada por Hibernate al guardar;
       updatable = false impide cambiarla después */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* Añade una línea y enlaza los dos lados de la relación a la vez.
       Si solo se añadiera a la lista, la línea no sabría a qué solicitud pertenece */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}