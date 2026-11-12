package co.edu.uco.backend.dto;

/** Conteos generales para el panel del administrador. */
public record ResumenAdminResponse(
        long estudiantes,
        long profesores,
        long administradores,
        long pendientes,
        long activos,
        long rechazados,
        long expulsados,
        long notas
) {
}
