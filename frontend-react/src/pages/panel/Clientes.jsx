// ============================================================
// Clientes y pacientes (RF-03) e historia clínica (RF-10).
//
// Disposición maestro-detalle: a la izquierda la búsqueda, a la
// derecha la ficha del cliente con sus pacientes. En recepción no
// se sabe con qué dato llega la persona, así que la búsqueda es
// una sola caja que consulta nombre, documento, teléfono o correo.
// ============================================================
import { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import FichaHistoria from './FichaHistoria';

/* El enum de la base no lleva eñe (PEQUENO) por compatibilidad del
   esquema; la interfaz sí debe mostrarlo bien escrito. */
const TAMANO = { PEQUENO: 'pequeño', MEDIANO: 'mediano', GRANDE: 'grande' };

const VACIO = {
  tipoDocumento: 'CC', numeroDocumento: '', nombres: '',
  apellidos: '', telefono: '', correo: '', direccion: ''
};

export default function Clientes() {
  const { usuario } = useAuth();
  const puedeEditar = ['ADMIN', 'RECEPCION'].includes(usuario.rol);

  const [texto, setTexto] = useState('');
  const [resultados, setResultados] = useState([]);
  const [ficha, setFicha] = useState(null);
  const [historial, setHistorial] = useState(null);
  const [editando, setEditando] = useState(null);
  const [creando, setCreando] = useState(false);
  const [nuevo, setNuevo] = useState(VACIO);
  const [mensaje, setMensaje] = useState(null);

  useEffect(() => { buscar(''); }, []);

  async function buscar(q) {
    setMensaje(null);
    try {
      setResultados(await api.buscarClientes(q));
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function abrir(id) {
    setHistorial(null);
    setEditando(null);
    try {
      setFicha(await api.detalleCliente(id));
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function verHistorial(mascota) {
    setMensaje(null);
    try {
      const registros = await api.historialDeMascota(mascota.id);
      setHistorial({ mascota, registros });
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function guardarEdicion(e) {
    e.preventDefault();
    setMensaje(null);
    try {
      await api.actualizarCliente(editando.id, {
        tipoDocumento: editando.tipoDocumento,
        numeroDocumento: editando.numeroDocumento,
        nombres: editando.nombres,
        apellidos: editando.apellidos,
        telefono: editando.telefono,
        direccion: editando.direccion || ''
      });
      setMensaje({ tipo: 'ok', texto: 'Datos actualizados.' });
      setEditando(null);
      await abrir(ficha.cliente.id);
      await buscar(texto);
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  async function guardarNuevo(e) {
    e.preventDefault();
    setMensaje(null);
    try {
      const creado = await api.crearCliente(nuevo);
      setMensaje({ tipo: 'ok', texto: 'Cliente registrado.' });
      setCreando(false);
      setNuevo(VACIO);
      await buscar('');
      await abrir(creado.id);
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section className="maestro-detalle">
      <aside className="columna-busqueda">
        <form className="buscador" onSubmit={(e) => { e.preventDefault(); buscar(texto); }}>
          <input type="search" value={texto} placeholder="Nombre, documento o teléfono"
                 onChange={(e) => setTexto(e.target.value)} />
          <button type="submit" className="btn-solido">Buscar</button>
        </form>

        {puedeEditar && (
          <button type="button" className="btn-plano ancho"
                  onClick={() => { setCreando(true); setFicha(null); }}>
            + Registrar cliente nuevo
          </button>
        )}

        <ul className="lista-clientes">
          {resultados.map((c) => (
            <li key={c.id}>
              <button type="button"
                      className={ficha?.cliente.id === c.id ? 'item activo' : 'item'}
                      onClick={() => { setCreando(false); abrir(c.id); }}>
                <strong>{c.nombreCompleto}</strong>
                <small>{c.numeroDocumento} · {c.telefono}</small>
                <small>{c.cantidadMascotas} paciente(s)</small>
              </button>
            </li>
          ))}
          {resultados.length === 0 && (
            <li className="panel-vacio">Sin resultados.</li>
          )}
        </ul>
      </aside>

      <div className="columna-ficha">
        {mensaje && (
          <p className={mensaje.tipo === 'error' ? 'aviso-error' : 'aviso-ok'}>
            {mensaje.texto}
          </p>
        )}

        {creando && (
          <form className="tarjeta-panel" onSubmit={guardarNuevo}>
            <h2>Registrar cliente</h2>
            <p className="nota">
              Para quien llega al mostrador sin cuenta en línea. No se le crea
              usuario: puede registrarse después con este mismo correo.
            </p>
            <div className="fila-doble">
              <label>Nombres
                <input required value={nuevo.nombres}
                       onChange={(e) => setNuevo({ ...nuevo, nombres: e.target.value })} />
              </label>
              <label>Apellidos
                <input required value={nuevo.apellidos}
                       onChange={(e) => setNuevo({ ...nuevo, apellidos: e.target.value })} />
              </label>
            </div>
            <div className="fila-doble">
              <label>Tipo de documento
                <select value={nuevo.tipoDocumento}
                        onChange={(e) => setNuevo({ ...nuevo, tipoDocumento: e.target.value })}>
                  <option value="CC">Cédula de ciudadanía</option>
                  <option value="CE">Cédula de extranjería</option>
                  <option value="PASAPORTE">Pasaporte</option>
                </select>
              </label>
              <label>Número
                <input required value={nuevo.numeroDocumento}
                       onChange={(e) => setNuevo({ ...nuevo, numeroDocumento: e.target.value })} />
              </label>
            </div>
            <div className="fila-doble">
              <label>Teléfono
                <input required value={nuevo.telefono}
                       onChange={(e) => setNuevo({ ...nuevo, telefono: e.target.value })} />
              </label>
              <label>Correo electrónico
                <input type="email" required value={nuevo.correo}
                       onChange={(e) => setNuevo({ ...nuevo, correo: e.target.value })} />
              </label>
            </div>
            <label>Dirección
              <input value={nuevo.direccion}
                     onChange={(e) => setNuevo({ ...nuevo, direccion: e.target.value })} />
            </label>
            <div className="modal-acciones">
              <button type="button" className="btn-plano"
                      onClick={() => setCreando(false)}>Cancelar</button>
              <button type="submit" className="btn-solido">Registrar</button>
            </div>
          </form>
        )}

        {!creando && !ficha && (
          <p className="panel-vacio">Seleccione un cliente del listado.</p>
        )}

        {!creando && ficha && !editando && (
          <article className="tarjeta-panel">
            <header className="ficha-cabecera">
              <div>
                <h2>{ficha.cliente.nombreCompleto}</h2>
                <p className="modal-sub">
                  {ficha.cliente.tipoDocumento} {ficha.cliente.numeroDocumento}
                </p>
              </div>
              {puedeEditar && (
                <button type="button" className="btn-mini"
                        onClick={() => setEditando({ ...ficha.cliente })}>
                  Editar datos
                </button>
              )}
            </header>

            <dl className="datos-ficha">
              <div><dt>Teléfono</dt><dd>{ficha.cliente.telefono}</dd></div>
              <div><dt>Correo</dt><dd>{ficha.cliente.correo}</dd></div>
              <div><dt>Dirección</dt><dd>{ficha.cliente.direccion || 'Sin registrar'}</dd></div>
            </dl>

            <h3>Pacientes</h3>
            {ficha.mascotas.length === 0 && (
              <p className="panel-vacio">Este cliente no tiene pacientes registrados.</p>
            )}
            <ul className="lista-mascotas">
              {ficha.mascotas.map((m) => (
                <li key={m.id}>
                  <div>
                    <strong>{m.nombre}</strong>
                    <small>
                      {m.especie}{m.raza ? ` · ${m.raza}` : ''} · {TAMANO[m.tamano] || m.tamano}
                      {m.pesoKg ? ` · ${m.pesoKg} kg` : ''}
                    </small>
                  </div>
                  <button type="button" className="btn-mini"
                          onClick={() => verHistorial(m)}>
                    Ver historia clínica
                  </button>
                </li>
              ))}
            </ul>
          </article>
        )}

        {editando && (
          <form className="tarjeta-panel" onSubmit={guardarEdicion}>
            <h2>Editar datos del cliente</h2>
            <p className="nota">
              El correo no se edita aquí: también es el usuario con el que
              inicia sesión, y cambiarlo dejaría la cuenta inconsistente.
            </p>
            <div className="fila-doble">
              <label>Nombres
                <input required value={editando.nombres}
                       onChange={(e) => setEditando({ ...editando, nombres: e.target.value })} />
              </label>
              <label>Apellidos
                <input required value={editando.apellidos}
                       onChange={(e) => setEditando({ ...editando, apellidos: e.target.value })} />
              </label>
            </div>
            <div className="fila-doble">
              <label>Documento
                <input required value={editando.numeroDocumento}
                       onChange={(e) => setEditando({ ...editando, numeroDocumento: e.target.value })} />
              </label>
              <label>Teléfono
                <input required value={editando.telefono}
                       onChange={(e) => setEditando({ ...editando, telefono: e.target.value })} />
              </label>
            </div>
            <label>Dirección
              <input value={editando.direccion || ''}
                     onChange={(e) => setEditando({ ...editando, direccion: e.target.value })} />
            </label>
            <div className="modal-acciones">
              <button type="button" className="btn-plano"
                      onClick={() => setEditando(null)}>Cancelar</button>
              <button type="submit" className="btn-solido">Guardar cambios</button>
            </div>
          </form>
        )}

        {historial && (
          <>
            <div className="panel-barra">
              <button type="button" className="btn-plano"
                      onClick={() => setHistorial(null)}>
                Cerrar historia clínica
              </button>
            </div>
            <FichaHistoria
              registros={historial.registros}
              titulo={`Historia clínica de ${historial.mascota.nombre}`} />
          </>
        )}
      </div>
    </section>
  );
}
