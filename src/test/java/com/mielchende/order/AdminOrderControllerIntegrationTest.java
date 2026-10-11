package com.mielchende.order;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.mielchende.TestcontainersConfiguration;
import com.mielchende.product.ProductEntity;
import com.mielchende.product.ProductRepository;
import com.mielchende.product.ProductStatus;
import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleRepository;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test de integración: la admin consulta y gestiona las solicitudes, contra MySQL real */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminOrderControllerIntegrationTest {

    private static final String ADMIN_ORDERS_URL = "/api/admin/orders";
    private static final String CLIENT_EMAIL = "cliente@prueba.com";
    private static final String ADMIN_EMAIL = "admin@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* Antes de cada test: datos limpios y dos solicitudes enviadas por la clienta,
       primero "Primera" y después "Segunda" */
    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        createUser("Cliente", CLIENT_EMAIL, RoleDataInitializer.ROLE_USER);
        createUser("Admin", ADMIN_EMAIL, RoleDataInitializer.ROLE_ADMIN);

        Long honeyId = productRepository.save(ProductEntity.builder()
                .name("Miel de castaño")
                .description("Descripción de prueba")
                .format("500 g")
                .price(new BigDecimal("8.50"))
                .status(ProductStatus.AVAILABLE)
                .build()).getId();

        String clientToken = token(CLIENT_EMAIL);
        sendOrder(clientToken, honeyId, "Primera");
        sendOrder(clientToken, honeyId, "Segunda");
    }

    /* Limpieza final para no bloquear los borrados de otras clases de test */
    @AfterEach
    void cleanOrders() {
        orderRepository.deleteAll();
    }

    @Test
    void adminSeesOrdersFromNewestToOldest() throws Exception {
        mockMvc.perform(get(ADMIN_ORDERS_URL)
                        .header("Authorization", "Bearer " + token(ADMIN_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                /* La más reciente primero */
                .andExpect(jsonPath("$[0].comments").value("Segunda"))
                .andExpect(jsonPath("$[1].comments").value("Primera"))
                /* Con los datos que necesita la admin para contactar */
                .andExpect(jsonPath("$[0].customerEmail").value(CLIENT_EMAIL))
                .andExpect(jsonPath("$[0].items[0].productName").value("Miel de castaño"));
    }

    @Test
    void summaryShowsPendingOrders() throws Exception {
        mockMvc.perform(get(ADMIN_ORDERS_URL + "/summary")
                        .header("Authorization", "Bearer " + token(ADMIN_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingOrders").value(2));
    }

    @Test
    void clientCannotSeeAllOrders() throws Exception {
        /* Una clienta no puede ver las solicitudes de las demás → 403 */
        mockMvc.perform(get(ADMIN_ORDERS_URL)
                        .header("Authorization", "Bearer " + token(CLIENT_EMAIL)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConfirmsReceivedOrder() throws Exception {
        /* RECEIVED → CONFIRMED: un paso adelante → 200 con el estado nuevo */
        patchStatus(anyOrderId(), "CONFIRMED", ADMIN_EMAIL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void skippingAStepReturns409() throws Exception {
        /* RECEIVED → SHIPPED: se salta CONFIRMED → 409 con mensaje explicativo */
        patchStatus(anyOrderId(), "SHIPPED", ADMIN_EMAIL)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("un paso")));
    }

    @Test
    void changingStatusOfNonexistentOrderReturns404() throws Exception {
        /* Un id que no existe en la base de datos → 404 */
        patchStatus(999999L, "CONFIRMED", ADMIN_EMAIL)
                .andExpect(status().isNotFound());
    }

    @Test
    void clientCannotChangeOrderStatus() throws Exception {
        /* Solo la admin puede cambiar estados: una clienta → 403 */
        patchStatus(anyOrderId(), "CONFIRMED", CLIENT_EMAIL)
                .andExpect(status().isForbidden());
    }

    /* Ayudante: envía el PATCH de cambio de estado con el token de la usuaria indicada.
       Devuelve ResultActions para que cada test añada sus propias comprobaciones */
    private ResultActions patchStatus(Long orderId, String newStatus, String email) throws Exception {
        return mockMvc.perform(patch(ADMIN_ORDERS_URL + "/" + orderId + "/status")
                .header("Authorization", "Bearer " + token(email))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"" + newStatus + "\" }"));
    }

    /* Ayudante: id de una de las solicitudes creadas en setUp (ambas están en RECEIVED) */
    private Long anyOrderId() {
        return orderRepository.findAll().get(0).getId();
    }

    /* Ayudante: la clienta envía una solicitud con un comentario identificativo */
    private void sendOrder(String token, Long productId, String comments) throws Exception {
        String body = """
                {
                  "items": [ { "productId": %d, "quantity": 1 } ],
                  "comments": "%s"
                }
                """.formatted(productId, comments);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    /* Ayudante: guarda un usuario con el rol indicado */
    private void createUser(String name, String email, String roleName) {
        userRepository.save(UserEntity.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .role(roleRepository.findByName(roleName).orElseThrow())
                .build());
    }

    /* Ayudante: login con Basic Auth y devuelve el token */
    private String token(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}