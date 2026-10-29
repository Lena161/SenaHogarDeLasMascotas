package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.ServicioDtos.*;
import com.hogarmascotas.api.entity.ConsultaVeterinaria;
import com.hogarmascotas.api.entity.Servicio;
import com.hogarmascotas.api.entity.ServicioSpa;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catalogo de servicios (RF-04).
 *
 * El alta es el unico punto del sistema donde se decide la subclase
 * concreta. A partir de ahi el resto del codigo trabaja contra la
 * abstraccion Servicio y el POLIMORFISMO resuelve precio, perfil
 * autorizado e historia clinica.
 */
@Service
public class ServicioService {

    private final ServicioRepository servicioRepo;

    public ServicioService(ServicioRepository servicioRepo) {
        this.servicioRepo = servicioRepo;
    }

    /** Catalogo publico: solo lo que se puede reservar hoy. */
    public List<ServicioResponse> listarActivos() {
        return servicioRepo.findByActivoTrue().stream()
                .map(ServicioResponse::desde).toList();
    }

    /** Vista de administracion: incluye los servicios retirados. */
    public List<ServicioResponse> listarTodos() {
        return servicioRepo.findAll().stream()
                .map(ServicioResponse::desde).toList();
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest datos) {
        servicioRepo.findByActivoTrue().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(datos.nombre().trim()))
                .findAny()
                .ifPresent(s -> { throw new ReglaNegocioException(
                        "Ya existe un servicio activo llamado '" + s.getNombre() + "'."); });

        // Unico switch por tipo del sistema: la fabrica. Despues de
        // esta linea nadie vuelve a preguntar de que tipo es.
        Servicio servicio = switch (datos.tipo()) {
            case CONSULTA -> new ConsultaVeterinaria();
            case SPA -> new ServicioSpa();
        };
        aplicar(servicio, datos);
        return ServicioResponse.desde(servicioRepo.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest datos) {
        Servicio servicio = servicioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado."));

        // El tipo no se puede cambiar: hay citas historicas que dependen
        // de como se calculo su precio y de quien las atendio.
        boolean esSpa = servicio instanceof ServicioSpa;
        boolean pideSpa = datos.tipo() == ServicioRequest.Tipo.SPA;
        if (esSpa != pideSpa) {
            throw new ReglaNegocioException(
                "El tipo de un servicio no puede cambiarse. Desactive este y cree uno nuevo.");
        }
        aplicar(servicio, datos);
        return ServicioResponse.desde(servicio);
    }

    /**
     * RF-04: el retiro es logico. Un servicio con citas historicas no
     * puede borrarse sin perder la trazabilidad de esas atenciones.
     */
    @Transactional
    public ServicioResponse cambiarEstado(Long id, boolean activo) {
        Servicio servicio = servicioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado."));
        servicio.setActivo(activo);
        return ServicioResponse.desde(servicio);
    }

    private void aplicar(Servicio servicio, ServicioRequest datos) {
        servicio.setNombre(datos.nombre().trim());
        servicio.setDescripcion(datos.descripcion());
        servicio.setDuracionMinutos(datos.duracionMinutos());
        servicio.setPrecioBase(datos.precioBase());
        if (servicio instanceof ServicioSpa spa) {
            spa.setRecargoPorTamano(
                    datos.recargoPorTamano() == null ? BigDecimal.ZERO : datos.recargoPorTamano());
        }
    }
}
