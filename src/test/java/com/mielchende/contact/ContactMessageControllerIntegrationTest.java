package com.mielchende.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.mielchende.TestcontainersConfiguration;
import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleRepository;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test de integración del formulario de contacto (público) y de su lectura
   en el panel (admin), contra MySQL real */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ContactMessageControllerIntegrationTest {

    private static final String PUBLIC_URL = "/api/contact";
    private static final String ADMIN_URL = "/api/admin/contact-messages";
    private static final String ADMIN_EMAIL = "admin@prueba.com";
    private static final String CLIENT_EMAIL = "cliente@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    private static final String VALID_MESSAGE = """
            {
              "name": "Ana",
              "email": "ana@prueba.com",
              "message": "¿Tenéis miel de castaño?"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* Antes de cada test: tablas limpias, un admin y un cliente */
    @BeforeEach
    void setUp() {
        contactMessageRepository.deleteAll();
        userRepository.deleteAll();

        createUser("Admin", ADMIN_EMAIL, RoleDataInitializer.ROLE_ADMIN);
        createUser("Cliente", CLIENT_EMAIL, RoleDataInitializer.ROLE_USER);
    }

    @Test
    void visitorCanSendMessageWithoutToken() throws Exception {
        /* Sin token: el formulario es público */
        mockMvc.perform(post(PUBLIC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_MESSAGE))
                .andExpect(status().isCreated());

        /* Y el mensaje queda guardado en la base de datos */
        assertThat(contactMessageRepository.findAll()).hasSize(1);
    }

    @Test
    void messageWithInvalidEmailReturns400() throws Exception {
        String message = VALID_MESSAGE.replace("ana@prueba.com", "esto-no-es-un-correo");

        mockMvc.perform(post(PUBLIC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message))
                .andExpect(status().isBadRequest());

        assertThat(contactMessageRepository.findAll()).isEmpty();
    }

    @Test
    void messageWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post(PUBLIC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Ana\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void messageLongerThan2000CharactersReturns400() throws Exception {
        String message = VALID_MESSAGE.replace("¿Tenéis miel de castaño?", "a".repeat(2001));

        /* @Size lo detiene antes de llegar a la base de datos */
        mockMvc.perform(post(PUBLIC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminSeesMessagesFromNewestToOldest() throws Exception {
        saveMessage("Antiguo", LocalDateTime.now().minusDays(1));
        saveMessage("Reciente", LocalDateTime.now());
        String adminToken = loginAndGetToken(ADMIN_EMAIL);

        mockMvc.perform(get(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].message").value("Reciente"))
                .andExpect(jsonPath("$[1].message").value("Antiguo"));
    }

    @Test
    void adminRouteWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(ADMIN_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRouteWithClientTokenReturns403() throws Exception {
        /* Autenticado (sabemos quién es) pero sin permiso (no es admin) */
        String clientToken = loginAndGetToken(CLIENT_EMAIL);

        mockMvc.perform(get(ADMIN_URL)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    /* Ayudante: guarda un mensaje directamente con una fecha concreta */
    private void saveMessage(String text, LocalDateTime createdAt) {
        contactMessageRepository.save(ContactMessageEntity.builder()
                .name("Ana")
                .email("ana@prueba.com")
                .message(text)
                .createdAt(createdAt)
                .build());
    }

    /* Ayudante: crea un usuario con el rol indicado */
    private void createUser(String name, String email, String roleName) {
        userRepository.save(UserEntity.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .role(roleRepository.findByName(roleName).orElseThrow())
                .build());
    }

    /* Ayudante: hace login con Basic Auth y devuelve el token JWT */
    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(email, PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}