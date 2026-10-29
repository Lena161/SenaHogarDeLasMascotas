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
            Long mascotaId,
            String mascota,
            String dueno,
            String telefonoDueno,
            String servicio,
            int duracionMinutos,
            boolean requiereHistorial,
            String profesional,
            LocalDateTime fechaHora,
            String estado,
            BigDecimal precioFinal,
            String observaciones
    ) {
        /**
         * Fabrica: traduce la entidad al contrato publico.
         * Los datos del dueno sirven a la agenda de recepcion (RF-08);
         * un cliente solo recibe por esta via sus propias citas, de modo
         * que nunca ve el contacto de otro cliente.
         */
        public static CitaResponse desde(Cita c) {
            return new CitaResponse(
                    c.getId(),
                    c.getMascota().getId(),
                    c.getMascota().getNombre(),
                    c.getMascota().getDueno().getNombreCompleto(),
                    c.getMascota().getDueno().getTelefono(),
                    c.getServicio().getNombre(),
                    c.getServicio().getDuracionMinutos(),
                    c.getServicio().requiereHistorialClinico(),
                    c.getEmpleado().getNombreCompleto(),
                    c.getFechaHora(),
                    c.getEstado().name(),
                    c.getPrecioFinal(),
                    c.getObservaciones());
        }
    }
}
