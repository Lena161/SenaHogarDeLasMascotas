// ============================================================
// Catálogo de servicios (RF-04).
//
// Dos campos merecen atención al sustentar:
//
// La DURACIÓN no es informativa: es el insumo con el que el
// backend calcula si dos citas se cruzan. Subestimarla libera la
// agenda antes de tiempo (RN-09, hallazgo de las pruebas).
//
// El TIPO decide, por polimorfismo, cómo se calcula el precio y
// qué perfil puede atender el servicio. Por eso solo se fija al
// crear: cambiarlo después alteraría citas ya atendidas.
// ============================================================
import { useEffect, useState } from 'react';
import { api } from '../../services/api';

const VACIO = {
  nombre: '', tipo: 'CONSULTA', descripcion: '',
  duracionMinutos: 30, precioBase: '', recargoPorTamano: 0
};

function pesos(valor) {
  if (valor === null || valor === undefined) return '—';
  return new Intl.NumberFormat('es-CO',
    { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
}

export default function Servicios() {
  const [servicios, setServicios] = useState(null);
  const [editando, setEditando] = useState(null);
  const [mensaje, setMensaje] = useState(null);

  useEffect(() => { cargar(); }, []);

  async function cargar() {
    try {
      setServicios(await api.serviciosTodos());
    } catch (e) {
      setServicios([]);
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

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

  return (
    <section>
      {mensaje && (
        <p className={mensaje.tipo === 'error' ? 'aviso-error' : 'aviso-ok'}>
          {mensaje.texto}
        </p>
      )}

      <div className="panel-barra">
        <button type="button" className="btn-solido"
                onClick={() => setEditando({ ...VACIO, id: null })}>
          + Nuevo servicio
        </button>
        <span className="panel-conteo">
          {servicios === null ? 'Cargando…' : `${servicios.length} servicio(s)`}
        </span>
      </div>

      {servicios !== null && (
        <div className="tabla-envoltura">
          <table className="tabla-panel">
            <thead>
              <tr>
                <th>Servicio</th>
                <th>Tipo</th>
                <th>Duración</th>
                <th>Precio base</th>
                <th>Recargo por tamaño</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {servicios.map((s) => (
                <tr key={s.id} className={s.activo ? '' : 'fila-inactiva'}>
                  <td>
                    <strong>{s.nombre}</strong>
                    {s.descripcion && <small>{s.descripcion}</small>}
                    {!s.activo && <small>Retirado del catálogo</small>}
                  </td>
                  <td>
                    <span className="estado-badge estado-atendida">
                      {s.tipo === 'SPA' ? 'Spa' : 'Consulta'}
                    </span>
                  </td>
                  <td>{s.duracionMinutos} min</td>
                  <td>{pesos(s.precioBase)}</td>
                  <td>{s.tipo === 'SPA' ? pesos(s.recargoPorTamano) : '—'}</td>
                  <td className="celda-acciones">
                    {s.activo ? (
                      <>
                        <button type="button" className="btn-mini"
                                onClick={() => setEditando({ ...s })}>Editar</button>
                        <button type="button" className="btn-mini peligro"
                                onClick={() => accion(() => api.desactivarServicio(s.id),
                                                      'Servicio retirado del catálogo.')}>
                          Retirar
                        </button>
                      </>
                    ) : (
                      <button type="button" className="btn-mini"
                              onClick={() => accion(() => api.reactivarServicio(s.id),
                                                    'Servicio reactivado.')}>
                        Reactivar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editando && (
        <FormServicio servicio={editando}
                      onCerrar={() => setEditando(null)}
                      onGuardar={async (cuerpo) => {
                        if (editando.id) await api.actualizarServicio(editando.id, cuerpo);
                        else await api.crearServicio(cuerpo);
                        setEditando(null);
                        setMensaje({ tipo: 'ok', texto: 'Servicio guardado.' });
                        await cargar();
                      }} />
      )}
    </section>
  );
}

function FormServicio({ servicio, onGuardar, onCerrar }) {
  const nuevo = !servicio.id;
  const [datos, setDatos] = useState({
    nombre: servicio.nombre || '',
    tipo: servicio.tipo || 'CONSULTA',
    descripcion: servicio.descripcion || '',
    duracionMinutos: servicio.duracionMinutos ?? 30,
    precioBase: servicio.precioBase ?? '',
    recargoPorTamano: servicio.recargoPorTamano ?? 0
  });
  const [error, setError] = useState(null);

  function cambiar(e) {
    setDatos({ ...datos, [e.target.name]: e.target.value });
  }

  async function enviar(e) {
    e.preventDefault();
    setError(null);
    try {
      await onGuardar({
        ...datos,
        duracionMinutos: Number(datos.duracionMinutos),
        precioBase: Number(datos.precioBase),
        recargoPorTamano: datos.tipo === 'SPA' ? Number(datos.recargoPorTamano) : 0
      });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="modal-fondo" role="dialog" aria-modal="true">
      <div className="modal">
        <header className="modal-cabecera">
          <h2>{nuevo ? 'Nuevo servicio' : 'Editar servicio'}</h2>
        </header>
        <form className="modal-cuerpo" onSubmit={enviar}>
          <label>Nombre
            <input name="nombre" required maxLength={80}
                   value={datos.nombre} onChange={cambiar} />
          </label>

          <label>Tipo
            <select name="tipo" value={datos.tipo} onChange={cambiar} disabled={!nuevo}>
              <option value="CONSULTA">Consulta veterinaria</option>
              <option value="SPA">Estética y spa</option>
            </select>
          </label>
          {!nuevo && (
            <p className="nota">
              El tipo no se puede cambiar: hay citas pasadas cuyo precio se
              calculó con esa regla. Para cambiarlo, retire este servicio y
              cree uno nuevo.
            </p>
          )}

          <label>Descripción
            <input name="descripcion" maxLength={255}
                   value={datos.descripcion} onChange={cambiar} />
          </label>

          <div className="fila-doble">
            <label>Duración (minutos)
              <input name="duracionMinutos" type="number" min="5" max="480" required
                     value={datos.duracionMinutos} onChange={cambiar} />
            </label>
            <label>Precio base
              <input name="precioBase" type="number" min="1" required
                     value={datos.precioBase} onChange={cambiar} />
            </label>
          </div>

          <p className="nota">
            La duración debe ser el tiempo real que ocupa la mascota, no el de
            la atención efectiva: con ella el sistema decide qué horarios
            quedan libres (RN-09).
          </p>

          {datos.tipo === 'SPA' && (
            <>
              <label>Recargo por tamaño
                <input name="recargoPorTamano" type="number" min="0"
                       value={datos.recargoPorTamano} onChange={cambiar} />
              </label>
              <p className="nota">
                Pequeño paga el precio base; mediano, base más un recargo;
                grande, base más dos recargos.
              </p>
            </>
          )}

          {error && <p className="aviso-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="btn-plano" onClick={onCerrar}>Cancelar</button>
            <button type="submit" className="btn-solido">Guardar</button>
          </div>
        </form>
      </div>
    </div>
  );
}
