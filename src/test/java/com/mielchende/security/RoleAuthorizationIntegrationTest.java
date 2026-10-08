package com.mielchende.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.mielchende.TestcontainersConfiguration;
import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleRepository;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Comprueba las reglas de acceso por rol, contra MySQL real.
   Las rutas aún no existen: 404 significa "la seguridad me ha dejado pasar" */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RoleAuthorizationIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@prueba.com";
    private static final String CLIENT_EMAIL = "cliente@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* Antes de cada test: una administradora y una clienta en la base de datos.
       La admin se crea directamente con el repositorio, porque el registro
       público siempre asigna ROLE_USER */
    @BeforeEach
    void createUsers() {
        userRepository.deleteAll();
        createUser("Admin", ADMIN_EMAIL, RoleDataInitializer.ROLE_ADMIN);
        createUser("Cliente", CLIENT_EMAIL, RoleDataInitializer.ROLE_USER);
    }

    @Test
    void adminRouteWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRouteWithClientTokenReturns403() throws Exception {
        /* Autenticada, pero sin el rol necesario → 403 Forbidden */
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + loginAndGetToken(CLIENT_EMAIL)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRouteWithAdminTokenIsAllowed() throws Exception {
        /* La seguridad deja pasar; 404 porque la ruta aún no existe */
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + loginAndGetToken(ADMIN_EMAIL)))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicCatalogRouteWorksWithoutToken() throws Exception {
        /* Catálogo público: sin token no da 401, llega hasta la ruta (404 de momento) */
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isNotFound());
    }

    @Test
    void corsAllowsFrontendOriginWithAuthorizationHeader() throws Exception {
        /* Petición "preflight": el navegador pregunta con OPTIONS
           si puede enviar la petición real desde el origen del frontend */
        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://web-desconocida.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    /* Ayudante: guarda un usuario con el rol indicado y la contraseña cifrada */
    private void createUser(String name, String email, String roleName) {
        UserEntity user = UserEntity.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .role(roleRepository.findByName(roleName).orElseThrow())
                .build();
        userRepository.save(user);
    }

    /* Ayudante: hace login con Basic Auth y devuelve el token JWT */
    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}