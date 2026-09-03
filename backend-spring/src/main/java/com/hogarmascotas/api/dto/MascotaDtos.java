package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.Mascota;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class MascotaDtos {

    public record MascotaRequest(
            @NotBlank String nombre,
            @NotBlank String especie,
            String raza,
            LocalDate fechaNacimiento,
            @NotBlank String sexo,
            Double pesoKg,
            @NotNull Mascota.Tamano tamano
    ) {}

    public record MascotaResponse(
            Long id, String nombre, String especie, String raza,
            LocalDate fechaNacimiento, String sexo, Double pesoKg,
            String tamano, int edad
    ) {
        public static MascotaResponse desde(Mascota m) {
            return new MascotaResponse(m.getId(), m.getNombre(), m.getEspecie(),
                    m.getRaza(), m.getFechaNacimiento(), m.getSexo(), m.getPesoKg(),
                    m.getTamano().name(), m.calcularEdad());
        }
    }
}
