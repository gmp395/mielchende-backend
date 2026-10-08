package com.mielchende.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/* Configuración de seguridad de la aplicación */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /* CSRF desactivado: API stateless con JWT, sin cookies de sesión (apuntes 15.4) */
            .csrf(csrf -> csrf.disable())

            /* Stateless: el servidor no guarda sesión (no hay JSESSIONID) */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /* Reglas de acceso por ruta */
            .authorizeHttpRequests(auth -> auth
                /* Registro: público */
                .requestMatchers("/api/auth/register").permitAll()
                /* Ruta interna de errores: pública, para no convertir errores en 401 */
                .requestMatchers("/error").permitAll()
                /* Todo lo demás, incluido el login, requiere autenticación */
                .anyRequest().authenticated()
            )

            /* Basic Auth: se usa en el login para enviar email y contraseña */
            .httpBasic(withDefaults())

            /* Resource server: valida el token Bearer en el resto de peticiones,
               usando el JwtDecoder de JwtConfig */
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /* Le dice a Spring dónde están los roles dentro del token.
       Por defecto los buscaría en "scope" con el prefijo SCOPE_;
       los nuestros están en el claim "roles" y ya llevan el prefijo ROLE_ */
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