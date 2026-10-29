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
        //
        // Se pregunta por el rol que el empleado declara y no por su clase.
        // Preguntar la clase con instanceof no es polimorfismo sino lo
        // contrario, y ademas falla cuando el objeto llega como proxy
        // perezoso de Hibernate: el proxy hereda de Empleado, no del
        // subtipo. Con getRol(), que cada subtipo implementa, el proxy
        // delega al objeto verdadero y la respuesta es correcta siempre.
        return "VETERINARIO".equals(empleado.getRol());
    }

    @Override
    public boolean requiereHistorialClinico() {
        // Toda atencion clinica deja registro en la historia (RF-09).
        return true;
    }
}
