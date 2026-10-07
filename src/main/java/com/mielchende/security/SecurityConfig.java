package com.mielchende.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/* Configuración de seguridad de la aplicación */
@Configuration
public class SecurityConfig {

    /* Cadena de filtros de seguridad (forma moderna; nunca WebSecurityConfigurerAdapter) */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /* CSRF desactivado: protege sesiones con cookies, y nuestra API
               será stateless con JWT, sin cookies de sesión (apuntes 15.4) */
            .csrf(csrf -> csrf.disable())

            /* Stateless: el servidor no guarda sesión (no hay JSESSIONID) */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /* Reglas de acceso por ruta, de la más concreta a la más general */
            .authorizeHttpRequests(auth -> auth
                /* Registro (y login, más adelante): públicos */
                .requestMatchers("/api/auth/**").permitAll()
                /* Ruta interna de errores de Spring: pública, para que un error
                   no se convierta en un 401 engañoso */
                .requestMatchers("/error").permitAll()
                /* Todo lo demás requiere estar autenticada */
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /* Bean de BCrypt (apuntes 15.14) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}