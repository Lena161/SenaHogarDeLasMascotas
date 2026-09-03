package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.CitaDtos.*;
import com.hogarmascotas.api.entity.*;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Nucleo de la logica de citas. Aqui viven las reglas que la Fase 2
 * dejo pendientes para la capa de servicio:
 *   RN-01 horario de atencion  | RN-02 solapamiento por duracion (profesional)
 *   RN-04 rol vs tipo servicio | RN-06 transiciones (delegada a Cita)
 *   RN-08 solapamiento por duracion (mascota, sin importar el profesional)
 */
@Service
public class CitaService {

    private static final List<Cita.Estado> ESTADOS_OCUPAN_AGENDA =
            List.of(Cita.Estado.PENDIENTE, Cita.Estado.CONFIRMADA);

    private final CitaRepository citaRepo;
    private final MascotaRepository mascotaRepo;
    private final ServicioRepository servicioRepo;
    private final EmpleadoRepository empleadoRepo;
    private final LocalTime horaApertura;
    private final LocalTime horaCierre;
    private final int antelacionCancelacionHoras;

    public CitaService(CitaRepository citaRepo, MascotaRepository mascotaRepo,
                       ServicioRepository servicioRepo, EmpleadoRepository empleadoRepo,
                       @Value("${app.negocio.hora-apertura}") String apertura,
                       @Value("${app.negocio.hora-cierre}") String cierre,
                       @Value("${app.negocio.antelacion-cancelacion-horas}") int antelacion) {
        this.citaRepo = citaRepo;
        this.mascotaRepo = mascotaRepo;
        this.servicioRepo = servicioRepo;
        this.empleadoRepo = empleadoRepo;
        this.horaApertura = LocalTime.parse(apertura);
        this.horaCierre = LocalTime.parse(cierre);
        this.antelacionCancelacionHoras = antelacion;
    }

    @Transactional
    public CitaResponse crear(CrearCitaRequest datos, Usuario solicitante) {
        Mascota mascota = mascotaRepo.findById(datos.mascotaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada."));

        // Un CLIENTE solo agenda para SUS mascotas (propiedad conservada del prototipo)
        if (solicitante.getRol() == Usuario.Rol.CLIENTE
                && !mascota.getDueno().getId().equals(solicitante.getDueno().getId())) {
            throw new RecursoNoEncontradoException("Mascota no encontrada.");
        }

        Servicio servicio = servicioRepo.findById(datos.servicioId())
                .filter(Servicio::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no disponible."));

        Empleado empleado = empleadoRepo.findById(datos.empleadoId())
                .filter(Empleado::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no disponible."));

        validarReglas(datos.fechaHora(), servicio, empleado, mascota, null);

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        cita.setEmpleado(empleado);
        cita.setFechaHora(datos.fechaHora());
        cita.setObservaciones(datos.observaciones());
        // POLIMORFISMO en accion: no sabemos (ni nos importa) si es
        // consulta o spa; el objeto servicio calcula su propio precio.
        cita.setPrecioFinal(servicio.calcularPrecio(mascota.getTamano()));

        return CitaResponse.desde(citaRepo.save(cita));
    }

    /**
     * Validaciones de agenda. Se extraen a un metodo para reutilizarlas
     * al reprogramar y para probarlas unitariamente.
     */
    void validarReglas(LocalDateTime fechaHora, Servicio servicio, Empleado empleado,
                        Mascota mascota, Long citaExcluida) {
        // RN-01: dentro del horario de atencion del negocio
        LocalTime hora = fechaHora.toLocalTime();
        LocalTime fin = hora.plusMinutes(servicio.getDuracionMinutos());
        if (hora.isBefore(horaApertura) || fin.isAfter(horaCierre)) {
            throw new ReglaNegocioException(
                "RN-01: la cita debe estar entre " + horaApertura + " y " + horaCierre + ".");
        }

        // RN-01 (jornada del profesional) - metodo heredado de Empleado
        if (!empleado.estaDisponible(fechaHora, servicio.getDuracionMinutos())) {
            throw new ReglaNegocioException(
                "RN-01: el profesional no atiende en ese horario.");
        }

        // RN-04: compatibilidad rol-servicio, resuelta con POLIMORFISMO
        if (!servicio.puedeSerAtendidoPor(empleado)) {
            throw new ReglaNegocioException(
                "RN-04: el servicio '" + servicio.getNombre()
                + "' no puede ser atendido por un " + empleado.getRol() + ".");
        }

        // RN-02: solapamiento por duracion. Se consultan las citas activas
        // del profesional en el dia y se verifica cruce de intervalos:
        // hay conflicto si (inicioA < finB) y (inicioB < finA).
        LocalDateTime inicioDia = fechaHora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        LocalDateTime inicioNueva = fechaHora;
        LocalDateTime finNueva = fechaHora.plusMinutes(servicio.getDuracionMinutos());

        List<Cita> delDia = citaRepo.findByEmpleadoIdAndEstadoInAndFechaHoraBetween(
                empleado.getId(), ESTADOS_OCUPAN_AGENDA, inicioDia, finDia);

        for (Cita existente : delDia) {
            if (citaExcluida != null && existente.getId().equals(citaExcluida)) continue;
            LocalDateTime inicioExist = existente.getFechaHora();
            LocalDateTime finExist = inicioExist.plusMinutes(
                    existente.getServicio().getDuracionMinutos());
            if (inicioExist.isBefore(finNueva) && inicioNueva.isBefore(finExist)) {
                throw new ReglaNegocioException(
                    "RN-02: el profesional ya tiene una cita de "
                    + inicioExist.toLocalTime() + " a " + finExist.toLocalTime() + ".");
            }
        }

        // RN-08: solapamiento por duracion, pero desde el punto de vista de
        // la mascota. Aunque cambie el profesional, una misma mascota no
        // puede estar en dos citas a la vez. Mismo criterio de cruce que
        // RN-02: hay conflicto si (inicioA < finB) y (inicioB < finA).
        List<Cita> delDiaMascota = citaRepo.findByMascotaIdAndEstadoInAndFechaHoraBetween(
                mascota.getId(), ESTADOS_OCUPAN_AGENDA, inicioDia, finDia);

        for (Cita existente : delDiaMascota) {
            if (citaExcluida != null && existente.getId().equals(citaExcluida)) continue;
            LocalDateTime inicioExist = existente.getFechaHora();
            LocalDateTime finExist = inicioExist.plusMinutes(
                    existente.getServicio().getDuracionMinutos());
            if (inicioExist.isBefore(finNueva) && inicioNueva.isBefore(finExist)) {
                throw new ReglaNegocioException(
                    "RN-08: la mascota ya tiene una cita de "
                    + inicioExist.toLocalTime() + " a " + finExist.toLocalTime() + ".");
            }
        }
    }

    public List<CitaResponse> misCitas(Usuario solicitante) {
        return citaRepo.findByMascotaDuenoIdOrderByFechaHoraAsc(solicitante.getDueno().getId())
                .stream().map(CitaResponse::desde).toList();
    }

    @Transactional
    public CitaResponse cancelar(Long citaId, Usuario solicitante) {
        Cita cita = obtenerVisible(citaId, solicitante);

        // RF-20: el cliente debe cancelar con la antelacion minima
        if (solicitante.getRol() == Usuario.Rol.CLIENTE) {
            LocalDateTime limite = cita.getFechaHora().minusHours(antelacionCancelacionHoras);
            if (LocalDateTime.now().isAfter(limite)) {
                throw new ReglaNegocioException(
                    "RF-20: las citas se cancelan con al menos "
                    + antelacionCancelacionHoras + " horas de antelacion. Comunicate por telefono.");
            }
        }
        cita.cancelar(); // RN-06 la protege la propia entidad
        return CitaResponse.desde(cita);
    }

    @Transactional
    public CitaResponse confirmar(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
        cita.confirmar();
        return CitaResponse.desde(cita);
    }

    @Transactional
    public CitaResponse atender(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
        cita.atender();
        return CitaResponse.desde(cita);
    }

    private Cita obtenerVisible(Long citaId, Usuario solicitante) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
        if (solicitante.getRol() == Usuario.Rol.CLIENTE
                && !cita.getMascota().getDueno().getId().equals(solicitante.getDueno().getId())) {
            throw new RecursoNoEncontradoException("Cita no encontrada.");
        }
        return cita;
    }
}
