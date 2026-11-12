package co.edu.uco.backend.exception;

import co.edu.uco.backend.domain.EstadoUsuario;
import org.springframework.http.HttpStatus;

/** La cuenta existe, pero no está habilitada para iniciar sesión. */
public class CuentaNoHabilitadaException extends ApiException {

    public CuentaNoHabilitadaException(EstadoUsuario estado) {
        super(HttpStatus.FORBIDDEN, mensajePara(estado));
    }

    private static String mensajePara(EstadoUsuario estado) {
        return switch (estado) {
            case PENDIENTE -> "Tu registro está pendiente de aprobación por el administrador";
            case RECHAZADO -> "Tu solicitud de registro fue rechazada";
            case EXPULSADO -> "Tu cuenta fue expulsada de la plataforma";
            case ACTIVO -> "La cuenta está habilitada";
        };
    }
}
