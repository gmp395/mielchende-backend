package com.mielchende.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mielchende.TestcontainersConfiguration;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test de integración: arranca la aplicación completa con un MySQL real
   (Testcontainers) y lanza peticiones HTTP simuladas con MockMvc.
   Recorre toda la cadena: seguridad → controller → servicio → base de datos. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerIntegrationTest {

    private static final String REGISTER_URL = "/api/auth/register";

    /* En los tests sí se permite @Autowired sobre campos:
       JUnit crea la clase y no hay constructor que usar */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    /* Antes de cada test vaciamos la tabla users,
       para que un test no dependa de lo que dejó el anterior.
       Los roles no se borran: los crea RoleDataInitializer al arrancar */
    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registerReturns201AndStoresHashedPassword() throws Exception {
        String body = """
                {
                  "name": "Ana Prueba",
                  "email": "ana@prueba.com",
                  "password": "contrasenaDePrueba123"
                }
                """;

        /* Petición y comprobaciones de la respuesta */
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@prueba.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                /* La contraseña nunca debe salir en la respuesta */
                .andExpect(jsonPath("$.password").doesNotExist());

        /* Comprobación en la base de datos real: el usuario existe
           y su contraseña está cifrada con BCrypt (empieza por $2a$) */
        UserEntity saved = userRepository.findByEmail("ana@prueba.com").orElseThrow();
        assertThat(saved.getPassword()).startsWith("$2a$");
    }

    @Test
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        String body = """
                {
                  "name": "Ana Prueba",
                  "email": "ana@prueba.com",
                  "password": "contrasenaDePrueba123"
                }
                """;

        /* Primer registro: correcto */
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        /* Segundo registro con el mismo email: conflicto */
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este email ya está registrado"));
    }

    @Test
    void registerReturns400WhenEmailIsBlank() throws Exception {
        String body = """
                {
                  "name": "Ana Prueba",
                  "email": "",
                  "password": "contrasenaDePrueba123"
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturns400WhenJsonIsMalformed() throws Exception {
        /* JSON roto a propósito: falta el valor de "email" */
        String body = """
                {
                  "name": "Ana Prueba",
                  "email":
                  "password": "contrasenaDePrueba123"
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es un JSON válido"));
    }
}