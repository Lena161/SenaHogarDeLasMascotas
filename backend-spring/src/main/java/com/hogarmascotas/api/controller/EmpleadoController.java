package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.entity.Empleado;
import com.hogarmascotas.api.repository.EmpleadoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Listado publico de profesionales activos: el formulario de reserva
 * lo necesita para elegir profesional (solo nombre y rol; los datos
 * personales no se exponen - decision del patron DTO).
 */
@RestController
@RequestMapping("/api/v1/empleados")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepo;

    public EmpleadoController(EmpleadoRepository empleadoRepo) {
        this.empleadoRepo = empleadoRepo;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return empleadoRepo.findByActivoTrue().stream()
                .filter(e -> e.getRol().equals("VETERINARIO") || e.getRol().equals("ESTETICISTA"))
                .map(this::resumen)
                .toList();
    }

    private Map<String, Object> resumen(Empleado e) {
        return Map.of("id", e.getId(), "nombre", e.getNombreCompleto(), "rol", e.getRol());
    }
}
