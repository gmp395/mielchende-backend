package com.mielchende.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/* Configuración de los tokens JWT: crea el encoder (genera tokens)
   y el decoder (los verifica), ambos con la misma clave secreta (HS512). */
@Configuration
public class JwtConfig {

    private final SecretKey secretKey;

    /* @Value lee la propiedad jwt.secret de application.properties,
       que a su vez viene de JWT_SECRET en el .env.
       La clave está en Base64, así que la decodificamos a bytes
       y construimos con ella una clave HMAC-SHA512 */
    public JwtConfig(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
    }

    /* Genera y firma los tokens (lo usaremos en el login) */
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /* Verifica la firma y la caducidad de los tokens que llegan en las peticiones */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}