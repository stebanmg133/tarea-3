package co.edu.uco.backend.domain;

public enum EstadoUsuario {
    /** Registrado, esperando aprobación del administrador. */
    PENDIENTE,
    /** Aprobado, puede iniciar sesión. */
    ACTIVO,
    /** El administrador rechazó la solicitud de registro. */
    RECHAZADO,
    /** Fue expulsado por el administrador; no puede iniciar sesión. */
    EXPULSADO
}
