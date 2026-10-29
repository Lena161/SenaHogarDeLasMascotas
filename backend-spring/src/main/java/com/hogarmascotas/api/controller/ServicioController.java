package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.ServicioDtos.ServicioRequest;
import com.hogarmascotas.api.dto.ServicioDtos.ServicioResponse;
import com.hogarmascotas.api.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catalogo de servicios (RF-04).
 * La lectura del catalogo activo es publica: alimenta la pagina de
 * servicios y el formulario de reserva. La gestion es del administrador.
 */
@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    /** Con todos=true devuelve tambien los retirados (vista de administracion). */
    @GetMapping
    public List<ServicioResponse> listar(
            @RequestParam(required = false, defaultValue = "false") boolean todos) {
        return todos ? servicioService.listarTodos() : servicioService.listarActivos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(datos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ServicioRequest datos) {
        return servicioService.actualizar(id, datos);
    }

    /** Retiro LOGICO del catalogo: el servicio deja de ofrecerse. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse desactivar(@PathVariable Long id) {
        return servicioService.cambiarEstado(id, false);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse reactivar(@PathVariable Long id) {
        return servicioService.cambiarEstado(id, true);
    }
}
