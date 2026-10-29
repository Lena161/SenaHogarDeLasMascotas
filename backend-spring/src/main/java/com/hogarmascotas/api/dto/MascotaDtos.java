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
            String tamano, int edad,
            // Datos del dueno: el personal clinico necesita saber a quien
            // pertenece el paciente y a que numero llamar. El cliente solo
            // recibe por esta via sus propias mascotas.
            Long duenoId, String dueno, String telefonoDueno
    ) {
        public static MascotaResponse desde(Mascota m) {
            return new MascotaResponse(m.getId(), m.getNombre(), m.getEspecie(),
                    m.getRaza(), m.getFechaNacimiento(), m.getSexo(), m.getPesoKg(),
                    m.getTamano().name(), m.calcularEdad(),
                    m.getDueno().getId(), m.getDueno().getNombreCompleto(),
                    m.getDueno().getTelefono());
        }
    }
}
