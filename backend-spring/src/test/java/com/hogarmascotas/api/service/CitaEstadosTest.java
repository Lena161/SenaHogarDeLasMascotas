package com.hogarmascotas.api.service;

import com.hogarmascotas.api.entity.Cita;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ENCAPSULAMIENTO en accion: la entidad Cita no tiene setEstado();
 * estas pruebas verifican que la maquina de estados de la Fase 1
 * (seccion 3.5) y la regla RN-06 estan protegidas por el objeto.
 */
class CitaEstadosTest {

    @Test
    @DisplayName("Flujo feliz: PENDIENTE -> CONFIRMADA -> ATENDIDA")
    void flujoNormal() {
        Cita cita = new Cita();
        assertEquals(Cita.Estado.PENDIENTE, cita.getEstado());

        cita.confirmar();
        assertEquals(Cita.Estado.CONFIRMADA, cita.getEstado());

        cita.atender();
        assertEquals(Cita.Estado.ATENDIDA, cita.getEstado());
    }

    @Test
    @DisplayName("RN-06: una cita ATENDIDA no puede cancelarse")
    void atendidaNoSeCancela() {
        Cita cita = new Cita();
        cita.confirmar();
        cita.atender();

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, cita::cancelar);
        assertTrue(error.getMessage().contains("RN-06"));
    }

    @Test
    @DisplayName("RN-06: no se puede atender una cita sin confirmar")
    void pendienteNoSeAtiende() {
        Cita cita = new Cita();
        assertThrows(ReglaNegocioException.class, cita::atender);
    }

    @Test
    @DisplayName("Una cita CANCELADA es terminal: no admite mas transiciones")
    void canceladaEsTerminal() {
        Cita cita = new Cita();
        cita.cancelar();

        assertThrows(ReglaNegocioException.class, cita::confirmar);
        assertThrows(ReglaNegocioException.class, cita::atender);
        assertThrows(ReglaNegocioException.class, cita::cancelar);
    }

    @Test
    @DisplayName("RF-07: una cita CONFIRMADA puede marcarse como NO_ASISTIO")
    void inasistenciaDesdeConfirmada() {
        Cita cita = new Cita();
        cita.confirmar();

        cita.marcarInasistencia();
        assertEquals(Cita.Estado.NO_ASISTIO, cita.getEstado());
    }

    @Test
    @DisplayName("RF-07: solo se marca inasistencia sobre una cita CONFIRMADA")
    void inasistenciaExigeConfirmacion() {
        // Una cita que nunca se confirmo no puede "no asistirse":
        // la clinica no reservo el espacio todavia.
        Cita pendiente = new Cita();
        assertThrows(ReglaNegocioException.class, pendiente::marcarInasistencia);

        Cita atendida = new Cita();
        atendida.confirmar();
        atendida.atender();
        assertThrows(ReglaNegocioException.class, atendida::marcarInasistencia);
    }

    @Test
    @DisplayName("NO_ASISTIO es terminal")
    void inasistenciaEsTerminal() {
        Cita cita = new Cita();
        cita.confirmar();
        cita.marcarInasistencia();

        assertThrows(ReglaNegocioException.class, cita::atender);
        assertThrows(ReglaNegocioException.class, cita::cancelar);
        assertThrows(ReglaNegocioException.class, cita::confirmar);
    }
}
