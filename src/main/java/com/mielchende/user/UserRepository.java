package com.mielchende.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la tabla users */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /* Busca un usuario por su email (lo usaremos en el login) */
    Optional<UserEntity> findByEmail(String email);

    /* Devuelve true si ya hay un usuario con ese email (para evitar duplicados al registrar) */
    boolean existsByEmail(String email);
}