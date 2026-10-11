package com.mielchende.role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Entidad que representa un rol de seguridad (ROLE_USER o ROLE_ADMIN).
   Se guarda en la tabla "roles". */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class RoleEntity {

    /* Clave primaria autoincremental generada por la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nombre del rol con el prefijo ROLE_ que espera Spring Security.
       Único y obligatorio: no puede haber dos roles con el mismo nombre */
    @Column(nullable = false, unique = true)
    private String name;
}