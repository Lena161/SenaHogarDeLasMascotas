package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.AuthDtos.*;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PATRON MVC: el controlador solo traduce HTTP <-> servicio.
 * Ninguna regla de negocio vive aqui (restriccion de diseno, Fase 1 §6.3).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<SesionResponse> registro(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrarCliente(datos));
    }

    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.iniciarSesion(datos);
    }

    /** Equivalente al GET /api/me del prototipo, ahora via token. */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication autenticacion) {
        Usuario usuario = authService.obtenerPorUsername(autenticacion.getName());
        String nombre = usuario.getDueno() != null
                ? usuario.getDueno().getNombreCompleto()
                : usuario.getEmpleado().getNombreCompleto();
        return Map.of(
                "id", usuario.getId(),
                "nombre", nombre,
                "username", usuario.getUsername(),
                "rol", usuario.getRol().name());
    }
}
