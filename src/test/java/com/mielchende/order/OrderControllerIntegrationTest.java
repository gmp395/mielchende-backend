package com.mielchende.order;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.jayway.jsonpath.JsonPath;
import com.mielchende.TestcontainersConfiguration;
import com.mielchende.product.ProductEntity;
import com.mielchende.product.ProductRepository;
import com.mielchende.product.ProductStatus;
import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleRepository;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test de integración de las solicitudes de pedido, contra MySQL real */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrderControllerIntegrationTest {

    private static final String ORDERS_URL = "/api/orders";
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

    private Long honeyId;
    private Long pollenId;
    private Long soldOutId;

    /* Antes de cada test: base de datos limpia, una clienta, una admin y tres productos.
       Las solicitudes se borran primero, porque dependen de usuarios y productos */
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        createUser("Cliente", CLIENT_EMAIL, RoleDataInitializer.ROLE_USER);
        createUser("Admin", ADMIN_EMAIL, RoleDataInitializer.ROLE_ADMIN);

        honeyId = saveProduct("Miel de castaño", ProductStatus.AVAILABLE);
        pollenId = saveProduct("Polen", ProductStatus.AVAILABLE);
        soldOutId = saveProduct("Cera", ProductStatus.SOLD_OUT);
    }

    /* Después de cada test, se borran las solicitudes: si se quedaran,
       los tests de otras clases no podrían borrar usuarios ni productos (clave foránea) */
    @AfterEach
    void cleanOrders() {
        orderRepository.deleteAll();
    }

    @Test
    void clientCreatesOrderWithSeveralProducts() throws Exception {
        String body = """
                {
                  "items": [
                    { "productId": %d, "quantity": 2 },
                    { "productId": %d, "quantity": 1 }
                  ],
                  "phone": "600000000",
                  "comments": "Entrega por la tarde"
                }
                """.formatted(honeyId, pollenId);

        mockMvc.perform(post(ORDERS_URL)
                        .header("Authorization", "Bearer " + token(CLIENT_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.customerEmail").value(CLIENT_EMAIL))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void orderWithSoldOutProductReturns409() throws Exception {
        String body = """
                { "items": [ { "productId": %d, "quantity": 1 } ] }
                """.formatted(soldOutId);

        mockMvc.perform(post(ORDERS_URL)
                        .header("Authorization", "Bearer " + token(CLIENT_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("agotado")));
    }

    @Test
    void orderWithoutItemsReturns400() throws Exception {
        mockMvc.perform(post(ORDERS_URL)
                        .header("Authorization", "Bearer " + token(CLIENT_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"items\": [] }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void orderWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post(ORDERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"items\": [ { \"productId\": 1, \"quantity\": 1 } ] }"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCannotCreateOrders() throws Exception {
        /* Las solicitudes son solo para clientas (ROLE_USER) → 403 */
        String body = """
                { "items": [ { "productId": %d, "quantity": 1 } ] }
                """.formatted(honeyId);

        mockMvc.perform(post(ORDERS_URL)
                        .header("Authorization", "Bearer " + token(ADMIN_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletingProductWithOrdersReturns409() throws Exception {
        /* 1. La clienta solicita miel */
        String body = """
                { "items": [ { "productId": %d, "quantity": 1 } ] }
                """.formatted(honeyId);
        mockMvc.perform(post(ORDERS_URL)
                        .header("Authorization", "Bearer " + token(CLIENT_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        /* 2. La admin intenta borrar ese producto: la base de datos lo impide → 409 */
        mockMvc.perform(delete("/api/admin/products/" + honeyId)
                        .header("Authorization", "Bearer " + token(ADMIN_EMAIL)))
                .andExpect(status().isConflict());
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

    /* Ayudante: guarda un producto y devuelve su id */
    private Long saveProduct(String name, ProductStatus status) {
        return productRepository.save(ProductEntity.builder()
                .name(name)
                .description("Descripción de prueba")
                .format("500 g")
                .price(new BigDecimal("8.50"))
                .status(status)
                .build()).getId();
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