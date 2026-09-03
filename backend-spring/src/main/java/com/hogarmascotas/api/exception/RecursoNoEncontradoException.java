package com.hogarmascotas.api.exception;

/** Entidad inexistente o no visible para el usuario. HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
