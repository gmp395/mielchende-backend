package com.mielchende.role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/* Crea los roles de la aplicación al arrancar, si todavía no existen.
   CommandLineRunner: Spring ejecuta el método run() automáticamente
   una vez que la aplicación ha arrancado. */
@Component
public class RoleDataInitializer implements CommandLineRunner {

    /* Nombres de los roles, con el prefijo ROLE_ que espera Spring Security */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RoleRepository roleRepository;

    /* Inyección por constructor (nunca @Autowired sobre el campo) */
    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfMissing(ROLE_USER);
        createRoleIfMissing(ROLE_ADMIN);
    }

    /* Solo inserta el rol si no está ya en la base de datos.
       Así se puede arrancar muchas veces sin duplicarlos */
    private void createRoleIfMissing(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(RoleEntity.builder().name(name).build());
        }
    }
}