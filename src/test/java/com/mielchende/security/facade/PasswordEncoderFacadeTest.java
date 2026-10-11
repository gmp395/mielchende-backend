package com.mielchende.security.facade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/* Test unitario de la fachada de cifrado.
   Aquí no usamos mock: usamos BCrypt real, porque lo que queremos
   comprobar es precisamente que el cifrado funciona. */
class PasswordEncoderFacadeTest {

    private final PasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final PasswordEncoderFacade facade = new PasswordEncoderFacade(bcrypt);

    @Test
    void encodedPasswordIsNotPlainText() {
        String encoded = facade.encode("miContrasenaSegura123");

        /* El hash nunca debe coincidir con la contraseña original */
        assertThat(encoded).isNotEqualTo("miContrasenaSegura123");
    }

    @Test
    void encodedPasswordMatchesOriginal() {
        String encoded = facade.encode("miContrasenaSegura123");

        /* BCrypt puede comprobar que el hash corresponde a la contraseña.
           Es lo que hará Spring Security en el login */
        assertThat(bcrypt.matches("miContrasenaSegura123", encoded)).isTrue();
    }
}