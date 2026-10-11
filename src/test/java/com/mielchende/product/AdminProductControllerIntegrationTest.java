package com.mielchende.product;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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

/* Test de integración de la gestión de productos por la admin, contra MySQL real */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminProductControllerIntegrationTest {

    private static final String ADMIN_URL = "/api/admin/products";
    private static final String ADMIN_EMAIL = "admin@prueba.com";
    private static final String PASSWORD = "contrasenaDePrueba123";

    private static final String VALID_PRODUCT = """
            {
              "name": "Miel de castaño",
              "description": "Miel oscura de sabor intenso",
              "format": "500 g",
              "price": 8.50,
              "status": "AVAILABLE"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    /* Antes de cada test: tablas limpias, una admin y su token */
    @BeforeEach
    void setUp() throws Exception {
        productRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(UserEntity.builder()
                .name("Admin")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .role(roleRepository.findByName(RoleDataInitializer.ROLE_ADMIN).orElseThrow())
                .build());

        String response = mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(ADMIN_EMAIL, PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        adminToken = JsonPath.read(response, "$.token");
    }

    @Test
    void createProductReturns201() throws Exception {
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Miel de castaño"));
    }

    @Test
    void createProductWithMissingFieldsReturns400() throws Exception {
        /* Falta casi todo: el nombre, la descripción, el precio... */
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"format\": \"500 g\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductReturns200() throws Exception {
        Long id = saveProduct();

        String updated = VALID_PRODUCT.replace("8.50", "9.00");

        mockMvc.perform(put(ADMIN_URL + "/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(9.00));
    }

    @Test
    void statusChangeIsVisibleInPublicCatalog() throws Exception {
        Long id = saveProduct();

        /* La admin lo marca como agotado */
        mockMvc.perform(patch(ADMIN_URL + "/" + id + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"status\": \"SOLD_OUT\" }"))
                .andExpect(status().isOk());

        /* El catálogo público, sin token, ya lo muestra agotado */
        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD_OUT"));
    }

    @Test
    void deleteProductReturns204() throws Exception {
        Long id = saveProduct();

        mockMvc.perform(delete(ADMIN_URL + "/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        /* Ya no existe en el catálogo */
        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownProductReturns404() throws Exception {
        mockMvc.perform(delete(ADMIN_URL + "/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    /* Ayudante: guarda un producto directamente y devuelve su id */
    private Long saveProduct() {
        return productRepository.save(ProductEntity.builder()
                .name("Miel de castaño")
                .description("Miel oscura de sabor intenso")
                .format("500 g")
                .price(new BigDecimal("8.50"))
                .status(ProductStatus.AVAILABLE)
                .build()).getId();
    }
}