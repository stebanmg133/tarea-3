package co.edu.uco.backend.exception;

import org.springframework.http.HttpStatus;

/** El usuario está autenticado, pero no tiene permiso sobre el recurso. */
public class AccesoDenegadoException extends ApiException {

    public AccesoDenegadoException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }
}
