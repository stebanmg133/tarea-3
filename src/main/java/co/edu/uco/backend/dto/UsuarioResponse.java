package co.edu.uco.backend.dto;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;

import java.time.Instant;

/**
 * Vista de un usuario. Los campos académicos (aula, materia, código) llegan solo
 * cuando aplican al rol, de lo contrario son {@code null}.
 */
public record UsuarioResponse(
        Long id,
        String nombreCompleto,
        String email,
        Rol rol,
        EstadoUsuario estado,
        Instant creadoEn,
        String aula,
        String materia,
        String codigoEstudiantil
) {
}
