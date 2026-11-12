package co.edu.uco.backend.exception;

import org.springframework.http.HttpStatus;

/** Se intentó una operación que las reglas del negocio no permiten. */
public class ReglaNegocioException extends ApiException {

    public ReglaNegocioException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
