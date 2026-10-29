// ============================================================
// Agenda del día (RF-07, RF-08).
//
// Una sola pantalla sirve a dos lecturas distintas de la misma
// información: la administración ve toda la clínica y gestiona
// los estados; el profesional ve solo sus propias citas y
// registra la atención. Qué consulta se hace lo decide el rol.
// ============================================================
import { useCallback, useEffect, useState } from 'react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import FormHistoria from './FormHistoria';

const COLORES_ESTADO = {
  PENDIENTE: 'estado-pendiente',
  CONFIRMADA: 'estado-confirmada',
  ATENDIDA: 'estado-atendida',
  CANCELADA: 'estado-cancelada',
  NO_ASISTIO: 'estado-noasistio'
};

const GESTIONAN_AGENDA = ['ADMIN', 'RECEPCION'];
const ATIENDEN = ['ADMIN', 'VETERINARIO', 'ESTETICISTA'];

/** Fecha de hoy en formato YYYY-MM-DD, en hora local y no UTC. */
function hoy() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function hora(fechaHora) {
  return new Date(fechaHora).toLocaleTimeString('es-CO',
    { hour: '2-digit', minute: '2-digit' });
}

export default function Agenda() {
  const { usuario } = useAuth();
  const gestiona = GESTIONAN_AGENDA.includes(usuario.rol);
  const atiende = ATIENDEN.includes(usuario.rol);

  const [fecha, setFecha] = useState(hoy());
  const [citas, setCitas] = useState(null);
  const [mensaje, setMensaje] = useState(null);
  const [atendiendo, setAtendiendo] = useState(null); // cita en curso

  const cargar = useCallback(async () => {
    setMensaje(null);
    try {
      // El administrador necesita ver la clínica completa; el
      // profesional, solo lo suyo. Son dos endpoints distintos
      // porque también son dos permisos distintos.
      const datos = gestiona
        ? await api.agendaDelDia(fecha)
        : await api.miAgenda(fecha, fecha);
      setCitas(datos);
    } catch (e) {
      setCitas([]);
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }, [fecha, gestiona]);

  useEffect(() => { cargar(); }, [cargar]);

  async function accion(fn, exito) {
    setMensaje(null);
    try {
      await fn();
      setMensaje({ tipo: 'ok', texto: exito });
      await cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function registrarAtencion(datos) {
    await api.registrarAtencion(atendiendo.id, datos);
    setAtendiendo(null);
    setMensaje({ tipo: 'ok', texto: 'Atención registrada.' });
    await cargar();
  }

  return (
    <section>
      <div className="panel-barra">
        <label className="campo-inline">
          Fecha
          <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
        </label>
        <button type="button" className="btn-plano" onClick={() => setFecha(hoy())}>
          Hoy
        </button>
        <span className="panel-conteo">
          {citas === null ? 'Cargando…' : `${citas.length} cita(s)`}
        </span>
      </div>

      {mensaje && (
        <p className={mensaje.tipo === 'error' ? 'aviso-error' : 'aviso-ok'}>
          {mensaje.texto}
        </p>
      )}

      {citas !== null && citas.length === 0 && (
        <p className="panel-vacio">No hay citas agendadas para esta fecha.</p>
      )}

      {citas !== null && citas.length > 0 && (
        <div className="tabla-envoltura">
          <table className="tabla-panel">
            <thead>
              <tr>
                <th>Hora</th>
                <th>Paciente</th>
                <th>Servicio</th>
                {gestiona && <th>Profesional</th>}
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {citas.map((c) => (
                <tr key={c.id}>
                  <td className="celda-hora">
                    {hora(c.fechaHora)}
                    <small>{c.duracionMinutos} min</small>
                  </td>
                  <td>
                    <strong>{c.mascota}</strong>
                    <small>{c.dueno} · {c.telefonoDueno}</small>
                  </td>
                  <td>{c.servicio}</td>
                  {gestiona && <td>{c.profesional}</td>}
                  <td>
                    <span className={`estado-badge ${COLORES_ESTADO[c.estado] || ''}`}>
                      {c.estado.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="celda-acciones">
                    {gestiona && c.estado === 'PENDIENTE' && (
                      <button type="button" className="btn-mini"
                              onClick={() => accion(() => api.confirmarCita(c.id),
                                                    'Cita confirmada.')}>
                        Confirmar
                      </button>
                    )}
                    {atiende && c.estado === 'CONFIRMADA' && (
                      <button type="button" className="btn-mini principal"
                              onClick={() => setAtendiendo(c)}>
                        {c.requiereHistorial ? 'Registrar atención' : 'Marcar atendida'}
                      </button>
                    )}
                    {gestiona && c.estado === 'CONFIRMADA' && (
                      <button type="button" className="btn-mini"
                              onClick={() => accion(() => api.marcarInasistencia(c.id),
                                                    'Registrada como no asistida.')}>
                        No asistió
                      </button>
                    )}
                    {gestiona && (c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA') && (
                      <button type="button" className="btn-mini peligro"
                              onClick={() => accion(() => api.cancelarCita(c.id),
                                                    'Cita cancelada.')}>
                        Cancelar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {atendiendo && (
        <FormHistoria cita={atendiendo}
                      onGuardar={registrarAtencion}
                      onCerrar={() => setAtendiendo(null)} />
      )}
    </section>
  );
}
