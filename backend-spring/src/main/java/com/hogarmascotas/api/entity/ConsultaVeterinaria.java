package com.hogarmascotas.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/** Servicio clinico: precio fijo, atendido solo por veterinarios. */
@Entity
@DiscriminatorValue("CONSULTA")
public class ConsultaVeterinaria extends Servicio {

    @Override
    public BigDecimal calcularPrecio(Mascota.Tamano tamano) {
        // La consulta clinica no varia por tamano de la mascota.
        return getPrecioBase();
    }

    @Override
    public boolean puedeSerAtendidoPor(Empleado empleado) {
        // RN-04: solo un veterinario puede atender una consulta.
        return empleado instanceof Veterinario;
    }
}
