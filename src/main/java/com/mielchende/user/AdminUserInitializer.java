package com.mielchende.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleRepository;
import com.mielchende.security.facade.PasswordEncoderFacade;

/* Crea la cuenta de administrador al arrancar, con los datos del .env.
   @Order(2): después de los roles */
@Component
@Order(2)
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderFacade passwordEncoderFacade;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    /* Además de los repositorios, recibe los datos del .env con @Value */
    public AdminUserInitializer(UserRepository userRepository,
                                RoleRepository roleRepository,
                                PasswordEncoderFacade passwordEncoderFacade,
                                @Value("${admin.name}") String adminName,
                                @Value("${admin.email}") String adminEmail,
                                @Value("${admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoderFacade = passwordEncoderFacade;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        /* Sin email o contraseña configurados, no se crea nada */
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        /* Si ya existe, no se toca: así no se duplica en cada arranque */
        if (userRepository.existsByEmail(email)) {
            return;
        }

        /* Se crea con la contraseña cifrada con BCrypt y el rol ROLE_ADMIN */
        userRepository.save(UserEntity.builder()
                .name(adminName.isBlank() ? "Administrador" : adminName)
                .email(email)
                .password(passwordEncoderFacade.encode(adminPassword))
                .role(roleRepository.findByName(RoleDataInitializer.ROLE_ADMIN).orElseThrow())
                .build());
    }
}