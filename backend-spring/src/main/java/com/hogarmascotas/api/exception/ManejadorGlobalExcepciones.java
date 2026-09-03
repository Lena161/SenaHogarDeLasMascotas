package com.hogarmascotas.api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Manejo centralizado de errores: los controladores no llevan try/catch;
 * toda excepcion se convierte aqui en una respuesta JSON uniforme
 * { "error": "..." }, el mismo contrato que usaba el prototipo Express
 * (el frontend React no necesita cambios en su manejo de errores).
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> reglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Choques con restricciones de la base de datos (segunda linea de
     * defensa de la Fase 2): p. ej. dos peticiones simultaneas que pasan
     * la validacion de RN-02 en el servicio pero chocan con la restriccion
     * unica uk_cita_empleado_horario, o un documento/correo duplicado.
     * Sin este handler la API responderia 500; con el, un 409 claro.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error",
                    "La operacion choca con una restriccion de datos "
                    + "(horario ya tomado o valor duplicado). Verifica e intenta de nuevo."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Datos invalidos.");
        return ResponseEntity.badRequest().body(Map.of("error", detalle));
    }
}

