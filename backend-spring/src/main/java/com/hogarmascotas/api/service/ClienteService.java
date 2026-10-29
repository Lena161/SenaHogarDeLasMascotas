package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.DuenoDtos.*;
import com.hogarmascotas.api.dto.MascotaDtos.MascotaResponse;
import com.hogarmascotas.api.entity.Dueno;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.DuenoRepository;
import com.hogarmascotas.api.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de clientes por el personal (RF-03).
 *
 * Complementa el registro publico de AuthService: aqui la
 * administracion consulta, corrige y da de baja fichas de clientes
 * que ya existen, incluidos los que nunca crearon cuenta en linea.
 */
@Service
public class ClienteService {

    private final DuenoRepository duenoRepo;
    private final MascotaRepository mascotaRepo;

    public ClienteService(DuenoRepository duenoRepo, MascotaRepository mascotaRepo) {
        this.duenoRepo = duenoRepo;
        this.mascotaRepo = mascotaRepo;
    }

    /** RF-03: busqueda unica por nombre, documento, telefono o correo. */
    public List<DuenoResponse> buscar(String texto, boolean incluirInactivos) {
        return duenoRepo.buscar(texto == null ? "" : texto.trim(), incluirInactivos)
                .stream().map(DuenoResponse::desde).toList();
    }

    /** RF-03: ficha del cliente con sus pacientes activos. */
    public DuenoDetalleResponse detalle(Long id) {
        Dueno dueno = obtener(id);
        List<MascotaResponse> mascotas = mascotaRepo.findByDuenoIdAndActivoTrue(id)
                .stream().map(MascotaResponse::desde).toList();
        return new DuenoDetalleResponse(DuenoResponse.desde(dueno), mascotas);
    }

    /**
     * RF-03: alta de cliente hecha en el mostrador. No crea usuario ni
     * contrasena: la ficha existe para poder agendar y facturar, y el
     * cliente puede crear despues su cuenta con ese mismo correo.
     */
    @Transactional
    public DuenoResponse crear(DuenoAltaRequest datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (duenoRepo.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe un cliente con ese correo.");
        }
        if (duenoRepo.existsByNumeroDocumento(datos.numeroDocumento().trim())) {
            throw new ReglaNegocioException("Ya existe un cliente con ese documento.");
        }
        Dueno dueno = new Dueno();
        dueno.setCorreo(correo);
        aplicar(dueno, datos.tipoDocumento(), datos.numeroDocumento(), datos.nombres(),
                datos.apellidos(), datos.telefono(), datos.direccion());
        return DuenoResponse.desde(duenoRepo.save(dueno));
    }

    @Transactional
    public DuenoResponse actualizar(Long id, DuenoRequest datos) {
        Dueno dueno = obtener(id);
        String documento = datos.numeroDocumento().trim();
        if (!documento.equals(dueno.getNumeroDocumento())
                && duenoRepo.existsByNumeroDocumento(documento)) {
            throw new ReglaNegocioException("Ya existe otro cliente con ese documento.");
        }
        aplicar(dueno, datos.tipoDocumento(), documento, datos.nombres(),
                datos.apellidos(), datos.telefono(), datos.direccion());
        return DuenoResponse.desde(dueno);
    }

    /**
     * RF-03: baja LOGICA. El cliente deja de aparecer en los listados,
     * pero sus citas y el historial de sus mascotas se conservan: esa
     * informacion clinica no deja de ser cierta porque el cliente se
     * retire.
     */
    @Transactional
    public DuenoResponse cambiarEstado(Long id, boolean activo) {
        Dueno dueno = obtener(id);
        dueno.setActivo(activo);
        // Las mascotas siguen al dueno: no tiene sentido un paciente
        // activo sin responsable a quien contactar.
        mascotaRepo.findByDuenoIdAndActivoTrue(id)
                .forEach(m -> { if (!activo) m.setActivo(false); });
        return DuenoResponse.desde(dueno);
    }

    private Dueno obtener(Long id) {
        return duenoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado."));
    }

    private void aplicar(Dueno dueno, String tipoDocumento, String numeroDocumento,
                         String nombres, String apellidos, String telefono, String direccion) {
        dueno.setTipoDocumento(tipoDocumento.trim().toUpperCase());
        dueno.setNumeroDocumento(numeroDocumento.trim());
        dueno.setNombres(nombres.trim());
        dueno.setApellidos(apellidos.trim());
        dueno.setTelefono(telefono.trim());
        dueno.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());
    }
}
