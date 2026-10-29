package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.CitaDtos.CitaResponse;
import com.hogarmascotas.api.dto.HistorialDtos.HistorialDirectoRequest;
import com.hogarmascotas.api.dto.HistorialDtos.HistorialResponse;
import com.hogarmascotas.api.dto.MascotaDtos.*;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.service.AuthService;
import com.hogarmascotas.api.service.CitaService;
import com.hogarmascotas.api.service.HistorialService;
import com.hogarmascotas.api.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pacientes (RF-02, RF-03) y su historia clinica (RF-09, RF-10).
 *
 * Las rutas son las mismas para el cliente y para el personal; quien
 * decide que puede ver cada uno es la capa de servicio, comparando el
 * dueno de la mascota con el usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final HistorialService historialService;
    private final CitaService citaService;
    private final AuthService authService;

    public MascotaController(MascotaService mascotaService, HistorialService historialService,
                             CitaService citaService, AuthService authService) {
        this.mascotaService = mascotaService;
        this.historialService = historialService;
        this.citaService = citaService;
        this.authService = authService;
    }

    /**
     * Sin parametros, las mascotas del cliente autenticado.
     * Con duenoId, las de ese cliente (uso del personal).
     */
    @GetMapping
    public List<MascotaResponse> listar(@RequestParam(required = false) Long duenoId,
                                        Authentication auth) {
        Usuario solicitante = authService.obtenerPorUsername(auth.getName());
        if (solicitante.getRol() == Usuario.Rol.CLIENTE) {
            return mascotaService.mias(solicitante); // sus propias mascotas
        }
        // El personal consulta las de un cliente concreto. Sin ese dato
        // no hay nada que listar: un empleado no tiene mascotas propias.
        return duenoId == null ? List.of() : mascotaService.deDueno(duenoId);
    }

    /** RF-03: busqueda de pacientes por nombre del paciente o del dueno. */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION','VETERINARIO','ESTETICISTA')")
    public List<MascotaResponse> buscar(@RequestParam(required = false) String q) {
        return mascotaService.buscar(q);
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> crear(@Valid @RequestBody MascotaRequest datos,
                                                 @RequestParam(required = false) Long duenoId,
                                                 Authentication auth) {
        Usuario solicitante = authService.obtenerPorUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mascotaService.crear(datos, duenoId, solicitante));
    }

    @PutMapping("/{id}")
    public MascotaResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody MascotaRequest datos,
                                      Authentication auth) {
        return mascotaService.actualizar(id, datos,
                authService.obtenerPorUsername(auth.getName()));
    }

    /** Baja LOGICA: conserva citas e historia clinica. */
    @DeleteMapping("/{id}")
    public MascotaResponse desactivar(@PathVariable Long id, Authentication auth) {
        return mascotaService.cambiarEstado(id, false,
                authService.obtenerPorUsername(auth.getName()));
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public MascotaResponse reactivar(@PathVariable Long id, Authentication auth) {
        return mascotaService.cambiarEstado(id, true,
                authService.obtenerPorUsername(auth.getName()));
    }

    /** RF-10: historia clinica del paciente, de la mas reciente a la mas antigua. */
    @GetMapping("/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Long id, Authentication auth) {
        return historialService.deMascota(id, authService.obtenerPorUsername(auth.getName()));
    }

    /** RF-09: entrada clinica sin cita previa (urgencia). */
    @PostMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    public ResponseEntity<HistorialResponse> registrarHistorial(
            @PathVariable Long id,
            @Valid @RequestBody HistorialDirectoRequest datos,
            Authentication auth) {
        Usuario profesional = authService.obtenerPorUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historialService.registrarDirecto(id, datos, profesional));
    }

    /** RF-03: citas del paciente, para la ficha que consulta el personal. */
    @GetMapping("/{id}/citas")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION','VETERINARIO','ESTETICISTA')")
    public List<CitaResponse> citas(@PathVariable Long id) {
        return citaService.deMascota(id);
    }
}
