package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Dueno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * PATRON REPOSITORY: la capa de servicio trabaja contra esta interfaz
 * sin conocer SQL; Spring Data genera la implementacion. Esto permite
 * ademas probar los servicios con repositorios simulados (Mockito).
 */
public interface DuenoRepository extends JpaRepository<Dueno, Long> {
    Optional<Dueno> findByCorreo(String correo);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByCorreo(String correo);

    /**
     * RF-03: busqueda por nombre, apellido, documento, telefono o correo.
     * Una sola caja de busqueda en la interfaz: en recepcion no se sabe
     * de antemano con que dato llega el cliente. Con el texto vacio
     * devuelve el listado completo.
     */
    @Query("""
           SELECT d FROM Dueno d
            WHERE (:texto IS NULL OR :texto = ''
                   OR LOWER(d.nombres)   LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(d.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR d.numeroDocumento  LIKE CONCAT('%', :texto, '%')
                   OR d.telefono         LIKE CONCAT('%', :texto, '%')
                   OR LOWER(d.correo)    LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:incluirInactivos = true OR d.activo = true)
            ORDER BY d.apellidos ASC, d.nombres ASC
           """)
    List<Dueno> buscar(@Param("texto") String texto,
                       @Param("incluirInactivos") boolean incluirInactivos);
}
