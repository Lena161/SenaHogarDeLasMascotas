package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.InventarioDtos.MovimientoRequest;
import com.hogarmascotas.api.dto.InventarioDtos.ProductoResponse;
import com.hogarmascotas.api.service.AuthService;
import com.hogarmascotas.api.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final InventarioService inventarioService;
    private final AuthService authService;

    public ProductoController(InventarioService inventarioService, AuthService authService) {
        this.inventarioService = inventarioService;
        this.authService = authService;
    }

    /** Catalogo publico (lo consume la pagina Productos del frontend). */
    @GetMapping
    public List<ProductoResponse> listar() {
        return inventarioService.listarActivos();
    }

    /** RF-12: registrar movimiento. Solo personal autorizado (RNF-04). */
    @PostMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public ResponseEntity<ProductoResponse> movimiento(@Valid @RequestBody MovimientoRequest datos,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventarioService.registrarMovimiento(
                        datos, authService.obtenerPorUsername(auth.getName())));
    }
}
