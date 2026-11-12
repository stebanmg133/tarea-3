package co.edu.uco.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UcoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UcoBackendApplication.class, args);
    }
}
