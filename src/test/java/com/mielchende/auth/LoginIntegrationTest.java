package com.mielchende.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.mielchende.TestcontainersConfiguration;
import com.mielchende.user.UserRepository;

/* Test de integración del login con JWT, contra MySQL real (Testcontainers) */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginIntegrationTest {

    private static final String EMAIL = "ana@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    /* Antes de cada test: base de datos limpia y un usuario registrado
       a través del propio endpoint, como lo haría una persona real */
    @BeforeEach
    void registerUser() throws Exception {
        userRepository.deleteAll();

        String body = """
                {
                  "name": "Ana Prueba",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        /* httpBasic() añade la cabecera Authorization: Basic ..., como hace Postman */
        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        /* Leemos el campo "token" del JSON de respuesta.
           Un JWT empieza siempre por "eyJ" (la cabecera {" codificada) */
        String token = JsonPath.read(response, "$.token");
        assertThat(token).startsWith("eyJ");
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(EMAIL, "contrasenaIncorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteRejectsRequestWithoutToken() throws Exception {
        /* Ruta protegida (todo lo que no es /api/auth/register lo es) sin token → 401 */
        mockMvc.perform(get("/api/ruta-protegida"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteAcceptsValidToken() throws Exception {
        /* 1. Login para obtener un token real */
        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(EMAIL, PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");

        /* 2. Con el token, la seguridad deja pasar la petición.
           La ruta no existe, así que la respuesta es 404, no 401:
           eso demuestra que el token se ha aceptado */
        mockMvc.perform(get("/api/ruta-protegida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}