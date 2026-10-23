package com.mielchende.security.captcha;

/* Contrato para verificar el token del captcha.
   Los services dependen de esta interfaz, no de Cloudflare:
   en los tests se sustituye por un mock y no se llama a internet */
public interface CaptchaVerifier {

    /* true si el token demuestra que quien envía es una persona */
    boolean verify(String token);
}