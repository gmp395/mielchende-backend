package com.mielchende.product;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la tabla products. De momento bastan los métodos que ya trae JpaRepository */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}