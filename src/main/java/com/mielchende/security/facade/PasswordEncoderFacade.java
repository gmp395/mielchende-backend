package com.mielchende.security.facade;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/* Fachada de cifrado: oculta que usamos BCrypt por debajo.
   Si un día cambiamos de algoritmo, solo se toca esta clase. */
@Component
public class PasswordEncoderFacade {

    private final PasswordEncoder passwordEncoder;

    /* Spring inyecta el bean BCrypt declarado en SecurityConfig */
    public PasswordEncoderFacade(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /* Recibe la contraseña en texto plano y devuelve su hash BCrypt */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}