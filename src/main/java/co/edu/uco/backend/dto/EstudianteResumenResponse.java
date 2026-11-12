package co.edu.uco.backend.dto;

import java.math.BigDecimal;

/** Estudiante visto por un profesor, con su promedio actual. */
public record EstudianteResumenResponse(
        Long estudianteId,
        String nombreCompleto,
        String email,
        String aula,
        String codigoEstudiantil,
        BigDecimal promedio
) {
}
