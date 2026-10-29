package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.HistorialDtos.*;
import com.hogarmascotas.api.entity.*;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.CitaRepository;
import com.hogarmascotas.api.repository.HistorialClinicoRepository;
import com.hogarmascotas.api.repository.MascotaRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Atencion de citas e historia clinica (RF-09, RF-10).
 *
 * Reune en una sola transaccion los dos efectos de atender a un
 * paciente: la cita cambia de estado y, si el servicio lo amerita,
 * queda el registro clinico. Que lo amerite o no lo decide el propio
 * servicio por POLIMORFISMO, no un condicional por tipo.
 */
@Service
public class HistorialService {

    private final HistorialClinicoRepository historialRepo;
    private final CitaRepository citaRepo;
    private final MascotaRepository mascotaRepo;

    public HistorialService(HistorialClinicoRepository historialRepo,
                            CitaRepository citaRepo,
                            MascotaRepository mascotaRepo) {
        this.historialRepo = historialRepo;
        this.citaRepo = citaRepo;
        this.mascotaRepo = mascotaRepo;
    }

    /**
     * RF-09: registra la atencion de una cita.
     *
     * @return el registro clinico creado, o null si el servicio no
     *         genera historia (un bano, por ejemplo): en ese caso la
     *         cita igualmente queda ATENDIDA.
     */
    @Transactional
    public HistorialResponse atender(Long citaId, AtencionRequest datos, Usuario profesional) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));

        Empleado empleado = exigirEmpleado(profesional);

        // Un profesional solo cierra SUS citas. El administrador puede
        // hacerlo en nombre de cualquiera (p. ej. si el profesional se
        // ausento), y queda registrado como responsable.
        if (profesional.getRol() != Usuario.Rol.ADMIN
                && !cita.getEmpleado().getId().equals(empleado.getId())) {
            throw new ReglaNegocioException(
                "La cita esta asignada a " + cita.getEmpleado().getNombreCompleto()
                + "; solo ese profesional o el administrador pueden registrarla.");
        }

        if (historialRepo.findByCitaId(citaId).isPresent()) {
            throw new ReglaNegocioException("Esta cita ya tiene su atencion registrada.");
        }

        // RN-06: la transicion la valida la propia entidad.
        cita.atender();

        // POLIMORFISMO: el servicio decide si hay historia clinica.
        if (!cita.getServicio().requiereHistorialClinico()) {
            return null;
        }

        HistorialClinico historial = HistorialClinico.registrar(
                cita.getMascota(), cita, empleado, datos.motivo(), datos.diagnostico());
        historial.setAnamnesis(datos.anamnesis());
        historial.setTemperaturaC(datos.temperaturaC());
        historial.setFrecuenciaCardiaca(datos.frecuenciaCardiaca());
        historial.setFrecuenciaRespiratoria(datos.frecuenciaRespiratoria());
        historial.setCondicionCorporal(datos.condicionCorporal());
        historial.setExamenFisico(datos.examenFisico());
        historial.setTratamiento(datos.tratamiento());
        historial.setMedicamentos(datos.medicamentos());
        historial.setVacunasAplicadas(datos.vacunasAplicadas());
        historial.setDesparasitacion(datos.desparasitacion());
        historial.setProximoControl(datos.proximoControl());
        historial.setObservaciones(datos.observaciones());
        historial.setPesoKg(datos.pesoKg());

        // El peso tomado en consulta actualiza la ficha del paciente
        if (datos.pesoKg() != null && datos.pesoKg() > 0) {
            cita.getMascota().setPesoKg(datos.pesoKg());
        }

        return HistorialResponse.desde(historialRepo.save(historial));
    }

    /**
     * RF-09 (atencion sin cita previa): registra una entrada clinica
     * directa, para una urgencia que no paso por la agenda.
     */
    @Transactional
    public HistorialResponse registrarDirecto(Long mascotaId, HistorialDirectoRequest datos,
                                              Usuario profesional) {
        Mascota mascota = mascotaRepo.findById(mascotaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada."));
        Empleado empleado = exigirEmpleado(profesional);

        HistorialClinico historial = HistorialClinico.registrar(
                mascota, null, empleado, datos.motivo(), datos.diagnostico());
        historial.setAnamnesis(datos.anamnesis());
        historial.setTemperaturaC(datos.temperaturaC());
        historial.setFrecuenciaCardiaca(datos.frecuenciaCardiaca());
        historial.setFrecuenciaRespiratoria(datos.frecuenciaRespiratoria());
        historial.setCondicionCorporal(datos.condicionCorporal());
        historial.setExamenFisico(datos.examenFisico());
        historial.setTratamiento(datos.tratamiento());
        historial.setMedicamentos(datos.medicamentos());
        historial.setVacunasAplicadas(datos.vacunasAplicadas());
        historial.setDesparasitacion(datos.desparasitacion());
        historial.setProximoControl(datos.proximoControl());
        historial.setObservaciones(datos.observaciones());
        historial.setPesoKg(datos.pesoKg());
        if (datos.pesoKg() != null && datos.pesoKg() > 0) {
            mascota.setPesoKg(datos.pesoKg());
        }
        return HistorialResponse.desde(historialRepo.save(historial));
    }

    /**
     * RF-10: historial completo de una mascota, del mas reciente al
     * mas antiguo. El cliente solo puede ver el de sus propias
     * mascotas; el personal clinico, el de cualquiera.
     */
    public List<HistorialResponse> deMascota(Long mascotaId, Usuario solicitante) {
        Mascota mascota = mascotaRepo.findById(mascotaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada."));

        if (solicitante.getRol() == Usuario.Rol.CLIENTE
                && !mascota.getDueno().getId().equals(solicitante.getDueno().getId())) {
            // Mismo criterio que en citas: no se revela que existe
            throw new RecursoNoEncontradoException("Mascota no encontrada.");
        }
        return historialRepo.findByMascotaIdOrderByFechaDesc(mascotaId)
                .stream().map(HistorialResponse::desde).toList();
    }

    /**
     * Devuelve el empleado de la cuenta, ya desenvuelto.
     *
     * Usuario.empleado es una relacion perezosa, asi que Hibernate entrega
     * un proxy que hereda de Empleado pero no del subtipo real. Eso basta
     * para llamar a sus metodos, pero rompe cualquier comprobacion con
     * instanceof: una veterinaria autentica no pasaba la validacion de
     * RN-07. Al desenvolverlo aqui, el resto del dominio recibe siempre la
     * instancia verdadera y el polimorfismo funciona como corresponde.
     */
    private Empleado exigirEmpleado(Usuario usuario) {
        if (usuario.getEmpleado() == null) {
            throw new ReglaNegocioException(
                "La cuenta no corresponde a un empleado de la clinica.");
        }
        return (Empleado) Hibernate.unproxy(usuario.getEmpleado());
    }
}
