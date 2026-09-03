package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.CitaDtos.*;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.service.AuthService;
import com.hogarmascotas.api.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;
    private final AuthService authService;

    public CitaController(CitaService citaService, AuthService authService) {
        this.citaService = citaService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CrearCitaRequest datos,
                                              Authentication auth) {
        Usuario solicitante = authService.obtenerPorUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citaService.crear(datos, solicitante));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<CitaResponse> mias(Authentication auth) {
        return citaService.misCitas(authService.obtenerPorUsername(auth.getName()));
    }

    /** Evolucion del DELETE del prototipo: cancelacion LOGICA (RN-06). */
    @PatchMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id, Authentication auth) {
        return citaService.cancelar(id, authService.obtenerPorUsername(auth.getName()));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public CitaResponse confirmar(@PathVariable Long id) {
        return citaService.confirmar(id);
    }

    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAnyRole('VETERINARIO','ESTETICISTA','ADMIN')")
    public CitaResponse atender(@PathVariable Long id) {
        return citaService.atender(id);
    }
}
