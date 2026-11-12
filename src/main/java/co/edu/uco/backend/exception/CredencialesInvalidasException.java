package co.edu.uco.backend.exception;

import org.springframework.http.HttpStatus;

/** Correo o contraseña incorrectos (no se revela cuál de los dos falló). */
public class CredencialesInvalidasException extends ApiException {

    public CredencialesInvalidasException() {
        super(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }
}
