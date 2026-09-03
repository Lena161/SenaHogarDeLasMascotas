package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.Cita;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CitaDtos {

    public record CrearCitaRequest(
            @NotNull Long mascotaId,
            @NotNull Long servicioId,
            @NotNull Long empleadoId,
            @NotNull @Future(message = "La cita debe ser en una fecha futura") LocalDateTime fechaHora,
            String observaciones
    ) {}

    public record CitaResponse(
            Long id,
            String mascota,
            String servicio,
            String profesional,
            LocalDateTime fechaHora,
            String estado,
            BigDecimal precioFinal,
            String observaciones
    ) {
        /** Fabrica: traduce la entidad al contrato publico. */
        public static CitaResponse desde(Cita c) {
            return new CitaResponse(
                    c.getId(),
                    c.getMascota().getNombre(),
                    c.getServicio().getNombre(),
                    c.getEmpleado().getNombreCompleto(),
                    c.getFechaHora(),
                    c.getEstado().name(),
                    c.getPrecioFinal(),
                    c.getObservaciones());
        }
    }
}
