package com.mielchende;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/* Comprueba que la aplicación completa arranca con una base de datos real.
   @Import trae el contenedor MySQL de Testcontainers */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MielchendeBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}