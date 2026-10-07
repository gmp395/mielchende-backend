package com.mielchende;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/* Configuración solo para tests: levanta un contenedor MySQL real.
   @ServiceConnection hace que Spring se conecte a él automáticamente,
   sin escribir URL, usuario ni contraseña. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /* Misma versión de MySQL que en desarrollo (compose.yaml),
       para que los tests prueben contra el mismo motor */
    @Bean
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.4"));
    }
}