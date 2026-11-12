package co.edu.uco.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Propiedades de la aplicación (prefijo {@code uco}).
 * Se validan al arrancar: si el secreto JWT es demasiado corto, la app no inicia.
 */
@ConfigurationProperties(prefix = "uco")
@Validated
public record UcoProperties(@Valid Jwt jwt, @Valid Admin admin) {

    public record Jwt(
            @NotBlank(message = "uco.jwt.secret es obligatorio")
            @Size(min = 32, message = "uco.jwt.secret debe tener al menos 32 caracteres")
            String secret,

            @NotNull(message = "uco.jwt.ttl es obligatorio")
            Duration ttl
    ) {
    }

    public record Admin(
            @NotBlank(message = "uco.admin.email es obligatorio") String email,
            @NotBlank(message = "uco.admin.password es obligatorio") String password,
            @NotBlank(message = "uco.admin.nombre es obligatorio") String nombre
    ) {
    }
}
