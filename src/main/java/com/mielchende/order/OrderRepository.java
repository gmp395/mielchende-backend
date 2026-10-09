package com.mielchende.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a las solicitudes. Las líneas se guardan y borran en cascada con ellas */
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /* Para la admin (MC-31): todas, de más reciente a más antigua */
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    /* Para la clienta ("Mis solicitudes"): solo las suyas, de más reciente a más antigua.
       Spring Data lee el nombre por partes:
       - findBy...UserEmail → recorre el campo "user" de OrderEntity y, dentro, su "email"
       - OrderByCreatedAtDesc → ordena por fecha de creación, descendente */
    List<OrderEntity> findByUserEmailOrderByCreatedAtDesc(String email);

    /* Para el contador del panel: cuántas hay en un estado (p. ej. RECEIVED) */
    long countByStatus(OrderStatus status);
}