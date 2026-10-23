package com.mielchende.security.captcha;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/* Test unitario del verificador real: solo los casos que no llaman a internet.
   Comprueba que falla cerrado cuando falta la clave o el token */
class TurnstileCaptchaVerifierTest {

    @Test
    void rejectsTokenWhenSecretKeyIsMissing() {
        TurnstileCaptchaVerifier verifier = new TurnstileCaptchaVerifier("");

        /* Sin clave configurada no se puede verificar a nadie */
        assertThat(verifier.verify("cualquier-token")).isFalse();
    }

    @Test
    void rejectsMissingToken() {
        TurnstileCaptchaVerifier verifier = new TurnstileCaptchaVerifier("clave-secreta");

        assertThat(verifier.verify(null)).isFalse();
        assertThat(verifier.verify("  ")).isFalse();
    }
}