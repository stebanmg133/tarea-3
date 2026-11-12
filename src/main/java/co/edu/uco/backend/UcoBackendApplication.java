package co.edu.uco.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Arrays;
import java.util.Map;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UcoBackendApplication {

    public static void main(String[] args) {
        boolean modoConsola = Arrays.asList(args).contains("--cli");

        SpringApplication aplicacion = new SpringApplication(UcoBackendApplication.class);
        if (modoConsola) {
            // La CLI no levanta el servidor web (puede correr con la app ya iniciada).
            aplicacion.setWebApplicationType(WebApplicationType.NONE);
            aplicacion.setDefaultProperties(Map.of("uco.cli", "true"));
        }
        aplicacion.run(args);
    }
}
