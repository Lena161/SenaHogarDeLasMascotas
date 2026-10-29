package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.CitaDtos.*;
import com.hogarmascotas.api.dto.HistorialDtos.AtencionRequest;
import com.hogarmascotas.api.dto.HistorialDtos.HistorialResponse;
import com.hogarmascotas.api.entity.Cita;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.service.AuthService;
import com.hogarmascotas.api.service.CitaService;
import com.hogarmascotas.api.service.HistorialService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * PATRON MVC: el controlador solo traduce HTTP <-> servicio.
 * Ninguna regla de negocio vive aqui (restriccion de diseno, Fase 1 §6.3).
 */
@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;
    private final HistorialService historialService;
    private final AuthService authService;

    public CitaController(CitaService citaService, HistorialService historialService,
                          AuthService authService) {
        this.citaService = citaService;
        this.historialService = historialService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CrearCitaRequest datos,
                                              Authentication auth) {
        Usuario solicitante = authService.obtenerPorUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citaService.crear(datos, solicitante));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<CitaResponse> mias(Authentication auth) {
        return citaService.misCitas(authService.obtenerPorUsername(auth.getName()));
    }

    /**
     * RF-08: agenda del dia para la administracion o la recepcion.
     * Sin parametro de fecha, el dia de hoy.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public List<CitaResponse> agendaDelDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Cita.Estado estado) {
        return citaService.agendaDelDia(fecha == null ? LocalDate.now() : fecha, estado);
    }

    /**
     * RF-08: agenda propia del profesional. El rango por defecto es el
     * dia de hoy; se amplia con los parametros desde y hasta.
     */
    @GetMapping("/agenda")
    @PreAuthorize("hasAnyRole('VETERINARIO','ESTETICISTA','ADMIN')")
    public List<CitaResponse> miAgenda(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        LocalDate inicio = desde == null ? LocalDate.now() : desde;
        LocalDate fin = hasta == null ? inicio : hasta;
        return citaService.miAgenda(inicio, fin, authService.obtenerPorUsername(auth.getName()));
    }

    /** Evolucion del DELETE del prototipo: cancelacion LOGICA (RN-06). */
    @PatchMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id, Authentication auth) {
        return citaService.cancelar(id, authService.obtenerPorUsername(auth.getName()));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public CitaResponse confirmar(@PathVariable Long id) {
        return citaService.confirmar(id);
    }

    /** RF-07: el paciente no se presento a una cita confirmada. */
    @PatchMapping("/{id}/inasistencia")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public CitaResponse inasistencia(@PathVariable Long id) {
        return citaService.marcarInasistencia(id);
    }

    /**
     * RF-07 + RF-09: registra la atencion. Si el servicio genera historia
     * clinica, el cuerpo debe traer motivo y diagnostico; si no (un bano),
     * basta con la llamada y la cita queda ATENDIDA.
     *
     * Responde la cita actualizada y, cuando corresponde, el registro
     * clinico creado.
     */
    @PostMapping("/{id}/atencion")
    @PreAuthorize("hasAnyRole('VETERINARIO','ESTETICISTA','ADMIN')")
    public Map<String, Object> atender(@PathVariable Long id,
                                       @RequestBody(required = false) AtencionRequest datos,
                                       Authentication auth) {
        Usuario profesional = authService.obtenerPorUsername(auth.getName());
        AtencionRequest cuerpo = datos == null ? AtencionRequest.vacia() : datos;
        HistorialResponse historial = historialService.atender(id, cuerpo, profesional);
        return historial == null
                ? Map.of("atendida", true)
                : Map.of("atendida", true, "historial", historial);
    }
}
