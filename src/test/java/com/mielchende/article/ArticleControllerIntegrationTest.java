package com.mielchende.article;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/* Test de integración de los artículos (público y panel), contra MySQL real */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ArticleControllerIntegrationTest {

    private static final String PUBLIC_URL = "/api/articles";
    private static final String ADMIN_URL = "/api/admin/articles";
    private static final String ADMIN_EMAIL = "admin@prueba.com";
    private static final String CLIENT_EMAIL = "cliente@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    private static final String VALID_ARTICLE = """
            {
              "title": "Llega la cosecha",
              "summary": "Este año la cosecha se adelanta",
              "body": "Primer párrafo.\\nSegundo párrafo."
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    /* Antes de cada test: tablas limpias, un admin y un cliente, y el token del admin */
    @BeforeEach
    void setUp() throws Exception {
        articleRepository.deleteAll();
        userRepository.deleteAll();

        createUser("Admin", ADMIN_EMAIL, RoleDataInitializer.ROLE_ADMIN);
        createUser("Cliente", CLIENT_EMAIL, RoleDataInitializer.ROLE_USER);
        adminToken = loginAndGetToken(ADMIN_EMAIL);
    }

    @Test
    void publicListShowsOnlyPublishedArticlesWithoutToken() throws Exception {
        saveArticle("Publicado", ArticleStatus.PUBLISHED, LocalDateTime.now());
        saveArticle("Borrador", ArticleStatus.DRAFT, null);

        /* Sin token: solo aparece el publicado */
        mockMvc.perform(get(PUBLIC_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Publicado"));
    }

    @Test
    void createArticleReturns201AsDraft() throws Exception {
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ARTICLE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist());
    }

    @Test
    void createArticleWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Solo título\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summaryLongerThan500CharactersReturns400() throws Exception {
        String longSummary = "a".repeat(501);
        String article = VALID_ARTICLE.replace("Este año la cosecha se adelanta", longSummary);

        /* @Size lo detiene antes de llegar a la base de datos */
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(article))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishingMakesArticleVisibleInPublicList() throws Exception {
        Long id = saveArticle("Borrador", ArticleStatus.DRAFT, null);

        /* El admin lo publica */
        mockMvc.perform(patch(ADMIN_URL + "/" + id + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"status\": \"PUBLISHED\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedAt").exists());

        /* La página pública, sin token, ya lo muestra */
        mockMvc.perform(get(PUBLIC_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void adminListIncludesDrafts() throws Exception {
        saveArticle("Borrador", ArticleStatus.DRAFT, null);

        mockMvc.perform(get(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void updateUnknownArticleReturns404() throws Exception {
        mockMvc.perform(put(ADMIN_URL + "/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ARTICLE))
                .andExpect(status().isNotFound());
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

        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ARTICLE))
                .andExpect(status().isForbidden());
    }

    /* Ayudante: guarda un artículo directamente y devuelve su id */
    private Long saveArticle(String title, ArticleStatus status, LocalDateTime publishedAt) {
        return articleRepository.save(ArticleEntity.builder()
                .title(title)
                .summary("Resumen")
                .body("Texto")
                .status(status)
                .createdAt(LocalDateTime.now())
                .publishedAt(publishedAt)
                .build()).getId();
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