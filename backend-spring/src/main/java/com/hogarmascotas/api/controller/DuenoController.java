package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.DuenoDtos.*;
import com.hogarmascotas.api.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion de clientes (RF-03). Todo el controlador es de uso interno:
 * ningun cliente accede a la ficha de otro.
 */
@RestController
@RequestMapping("/api/v1/duenos")
@PreAuthorize("hasAnyRole('ADMIN','RECEPCION','VETERINARIO')")
public class DuenoController {

    private final ClienteService clienteService;

    public DuenoController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<DuenoResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "false") boolean incluirInactivos) {
        return clienteService.buscar(q, incluirInactivos);
    }

    @GetMapping("/{id}")
    public DuenoDetalleResponse detalle(@PathVariable Long id) {
        return clienteService.detalle(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public ResponseEntity<DuenoResponse> crear(@Valid @RequestBody DuenoAltaRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(datos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public DuenoResponse actualizar(@PathVariable Long id,
                                    @Valid @RequestBody DuenoRequest datos) {
        return clienteService.actualizar(id, datos);
    }

    /** Baja LOGICA: conserva citas e historial clinico. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DuenoResponse desactivar(@PathVariable Long id) {
        return clienteService.cambiarEstado(id, false);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public DuenoResponse reactivar(@PathVariable Long id) {
        return clienteService.cambiarEstado(id, true);
    }
}
