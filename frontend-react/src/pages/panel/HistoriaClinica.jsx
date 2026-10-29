// ============================================================
// Sección clínica de la veterinaria (RF-09, RF-10).
//
// Permite abrir la historia de un paciente sin pasar por la
// agenda. Hace falta porque no toda atención viene de una cita:
// una urgencia, una revisión de mostrador o una vacuna que se
// aplica de paso también deben quedar registradas.
// ============================================================
import { useEffect, useState } from 'react';
import { api } from '../../services/api';
import FormHistoria from './FormHistoria';
import FichaHistoria from './FichaHistoria';

const TAMANO = { PEQUENO: 'pequeño', MEDIANO: 'mediano', GRANDE: 'grande' };

export default function HistoriaClinica() {
  const [texto, setTexto] = useState('');
  const [pacientes, setPacientes] = useState([]);
  const [paciente, setPaciente] = useState(null);
  const [registros, setRegistros] = useState(null);
  const [registrando, setRegistrando] = useState(false);
  const [mensaje, setMensaje] = useState(null);

  useEffect(() => { buscar(''); }, []);

  async function buscar(q) {
    setMensaje(null);
    try {
      setPacientes(await api.buscarPacientes(q));
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function abrir(m) {
    setPaciente(m);
    setRegistros(null);
    try {
      setRegistros(await api.historialDeMascota(m.id));
    } catch (e) {
      setRegistros([]);
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function guardar(datos) {
    await api.registrarHistorialDirecto(paciente.id, datos);
    setRegistrando(false);
    setMensaje({ tipo: 'ok', texto: 'Historia clínica registrada.' });
    await abrir(paciente);
    await buscar(texto); // el peso pudo cambiar
  }

  return (
    <section className="maestro-detalle">
      <aside className="columna-busqueda">
        <form className="buscador" onSubmit={(e) => { e.preventDefault(); buscar(texto); }}>
          <input type="search" value={texto} placeholder="Paciente o dueño"
                 onChange={(e) => setTexto(e.target.value)} />
          <button type="submit" className="btn-solido">Buscar</button>
        </form>

        <ul className="lista-clientes">
          {pacientes.map((m) => (
            <li key={m.id}>
              <button type="button"
                      className={paciente?.id === m.id ? 'item activo' : 'item'}
                      onClick={() => abrir(m)}>
                <strong>{m.nombre}</strong>
                <small>
                  {m.especie.toLowerCase()}
                  {m.raza ? ` · ${m.raza}` : ''} · {TAMANO[m.tamano] || m.tamano}
                </small>
                <small>{m.dueno}</small>
              </button>
            </li>
          ))}
          {pacientes.length === 0 && <li className="panel-vacio">Sin resultados.</li>}
        </ul>
      </aside>

      <div className="columna-ficha">
        {mensaje && (
          <p className={mensaje.tipo === 'error' ? 'aviso-error' : 'aviso-ok'}>
            {mensaje.texto}
          </p>
        )}

        {!paciente && (
          <p className="panel-vacio">
            Busque un paciente para ver o registrar su historia clínica.
          </p>
        )}

        {paciente && (
          <article className="tarjeta-panel">
            <header className="ficha-cabecera">
              <div>
                <h2>{paciente.nombre}</h2>
                <p className="modal-sub">
                  {paciente.especie.toLowerCase()}
                  {paciente.raza ? `, ${paciente.raza}` : ''}
                  {paciente.sexo ? ` · ${paciente.sexo === 'M' ? 'macho' : 'hembra'}` : ''}
                  {paciente.edad ? ` · ${paciente.edad} año(s)` : ''}
                  {paciente.pesoKg ? ` · ${paciente.pesoKg} kg` : ''}
                </p>
                <p className="modal-sub">
                  {paciente.dueno} · {paciente.telefonoDueno}
                </p>
              </div>
              <button type="button" className="btn-solido"
                      onClick={() => setRegistrando(true)}>
                + Nueva atención
              </button>
            </header>
          </article>
        )}

        {paciente && registros !== null && (
          <FichaHistoria registros={registros} />
        )}
      </div>

      {registrando && (
        <FormHistoria paciente={paciente}
                      onGuardar={guardar}
                      onCerrar={() => setRegistrando(false)} />
      )}
    </section>
  );
}
