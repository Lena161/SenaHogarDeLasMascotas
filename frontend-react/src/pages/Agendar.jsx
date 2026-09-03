// Agendar — CONMUTADO al modelo definitivo:
// - La mascota se ELIGE entre las registradas (antes: texto libre).
// - El servicio es un objeto con tipo, duración y precio (antes: nombre).
// - Se elige PROFESIONAL, filtrado por tipo de servicio (RN-04:
//   consultas → veterinarios, spa → esteticistas).
// - "Mis citas" muestra el ESTADO y el precio calculado por el
//   polimorfismo del backend; cancelar es una transición lógica
//   (RN-06), no un borrado.
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Hero from '../components/Hero';
import Panel from '../components/Panel';
import FormMessage from '../components/FormMessage';
import { api } from '../services/api';

const HOY = new Date().toISOString().split('T')[0];

const COLORES_ESTADO = {
  PENDIENTE: 'estado-pendiente',
  CONFIRMADA: 'estado-confirmada',
  ATENDIDA: 'estado-atendida',
  CANCELADA: 'estado-cancelada',
  NO_ASISTIO: 'estado-noasistio'
};

function formatearFechaHora(iso) {
  const [fecha, hora] = iso.split('T');
  const [y, m, d] = fecha.split('-');
  return `${d}/${m}/${y} · ${hora.substring(0, 5)}`;
}

function formatearPrecio(valor) {
  if (valor == null) return '';
  return new Intl.NumberFormat('es-CO', {
    style: 'currency', currency: 'COP', maximumFractionDigits: 0
  }).format(valor);
}

export default function Agendar() {
  const [mascotas, setMascotas] = useState([]);
  const [servicios, setServicios] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [citas, setCitas] = useState(null);
  const [mensaje, setMensaje] = useState(null);
  const [formulario, setFormulario] = useState({
    mascotaId: '', servicioId: '', empleadoId: '',
    fecha: '', hora: '', observaciones: ''
  });

  useEffect(() => {
    api.misMascotas().then(setMascotas).catch(() => setMascotas([]));
    api.servicios().then(setServicios).catch(() => setServicios([]));
    api.empleados().then(setEmpleados).catch(() => setEmpleados([]));
    cargarCitas();
  }, []);

  async function cargarCitas() {
    try {
      setCitas(await api.misCitas());
    } catch {
      setCitas([]);
    }
  }

  function manejarCambio(e) {
    const { name, value } = e.target;
    // Al cambiar de servicio se reinicia el profesional elegido,
    // porque la lista de profesionales aptos depende del tipo (RN-04)
    if (name === 'servicioId') {
      setFormulario({ ...formulario, servicioId: value, empleadoId: '' });
    } else {
      setFormulario({ ...formulario, [name]: value });
    }
  }

  // RN-04 en la interfaz: solo se ofrecen los profesionales del
  // perfil correcto (el backend lo valida de nuevo, por supuesto)
  const servicioElegido = servicios.find((s) => s.id === Number(formulario.servicioId));
  const rolRequerido = servicioElegido?.tipo === 'SPA' ? 'ESTETICISTA' : 'VETERINARIO';
  const profesionalesAptos = servicioElegido
    ? empleados.filter((e) => e.rol === rolRequerido)
    : [];

  async function manejarEnvio(e) {
    e.preventDefault();
    try {
      const cita = await api.crearCita({
        mascotaId: Number(formulario.mascotaId),
        servicioId: Number(formulario.servicioId),
        empleadoId: Number(formulario.empleadoId),
        // LocalDateTime del backend: "2026-08-20T09:00:00"
        fechaHora: `${formulario.fecha}T${formulario.hora}:00`,
        observaciones: formulario.observaciones || null
      });
      setMensaje({
        tipo: 'success',
        texto: `¡Cita agendada! Valor del servicio: ${formatearPrecio(cita.precioFinal)}.`
      });
      setFormulario({ mascotaId: '', servicioId: '', empleadoId: '', fecha: '', hora: '', observaciones: '' });
      cargarCitas();
    } catch (error) {
      // Aquí llegan los mensajes de las reglas: RN-01, RN-02, RN-04
      setMensaje({ tipo: 'error', texto: error.message });
    }
  }

  async function manejarCancelar(id) {
    try {
      await api.cancelarCita(id); // PATCH: transición a CANCELADA
      cargarCitas();
    } catch (error) {
      // P. ej. RF-20: antelación insuficiente
      setMensaje({ tipo: 'error', texto: error.message });
    }
  }

  const sinMascotas = mascotas.length === 0;

  return (
    <main>
      <Hero compacto titulo="Agenda la cita de tu mascota"
            subtitulo="Elige tu mascota, el servicio, el profesional y el horario." />

      <div className="agendar-grid">
        <Panel titulo="Nueva cita">
          {sinMascotas ? (
            <p className="citas-empty">
              Primero registra una mascota en{' '}
              <Link to="/mascotas">Mis Mascotas</Link> para poder agendar.
            </p>
          ) : (
            <form className="form-app" onSubmit={manejarEnvio}>
              <FormMessage mensaje={mensaje} />
              <label>Mascota
                <select name="mascotaId" required value={formulario.mascotaId} onChange={manejarCambio}>
                  <option value="">Selecciona...</option>
                  {mascotas.map((m) => (
                    <option key={m.id} value={m.id}>{m.nombre} ({m.especie})</option>
                  ))}
                </select>
              </label>
              <label>Servicio
                <select name="servicioId" required value={formulario.servicioId} onChange={manejarCambio}>
                  <option value="">Selecciona...</option>
                  {servicios.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.nombre} · {s.duracionMinutos} min · desde {formatearPrecio(s.precioBase)}
                    </option>
                  ))}
                </select>
              </label>
              {servicioElegido && (
                <label>Profesional
                  <select name="empleadoId" required value={formulario.empleadoId} onChange={manejarCambio}>
                    <option value="">Selecciona...</option>
                    {profesionalesAptos.map((p) => (
                      <option key={p.id} value={p.id}>{p.nombre}</option>
                    ))}
                  </select>
                </label>
              )}
              <div className="form-row">
                <label>Fecha
                  <input type="date" name="fecha" required min={HOY}
                         value={formulario.fecha} onChange={manejarCambio} />
                </label>
                <label>Hora
                  <input type="time" name="hora" required min="08:00" max="18:00"
                         value={formulario.hora} onChange={manejarCambio} />
                </label>
              </div>
              <label>Notas (opcional)
                <textarea name="observaciones"
                          placeholder="Cuéntanos algo más sobre el motivo de la visita"
                          value={formulario.observaciones} onChange={manejarCambio} />
              </label>
              <button type="submit" className="form-submit">Agendar Cita</button>
            </form>
          )}
        </Panel>

        <Panel titulo="Mis citas">
          <div className="citas-list">
            {citas === null && <p className="citas-empty">Cargando...</p>}
            {citas !== null && citas.length === 0 && (
              <p className="citas-empty">Aún no tienes citas agendadas.</p>
            )}
            {citas !== null && citas.map((c) => (
              <div className="cita-item" key={c.id}>
                <div className="info">
                  <strong>{c.mascota} — {c.servicio}</strong>
                  <span>
                    {formatearFechaHora(c.fechaHora)} · {c.profesional}
                    {c.precioFinal != null && ` · ${formatearPrecio(c.precioFinal)}`}
                  </span>
                  <span className={`estado-badge ${COLORES_ESTADO[c.estado] || ''}`}>
                    {c.estado.replace('_', ' ')}
                  </span>
                </div>
                {(c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA') && (
                  <button type="button" className="cita-cancel"
                          onClick={() => manejarCancelar(c.id)}>
                    Cancelar
                  </button>
                )}
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </main>
  );
}
