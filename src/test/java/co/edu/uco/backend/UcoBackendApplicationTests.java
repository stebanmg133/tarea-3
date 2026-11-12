package co.edu.uco.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UcoBackendApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring arranca con la configuración de pruebas.
    }
}
