package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByDuenoIdAndActivoTrue(Long duenoId);
}
