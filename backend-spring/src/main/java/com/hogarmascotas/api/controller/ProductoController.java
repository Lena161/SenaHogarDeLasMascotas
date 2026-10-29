package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.InventarioDtos.MovimientoRequest;
import com.hogarmascotas.api.dto.InventarioDtos.MovimientoResponse;
import com.hogarmascotas.api.dto.InventarioDtos.ProductoRequest;
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

    /**
     * Catalogo publico (lo consume la pagina Productos del frontend).
     * Con todos=true devuelve tambien los descontinuados, para la
     * vista de administracion.
     */
    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false, defaultValue = "false") boolean todos) {
        return todos ? inventarioService.listarTodos() : inventarioService.listarActivos();
    }

    /** RF-13: productos en o por debajo del stock minimo. */
    @GetMapping("/por-reponer")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public List<ProductoResponse> porReponer() {
        return inventarioService.porReponer();
    }

    /** RF-11: alta de producto. Nace en cero; se carga con una entrada. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(datos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest datos) {
        return inventarioService.actualizar(id, datos);
    }

    /** RF-11: baja LOGICA del catalogo. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse descontinuar(@PathVariable Long id) {
        return inventarioService.cambiarEstado(id, false);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse reactivar(@PathVariable Long id) {
        return inventarioService.cambiarEstado(id, true);
    }

    /** RNF-10: trazabilidad de los movimientos del producto. */
    @GetMapping("/{id}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public List<MovimientoResponse> movimientos(@PathVariable Long id) {
        return inventarioService.movimientosDe(id);
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
