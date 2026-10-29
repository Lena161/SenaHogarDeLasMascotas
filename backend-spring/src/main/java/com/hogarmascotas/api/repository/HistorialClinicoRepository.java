package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** PATRON REPOSITORY: acceso al historial clinico (RF-10). */
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {

    /** RF-10: historial de una mascota en orden cronologico inverso. */
    List<HistorialClinico> findByMascotaIdOrderByFechaDesc(Long mascotaId);

    /** Una cita genera a lo sumo un registro clinico. */
    Optional<HistorialClinico> findByCitaId(Long citaId);
}
