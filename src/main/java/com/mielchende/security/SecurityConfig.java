package com.mielchende.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/* Configuración de seguridad de la aplicación */
@Configuration
public class SecurityConfig {

    /* Orígenes permitidos para CORS, leídos de application.properties */
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /* CORS: primer filtro de la cadena (apuntes 15.5).
               Usa el bean corsConfigurationSource definido más abajo */
            .cors(withDefaults())

            /* CSRF desactivado: API stateless con JWT, sin cookies de sesión (apuntes 15.4) */
            .csrf(csrf -> csrf.disable())

            /* Stateless: el servidor no guarda sesión (no hay JSESSIONID) */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /* Reglas de acceso, de la más concreta a la más general */
            .authorizeHttpRequests(auth -> auth
                /* Registro: público */
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                /* Catálogo y ficha de producto: públicos, solo lectura */
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                /* Ruta interna de errores: pública, para no convertir errores en 401 */
                .requestMatchers("/error").permitAll()
                /* Panel de administración: solo ROLE_ADMIN.
                   hasRole("ADMIN") añade el prefijo ROLE_ automáticamente */
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                /* Todo lo demás (login, pedidos...): cualquier usuaria autenticada */
                .anyRequest().authenticated()
            )

            /* Basic Auth: se usa en el login para enviar email y contraseña */
            .httpBasic(withDefaults())

            /* Resource server: valida el token Bearer en el resto de peticiones */
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /* Configuración CORS: qué orígenes, métodos y cabeceras acepta la API.
       Incluye Authorization, sin la cual el navegador bloquearía el envío del token
       (error típico "blocked by CORS policy", apuntes 15.20) */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        /* Se aplica a todas las rutas de la API */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /* Le dice a Spring dónde están los roles dentro del token:
       en el claim "roles" y ya con el prefijo ROLE_ */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /* Bean de BCrypt (apuntes 15.14) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}