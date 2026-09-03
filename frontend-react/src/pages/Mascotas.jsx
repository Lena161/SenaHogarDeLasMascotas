// PÁGINA NUEVA de la conmutación (RF-02, RN-03).
// La mascota deja de ser un texto en el formulario de cita y se
// convierte en una entidad del dueño: se registra una vez y se
// reutiliza en todas las reservas y en el futuro historial clínico.
import { useEffect, useState } from 'react';
import Hero from '../components/Hero';
import Panel from '../components/Panel';
import FormMessage from '../components/FormMessage';
import { api } from '../services/api';

const FORM_VACIO = {
  nombre: '', especie: 'PERRO', raza: '',
  fechaNacimiento: '', sexo: 'M', pesoKg: '', tamano: 'MEDIANO'
};

export default function Mascotas() {
  const [mascotas, setMascotas] = useState(null);
  const [mensaje, setMensaje] = useState(null);
  const [formulario, setFormulario] = useState(FORM_VACIO);

  useEffect(() => { cargar(); }, []);

  async function cargar() {
    try {
      setMascotas(await api.misMascotas());
    } catch {
      setMascotas([]);
    }
  }

  function manejarCambio(e) {
    setFormulario({ ...formulario, [e.target.name]: e.target.value });
  }

  async function manejarEnvio(e) {
    e.preventDefault();
    try {
      await api.crearMascota({
        ...formulario,
        // Campos opcionales: se envían null si están vacíos
        raza: formulario.raza || null,
        fechaNacimiento: formulario.fechaNacimiento || null,
        pesoKg: formulario.pesoKg ? Number(formulario.pesoKg) : null
      });
      setMensaje({ tipo: 'success', texto: `¡${formulario.nombre} quedó registrado!` });
      setFormulario(FORM_VACIO);
      cargar();
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.message });
    }
  }

  return (
    <main>
      <Hero compacto titulo="Mis Mascotas"
            subtitulo="Registra a tus compañeros para poder agendar sus citas." />

      <div className="agendar-grid">
        <Panel titulo="Registrar mascota">
          <form className="form-app" onSubmit={manejarEnvio}>
            <FormMessage mensaje={mensaje} />
            <label>Nombre
              <input type="text" name="nombre" required
                     value={formulario.nombre} onChange={manejarCambio} />
            </label>
            <div className="form-row">
              <label>Especie
                <select name="especie" value={formulario.especie} onChange={manejarCambio}>
                  <option value="PERRO">Perro</option>
                  <option value="GATO">Gato</option>
                  <option value="AVE">Ave</option>
                  <option value="ROEDOR">Roedor</option>
                  <option value="OTRO">Otro</option>
                </select>
              </label>
              <label>Sexo
                <select name="sexo" value={formulario.sexo} onChange={manejarCambio}>
                  <option value="M">Macho</option>
                  <option value="H">Hembra</option>
                </select>
              </label>
            </div>
            <div className="form-row">
              <label>Raza (opcional)
                <input type="text" name="raza"
                       value={formulario.raza} onChange={manejarCambio} />
              </label>
              <label>Nacimiento (opcional)
                <input type="date" name="fechaNacimiento"
                       value={formulario.fechaNacimiento} onChange={manejarCambio} />
              </label>
            </div>
            <div className="form-row">
              <label>Peso en kg (opcional)
                <input type="number" name="pesoKg" step="0.1" min="0.1"
                       value={formulario.pesoKg} onChange={manejarCambio} />
              </label>
              <label>Tamaño
                {/* El tamaño alimenta el precio polimórfico de los servicios de spa */}
                <select name="tamano" value={formulario.tamano} onChange={manejarCambio}>
                  <option value="PEQUENO">Pequeño</option>
                  <option value="MEDIANO">Mediano</option>
                  <option value="GRANDE">Grande</option>
                </select>
              </label>
            </div>
            <button type="submit" className="form-submit">Registrar</button>
          </form>
        </Panel>

        <Panel titulo="Mis mascotas registradas">
          <div className="citas-list">
            {mascotas === null && <p className="citas-empty">Cargando...</p>}
            {mascotas !== null && mascotas.length === 0 && (
              <p className="citas-empty">Aún no has registrado mascotas.</p>
            )}
            {mascotas !== null && mascotas.map((m) => (
              <div className="cita-item" key={m.id}>
                <div className="info">
                  <strong>{m.nombre} · {m.especie}{m.raza ? ` (${m.raza})` : ''}</strong>
                  <span>
                    {m.tamano.charAt(0) + m.tamano.slice(1).toLowerCase()}
                    {m.edad > 0 ? ` · ${m.edad} año${m.edad > 1 ? 's' : ''}` : ''}
                    {m.pesoKg ? ` · ${m.pesoKg} kg` : ''}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </main>
  );
}