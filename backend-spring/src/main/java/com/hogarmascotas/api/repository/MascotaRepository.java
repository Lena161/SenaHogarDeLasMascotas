package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByDuenoIdAndActivoTrue(Long duenoId);

    /**
     * RF-03: busqueda de pacientes para el personal clinico.
     * Se busca por el nombre del paciente y tambien por el de su dueno,
     * porque en la practica el profesional recuerda cualquiera de los
     * dos: "la gata de don Carlos" es una forma legitima de buscar.
     */
    @Query("""
           SELECT m FROM Mascota m
            WHERE m.activo = true
              AND (:texto IS NULL OR :texto = ''
                   OR LOWER(m.nombre)           LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(m.dueno.nombres)    LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(m.dueno.apellidos)  LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR m.dueno.numeroDocumento   LIKE CONCAT('%', :texto, '%'))
            ORDER BY m.nombre ASC
           """)
    List<Mascota> buscar(@Param("texto") String texto);
}
