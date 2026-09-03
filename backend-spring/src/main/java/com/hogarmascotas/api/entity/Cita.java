package com.hogarmascotas.api.entity;

import com.hogarmascotas.api.exception.ReglaNegocioException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reserva de un servicio para una mascota con un profesional.
 *
 * EVIDENCIA POO - ENCAPSULAMIENTO REAL: el estado NO tiene setter
 * publico. Las unicas formas de cambiarlo son los metodos de
 * transicion (confirmar, cancelar, atender, marcarInasistencia),
 * que implementan el diagrama de estados de la Fase 1 (seccion 3.5)
 * y protegen la regla RN-06: el objeto cuida sus invariantes.
 */
@Entity
@Table(name = "cita")
public class Cita {

    public enum Estado { PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "precio_final", precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(length = 255)
    private String observaciones;

    // ---------- Transiciones de estado (RN-06) ----------

    public void confirmar() {
        exigirEstado(Estado.PENDIENTE, "confirmar");
        this.estado = Estado.CONFIRMADA;
    }

    public void cancelar() {
        if (estado != Estado.PENDIENTE && estado != Estado.CONFIRMADA) {
            throw new ReglaNegocioException(
                "RN-06: una cita en estado " + estado + " no puede cancelarse.");
        }
        this.estado = Estado.CANCELADA;
    }

    public void atender() {
        exigirEstado(Estado.CONFIRMADA, "atender");
        this.estado = Estado.ATENDIDA;
    }

    public void marcarInasistencia() {
        exigirEstado(Estado.CONFIRMADA, "marcar inasistencia de");
        this.estado = Estado.NO_ASISTIO;
    }

    private void exigirEstado(Estado requerido, String accion) {
        if (estado != requerido) {
            throw new ReglaNegocioException(
                "RN-06: solo se puede " + accion + " una cita " + requerido
                + " (estado actual: " + estado + ").");
        }
    }

    // ---------- Getters / setters (sin setEstado publico) ----------
    public Long getId() { return id; }
    public Mascota getMascota() { return mascota; }
    public void setMascota(Mascota mascota) { this.mascota = mascota; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Estado getEstado() { return estado; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(BigDecimal precioFinal) { this.precioFinal = precioFinal; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
