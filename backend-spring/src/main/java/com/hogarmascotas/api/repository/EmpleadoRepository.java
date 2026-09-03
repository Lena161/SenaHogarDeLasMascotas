package com.hogarmascotas.api.repository;

import com.hogarmascotas.api.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByActivoTrue();
}
