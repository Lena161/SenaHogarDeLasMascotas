package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.ServicioDtos.ServicioResponse;
import com.hogarmascotas.api.repository.ServicioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Catalogo publico de servicios (RF-04, lectura). */
@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioRepository servicioRepo;

    public ServicioController(ServicioRepository servicioRepo) {
        this.servicioRepo = servicioRepo;
    }

    @GetMapping
    public List<ServicioResponse> listar() {
        return servicioRepo.findByActivoTrue().stream()
                .map(ServicioResponse::desde).toList();
    }
}
