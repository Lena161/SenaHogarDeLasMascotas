package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Citas de un profesional en una ventana de tiempo, por estados (para RN-02). */
    List<Cita> findByEmpleadoIdAndEstadoInAndFechaHoraBetween(
            Long empleadoId,
            Collection<Cita.Estado> estados,
            LocalDateTime desde,
            LocalDateTime hasta);

    /** Citas de una mascota en una ventana de tiempo, por estados (para RN-08). */
    List<Cita> findByMascotaIdAndEstadoInAndFechaHoraBetween(
            Long mascotaId,
            Collection<Cita.Estado> estados,
            LocalDateTime desde,
            LocalDateTime hasta);

    /** Citas de todas las mascotas de un dueno (para "Mis citas"). */
    List<Cita> findByMascotaDuenoIdOrderByFechaHoraAsc(Long duenoId);

    /** RF-08: agenda completa de la clinica en una ventana de tiempo. */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(
            LocalDateTime desde, LocalDateTime hasta);

    /** RF-08: agenda de un profesional en una ventana de tiempo. */
    List<Cita> findByEmpleadoIdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long empleadoId, LocalDateTime desde, LocalDateTime hasta);

    /** Historial de citas de una mascota (ficha del paciente, RF-03). */
    List<Cita> findByMascotaIdOrderByFechaHoraDesc(Long mascotaId);
}
