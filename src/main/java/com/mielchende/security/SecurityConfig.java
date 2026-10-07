package com.mielchende.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/* Configuración de seguridad de la aplicación.
   @Configuration indica a Spring que esta clase declara beans. */
@Configuration
public class SecurityConfig {

    /* Bean de BCrypt (apuntes 15.14): Spring lo crea una vez
       y lo inyecta donde se necesite cifrar o comparar contraseñas */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}