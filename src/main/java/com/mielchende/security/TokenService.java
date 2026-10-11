package com.mielchende.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/* Genera los tokens JWT tras un login correcto */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    /* La duración del token se lee de application.properties (jwt.expiration-minutes) */
    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    /* Recibe la autenticación ya validada por Spring y devuelve el token firmado */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        /* Roles del usuario como lista de textos, p. ej. ["ROLE_USER"] */
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        /* Payload (claims) del token. Nada de datos sensibles: ni contraseña ni datos privados */
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mielchende")                                    /* quién emite el token */
                .issuedAt(now)                                           /* fecha de creación */
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES)) /* fecha de caducidad */
                .subject(authentication.getName())                       /* el email del usuario */
                .claim("roles", roles)                                   /* sus roles */
                .build();

        /* Cabecera: indica el algoritmo de firma (HS512, clave simétrica) */
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}