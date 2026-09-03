package com.hogarmascotas.api.exception;

/** Violacion de una regla de negocio (RN-xx). Se traduce a HTTP 409. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
