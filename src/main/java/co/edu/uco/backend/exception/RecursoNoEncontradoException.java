package co.edu.uco.backend.exception;

import org.springframework.http.HttpStatus;

/** El recurso solicitado no existe. */
public class RecursoNoEncontradoException extends ApiException {

    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }

    public static RecursoNoEncontradoException de(String recurso, Object id) {
        return new RecursoNoEncontradoException(recurso + " no encontrado(a) con id " + id);
    }
}
