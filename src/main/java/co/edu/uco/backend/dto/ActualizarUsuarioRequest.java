package co.edu.uco.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * Edición de datos de un usuario por parte del administrador.
 * Los campos nulos significan "no cambiar".
 */
public record ActualizarUsuarioRequest(

        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombreCompleto,

        @Size(max = 80, message = "El aula no puede superar 80 caracteres")
        String aula,

        @Size(max = 80, message = "La materia no puede superar 80 caracteres")
        String materia,

        @Size(max = 30, message = "El código no puede superar 30 caracteres")
        String codigoEstudiantil
) {
}
