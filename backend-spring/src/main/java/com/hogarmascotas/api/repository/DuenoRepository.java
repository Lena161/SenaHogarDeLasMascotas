package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Dueno;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
