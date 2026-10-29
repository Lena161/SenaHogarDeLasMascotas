package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.HistorialClinico;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PATRON DTO: contrato de la atencion clinica (RF-09, RF-10).
 * La entidad HistorialClinico nunca viaja por la red.
 *
 * El formato sigue la estructura habitual de una historia clinica
 * veterinaria: anamnesis, constantes vitales, examen fisico,
 * diagnostico y plan terapeutico. Las constantes van en campos
 * propios y no dentro del texto del examen porque su utilidad
 * esta en poder compararlas entre consultas.
 */
public class HistorialDtos {

    /**
     * Datos que el profesional registra al atender una cita.
     *
     * Motivo y diagnostico son los unicos indispensables; el resto
     * depende del caso. La validacion real vive en la entidad: para
     * un servicio de spa no se exige ninguno, y es la capa de
     * servicio quien lo decide consultando
     * servicio.requiereHistorialClinico().
     */
    public record AtencionRequest(
            String motivo,
            String anamnesis,
            @DecimalMin(value = "25.0", message = "Temperatura fuera de rango plausible")
            @DecimalMax(value = "45.0", message = "Temperatura fuera de rango plausible")
            Double temperaturaC,
            @Min(10) @Max(400) Integer frecuenciaCardiaca,
            @Min(5) @Max(200) Integer frecuenciaRespiratoria,
            @Positive Double pesoKg,
            @Min(value = 1, message = "La condicion corporal va de 1 a 5")
            @Max(value = 5, message = "La condicion corporal va de 1 a 5")
            Integer condicionCorporal,
            String examenFisico,
            String diagnostico,
            String tratamiento,
            String medicamentos,
            String vacunasAplicadas,
            String desparasitacion,
            LocalDate proximoControl,
            String observaciones
    ) {
        /** Cuerpo vacio, para los servicios que no generan historia. */
        public static AtencionRequest vacia() {
            return new AtencionRequest(null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
        }
    }

    /**
     * Alta de un registro clinico sin cita previa (atencion de
     * urgencia o consulta de mostrador). Aqui motivo y diagnostico
     * si son obligatorios, porque no hay servicio que lo decida.
     */
    public record HistorialDirectoRequest(
            @NotBlank(message = "El motivo de la consulta es obligatorio")
            @Size(max = 255) String motivo,
            @Size(max = 1000) String anamnesis,
            @DecimalMin(value = "25.0", message = "Temperatura fuera de rango plausible")
            @DecimalMax(value = "45.0", message = "Temperatura fuera de rango plausible")
            Double temperaturaC,
            @Min(10) @Max(400) Integer frecuenciaCardiaca,
            @Min(5) @Max(200) Integer frecuenciaRespiratoria,
            @Positive Double pesoKg,
            @Min(1) @Max(5) Integer condicionCorporal,
            @Size(max = 1000) String examenFisico,
            @NotBlank(message = "El diagnostico es obligatorio")
            @Size(max = 500) String diagnostico,
            @Size(max = 500) String tratamiento,
            @Size(max = 500) String medicamentos,
            @Size(max = 255) String vacunasAplicadas,
            @Size(max = 255) String desparasitacion,
            LocalDate proximoControl,
            @Size(max = 500) String observaciones
    ) {}

    public record HistorialResponse(
            Long id,
            Long mascotaId,
            String mascota,
            Long citaId,
            String servicio,
            String veterinario,
            LocalDateTime fecha,
            String motivo,
            String anamnesis,
            Double temperaturaC,
            Integer frecuenciaCardiaca,
            Integer frecuenciaRespiratoria,
            Double pesoKg,
            Integer condicionCorporal,
            String examenFisico,
            String diagnostico,
            String tratamiento,
            String medicamentos,
            String vacunasAplicadas,
            String desparasitacion,
            LocalDate proximoControl,
            String observaciones
    ) {
        public static HistorialResponse desde(HistorialClinico h) {
            return new HistorialResponse(
                    h.getId(),
                    h.getMascota().getId(),
                    h.getMascota().getNombre(),
                    h.getCita() == null ? null : h.getCita().getId(),
                    h.getCita() == null ? null : h.getCita().getServicio().getNombre(),
                    h.getVeterinario().getNombreCompleto(),
                    h.getFecha(),
                    h.getMotivo(),
                    h.getAnamnesis(),
                    h.getTemperaturaC(),
                    h.getFrecuenciaCardiaca(),
                    h.getFrecuenciaRespiratoria(),
                    h.getPesoKg(),
                    h.getCondicionCorporal(),
                    h.getExamenFisico(),
                    h.getDiagnostico(),
                    h.getTratamiento(),
                    h.getMedicamentos(),
                    h.getVacunasAplicadas(),
                    h.getDesparasitacion(),
                    h.getProximoControl(),
                    h.getObservaciones());
        }
    }
}
