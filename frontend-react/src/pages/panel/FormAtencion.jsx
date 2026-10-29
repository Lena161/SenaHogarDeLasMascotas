// ============================================================
// Registro de la atención (RF-09).
//
// El formulario cambia según el servicio: una consulta pide
// motivo y diagnóstico; un baño no genera historia clínica y
// solo confirma que se prestó. Esa diferencia no se decide aquí
// sino en el backend, que la resuelve por polimorfismo; la
// interfaz únicamente refleja lo que el servicio ya declaró.
// ============================================================
import { useState } from 'react';

export default function FormAtencion({ cita, onGuardar, onCerrar }) {
  const clinica = cita.requiereHistorial;
  const [datos, setDatos] = useState({
    motivo: cita.observaciones || '',
    diagnostico: '',
    tratamiento: '',
    vacunasAplicadas: '',
    observaciones: '',
    pesoKg: ''
  });
  const [error, setError] = useState(null);
  const [guardando, setGuardando] = useState(false);

  function cambiar(e) {
    setDatos({ ...datos, [e.target.name]: e.target.value });
  }

  async function enviar(e) {
    e.preventDefault();
    setError(null);
    setGuardando(true);
    try {
      await onGuardar(clinica
        ? { ...datos, pesoKg: datos.pesoKg === '' ? null : Number(datos.pesoKg) }
        : {});
    } catch (err) {
      setError(err.message);
      setGuardando(false);
    }
  }

  return (
    <div className="modal-fondo" role="dialog" aria-modal="true"
         aria-label="Registrar atención">
      <div className="modal">
        <header className="modal-cabecera">
          <h2>{clinica ? 'Registrar atención' : 'Confirmar servicio prestado'}</h2>
          <p className="modal-sub">
            {cita.mascota} · {cita.servicio}
          </p>
        </header>

        <form onSubmit={enviar} className="modal-cuerpo">
          {!clinica && (
            <p className="nota">
              Este servicio no genera historia clínica. Al guardar, la cita
              queda registrada como atendida.
            </p>
          )}

          {clinica && (
            <>
              <label>Motivo de la consulta
                <input name="motivo" value={datos.motivo} onChange={cambiar}
                       required maxLength={255} />
              </label>

              <label>Diagnóstico
                <textarea name="diagnostico" value={datos.diagnostico} onChange={cambiar}
                          required rows={3} maxLength={500} />
              </label>

              <label>Tratamiento indicado
                <textarea name="tratamiento" value={datos.tratamiento} onChange={cambiar}
                          rows={2} maxLength={500} />
              </label>

              <div className="fila-doble">
                <label>Vacunas aplicadas
                  <input name="vacunasAplicadas" value={datos.vacunasAplicadas}
                         onChange={cambiar} maxLength={255} />
                </label>
                <label>Peso en consulta (kg)
                  <input name="pesoKg" type="number" step="0.1" min="0.1"
                         value={datos.pesoKg} onChange={cambiar} />
                </label>
              </div>

              <label>Observaciones
                <textarea name="observaciones" value={datos.observaciones}
                          onChange={cambiar} rows={2} maxLength={500} />
              </label>

              <p className="nota">
                El peso registrado actualiza la ficha del paciente. La historia
                clínica no se edita después: una corrección se hace agregando
                un registro nuevo.
              </p>
            </>
          )}

          {error && <p className="aviso-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="btn-plano" onClick={onCerrar}>
              Cancelar
            </button>
            <button type="submit" className="btn-solido" disabled={guardando}>
              {guardando ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
