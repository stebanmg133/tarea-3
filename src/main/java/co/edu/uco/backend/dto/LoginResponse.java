package co.edu.uco.backend.dto;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;

import java.time.Instant;

/** Respuesta del login: token JWT + datos básicos del usuario. */
public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        Long usuarioId,
        String nombreCompleto,
        String email,
        Rol rol,
        EstadoUsuario estado
) {
}
