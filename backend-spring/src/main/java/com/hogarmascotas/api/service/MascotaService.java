package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.MascotaDtos.*;
import com.hogarmascotas.api.entity.Dueno;
import com.hogarmascotas.api.entity.Mascota;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.repository.DuenoRepository;
import com.hogarmascotas.api.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Pacientes de la clinica (RF-02, RF-03).
 *
 * El mismo servicio atiende al cliente que registra su mascota desde
 * el portal y al personal que la registra en el mostrador. La
 * diferencia es solo de quien es el dueno resultante, y esa decision
 * se concentra en resolverDueno().
 */
@Service
public class MascotaService {

    private final MascotaRepository mascotaRepo;
    private final DuenoRepository duenoRepo;

    public MascotaService(MascotaRepository mascotaRepo, DuenoRepository duenoRepo) {
        this.mascotaRepo = mascotaRepo;
        this.duenoRepo = duenoRepo;
    }

    public List<MascotaResponse> mias(Usuario solicitante) {
        return mascotaRepo.findByDuenoIdAndActivoTrue(solicitante.getDueno().getId())
                .stream().map(MascotaResponse::desde).toList();
    }

    /**
     * RF-03: busqueda de pacientes para el personal clinico, por nombre
     * del paciente o de su dueno.
     */
    public List<MascotaResponse> buscar(String texto) {
        return mascotaRepo.buscar(texto == null ? "" : texto.trim())
                .stream().map(MascotaResponse::desde).toList();
    }

    /** RF-03: pacientes de un cliente, consultados por el personal. */
    public List<MascotaResponse> deDueno(Long duenoId) {
        if (!duenoRepo.existsById(duenoId)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado.");
        }
        return mascotaRepo.findByDuenoIdAndActivoTrue(duenoId)
                .stream().map(MascotaResponse::desde).toList();
    }

    /**
     * RF-02: alta de paciente. Si quien registra es un cliente, la
     * mascota queda a su nombre; si es personal, debe indicar a que
     * cliente pertenece (RN-03: siempre hay un dueno).
     */
    @Transactional
    public MascotaResponse crear(MascotaRequest datos, Long duenoId, Usuario solicitante) {
        Mascota mascota = new Mascota();
        mascota.setDueno(resolverDueno(duenoId, solicitante)); // RN-03
        aplicar(mascota, datos);
        return MascotaResponse.desde(mascotaRepo.save(mascota));
    }

    /** RF-03: correccion de la ficha del paciente. */
    @Transactional
    public MascotaResponse actualizar(Long id, MascotaRequest datos, Usuario solicitante) {
        Mascota mascota = obtenerVisible(id, solicitante);
        aplicar(mascota, datos);
        return MascotaResponse.desde(mascota);
    }

    /**
     * RF-03: baja LOGICA del paciente. No se elimina: sus citas y su
     * historia clinica deben seguir existiendo.
     */
    @Transactional
    public MascotaResponse cambiarEstado(Long id, boolean activo, Usuario solicitante) {
        Mascota mascota = obtenerVisible(id, solicitante);
        mascota.setActivo(activo);
        return MascotaResponse.desde(mascota);
    }

    /**
     * Un cliente solo ve y toca sus propias mascotas. Se responde
     * "no encontrada" y no "prohibido" para no revelar que existe.
     */
    private Mascota obtenerVisible(Long id, Usuario solicitante) {
        Mascota mascota = mascotaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada."));
        if (solicitante.getRol() == Usuario.Rol.CLIENTE
                && !mascota.getDueno().getId().equals(solicitante.getDueno().getId())) {
            throw new RecursoNoEncontradoException("Mascota no encontrada.");
        }
        return mascota;
    }

    private Dueno resolverDueno(Long duenoId, Usuario solicitante) {
        if (solicitante.getRol() == Usuario.Rol.CLIENTE) {
            return solicitante.getDueno();
        }
        if (duenoId == null) {
            throw new RecursoNoEncontradoException(
                "Debe indicarse a que cliente pertenece la mascota.");
        }
        return duenoRepo.findById(duenoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado."));
    }

    private void aplicar(Mascota mascota, MascotaRequest datos) {
        mascota.setNombre(datos.nombre().trim());
        mascota.setEspecie(datos.especie().trim().toUpperCase());
        mascota.setRaza(datos.raza());
        mascota.setFechaNacimiento(datos.fechaNacimiento());
        mascota.setSexo(datos.sexo());
        mascota.setPesoKg(datos.pesoKg());
        mascota.setTamano(datos.tamano());
    }
}
