package co.edu.uco.backend.exception;

import org.springframework.http.HttpStatus;

/** Excepción base de la API: lleva el código HTTP que debe devolverse. */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
