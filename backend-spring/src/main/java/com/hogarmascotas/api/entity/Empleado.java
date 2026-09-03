package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Jerarquia de empleados (Fase 1, seccion 4.2; Fase 2, decision 2.1).
 *
 * EVIDENCIA POO - HERENCIA con estrategia SINGLE_TABLE: toda la
 * jerarquia persiste en la tabla `empleado` y la columna `rol`
 * discrimina el subtipo, exactamente como se diseno en la Fase 2.
 */
@Entity
@Table(name = "empleado")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
public abstract class Empleado extends Persona {

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio = LocalTime.of(8, 0);

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin = LocalTime.of(18, 0);

    @Column(nullable = false)
    private boolean activo = true;

    /** Cada subtipo declara su rol (usado por seguridad y RN-04). */
    public abstract String getRol();

    /**
     * RN-01 (parte de jornada): el empleado atiende dentro de su horario
     * y no los domingos. Metodo heredado por todos los subtipos.
     */
    public boolean estaDisponible(LocalDateTime inicio, int duracionMinutos) {
        if (inicio.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
        LocalTime horaCita = inicio.toLocalTime();
        LocalTime finCita = horaCita.plusMinutes(duracionMinutos);
        return !horaCita.isBefore(horaInicio) && !finCita.isAfter(horaFin);
    }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
