package com.mielchende.user;

import com.mielchende.role.RoleEntity;

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

/* Entidad que representa a una persona registrada (cliente o administradora).
   Se guarda en la tabla "users". */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class UserEntity {

    /* Clave primaria autoincremental generada por la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nombre con el que se dirige la web al usuario */
    @Column(nullable = false)
    private String name;

    /* Email: sirve como identificador para iniciar sesión,
       por eso es único y obligatorio */
    @Column(nullable = false, unique = true)
    private String email;

    /* Contraseña cifrada con BCrypt (nunca en texto plano) */
    @Column(nullable = false)
    private String password;

    /* Rol del usuario. Muchos usuarios comparten el mismo rol,
       por eso es @ManyToOne. La columna role_id guarda la clave foránea */
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
}