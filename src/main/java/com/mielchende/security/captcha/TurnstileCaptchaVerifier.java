package com.mielchende.security.captcha;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/* Implementación con Cloudflare Turnstile.
   El frontend obtiene un token al superar el captcha; aquí se comprueba
   con la API siteverify de Cloudflare usando la clave secreta */
@Component
public class TurnstileCaptchaVerifier implements CaptchaVerifier {

    private static final String VERIFY_URL =
            "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final String secretKey;

    /* RestClient: cliente HTTP de Spring para que el backend
       haga peticiones a otras APIs */
    private final RestClient restClient = RestClient.create();

    public TurnstileCaptchaVerifier(@Value("${turnstile.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public boolean verify(String token) {
        /* Falla cerrado: sin clave o sin token no se puede verificar a nadie */
        if (isBlank(secretKey) || isBlank(token)) {
            return false;
        }

        /* Cloudflare espera un formulario con la clave secreta y el token */
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("response", token);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            /* La respuesta trae "success": true si el token es válido */
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (RestClientException ex) {
            /* Cloudflare no responde o da error: también se rechaza */
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}