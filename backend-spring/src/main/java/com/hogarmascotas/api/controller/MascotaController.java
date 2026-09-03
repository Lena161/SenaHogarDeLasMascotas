package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.MascotaDtos.*;
import com.hogarmascotas.api.service.AuthService;
import com.hogarmascotas.api.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mascotas")
@PreAuthorize("hasRole('CLIENTE')")
public class MascotaController {

    private final MascotaService mascotaService;
    private final AuthService authService;

    public MascotaController(MascotaService mascotaService, AuthService authService) {
        this.mascotaService = mascotaService;
        this.authService = authService;
    }

    @GetMapping
    public List<MascotaResponse> mias(Authentication auth) {
        return mascotaService.mias(authService.obtenerPorUsername(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> crear(@Valid @RequestBody MascotaRequest datos,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mascotaService.crear(datos, authService.obtenerPorUsername(auth.getName())));
    }
}
