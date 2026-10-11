package com.mielchende.role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/* Crea los roles de la aplicación al arrancar, si todavía no existen.
   @Order(1): se ejecuta el primero, porque la cuenta de admin necesita ROLE_ADMIN */
@Component
@Order(1)
public class RoleDataInitializer implements CommandLineRunner {

    /* Nombres de los roles, con el prefijo ROLE_ que espera Spring Security */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfMissing(ROLE_USER);
        createRoleIfMissing(ROLE_ADMIN);
    }

    /* Solo inserta el rol si no está ya en la base de datos */
    private void createRoleIfMissing(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(RoleEntity.builder().name(name).build());
        }
    }
}