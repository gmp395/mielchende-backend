package com.mielchende.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la tabla roles. JpaRepository ya aporta save, findById, findAll, etc. */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    /* Query method: Spring genera la consulta a partir del nombre del método.
       Optional porque el rol podría no existir */
    Optional<RoleEntity> findByName(String name);
}