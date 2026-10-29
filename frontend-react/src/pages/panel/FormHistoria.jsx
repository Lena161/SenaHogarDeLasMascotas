// ============================================================
// Formato de historia clínica veterinaria (RF-09).
//
// Un solo componente sirve a los dos caminos de registro:
//   - desde una cita de la agenda (lleva `cita`)
//   - desde la sección clínica, sin cita previa (lleva `paciente`)
// Son el mismo acto clínico; lo único que cambia es si la
// atención estaba agendada, así que no tiene sentido mantener
// dos formularios que habría que corregir por separado.
//
// La estructura sigue el orden en que se hace la consulta:
// se escucha al propietario, se toman las constantes, se
// examina, se concluye y se indica. Un formulario que sigue el
// procedimiento se llena mientras se atiende; uno que agrupa
// por tipo de dato obliga a saltar de un lado a otro.
// ============================================================
import { useState } from 'react';

const INICIAL = {
  motivo: '', anamnesis: '',
  temperaturaC: '', frecuenciaCardiaca: '', frecuenciaRespiratoria: '',
  pesoKg: '', condicionCorporal: '',
  examenFisico: '', diagnostico: '', tratamiento: '', medicamentos: '',
  vacunasAplicadas: '', desparasitacion: '', proximoControl: '',
  observaciones: ''
};

/** Convierte "" en null y deja number donde corresponde. */
function limpiar(datos) {
  const numericos = ['temperaturaC', 'pesoKg'];
  const enteros = ['frecuenciaCardiaca', 'frecuenciaRespiratoria', 'condicionCorporal'];
  const salida = {};
  Object.entries(datos).forEach(([clave, valor]) => {
    if (valor === '' || valor === null) { salida[clave] = null; return; }
    if (numericos.includes(clave)) { salida[clave] = Number(valor); return; }
    if (enteros.includes(clave)) { salida[clave] = parseInt(valor, 10); return; }
    salida[clave] = valor;
  });
  return salida;
}

export default function FormHistoria({ cita, paciente, onGuardar, onCerrar }) {
  // Un servicio de spa no genera historia clínica: lo declara el
  // propio servicio en el backend y aquí solo se refleja.
  const soloConfirmar = cita ? !cita.requiereHistorial : false;

  const nombrePaciente = cita ? cita.mascota : paciente.nombre;
  const pesoPrevio = paciente?.pesoKg ?? null;

  const [datos, setDatos] = useState({
    ...INICIAL,
    motivo: cita?.observaciones || ''
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
      await onGuardar(soloConfirmar ? {} : limpiar(datos));
    } catch (err) {
      setError(err.message);
      setGuardando(false);
    }
  }

  return (
    <div className="modal-fondo" role="dialog" aria-modal="true"
         aria-label="Historia clínica">
      <div className="modal ancho">
        <header className="modal-cabecera">
          <h2>
            {soloConfirmar ? 'Confirmar servicio prestado' : 'Historia clínica'}
          </h2>
          <p className="modal-sub">
            {nombrePaciente}
            {cita ? ` · ${cita.servicio}` : ' · atención sin cita previa'}
            {paciente ? ` · ${paciente.especie.toLowerCase()}${paciente.raza ? `, ${paciente.raza}` : ''}` : ''}
          </p>
        </header>

        <form onSubmit={enviar} className="modal-cuerpo">
          {soloConfirmar && (
            <p className="nota">
              Este servicio no genera historia clínica. Al guardar, la cita
              queda registrada como atendida.
            </p>
          )}

          {!soloConfirmar && (
            <>
              {/* ---------- Anamnesis ---------- */}
              <fieldset className="bloque-clinico">
                <legend>Anamnesis</legend>

                <label>Motivo de consulta
                  <input name="motivo" value={datos.motivo} onChange={cambiar}
                         required maxLength={255}
                         placeholder="Control anual, decaimiento, herida en pata…" />
                </label>

                <label>Relato del propietario
                  <textarea name="anamnesis" value={datos.anamnesis} onChange={cambiar}
                            rows={3} maxLength={1000}
                            placeholder="Desde cuándo, cómo empezó, qué cambió en apetito, ánimo, orina o heces…" />
                </label>
              </fieldset>

              {/* ---------- Constantes ---------- */}
              <fieldset className="bloque-clinico">
                <legend>Constantes vitales</legend>

                <div className="rejilla-constantes">
                  <label>Temperatura (°C)
                    <input name="temperaturaC" type="number" step="0.1"
                           min="25" max="45"
                           value={datos.temperaturaC} onChange={cambiar} />
                  </label>
                  <label>FC (lpm)
                    <input name="frecuenciaCardiaca" type="number"
                           min="10" max="400"
                           value={datos.frecuenciaCardiaca} onChange={cambiar} />
                  </label>
                  <label>FR (rpm)
                    <input name="frecuenciaRespiratoria" type="number"
                           min="5" max="200"
                           value={datos.frecuenciaRespiratoria} onChange={cambiar} />
                  </label>
                  <label>Peso (kg)
                    <input name="pesoKg" type="number" step="0.1" min="0.1"
                           value={datos.pesoKg} onChange={cambiar} />
                  </label>
                  <label>Condición corporal
                    <select name="condicionCorporal"
                            value={datos.condicionCorporal} onChange={cambiar}>
                      <option value="">—</option>
                      <option value="1">1 · Caquéctico</option>
                      <option value="2">2 · Delgado</option>
                      <option value="3">3 · Ideal</option>
                      <option value="4">4 · Sobrepeso</option>
                      <option value="5">5 · Obeso</option>
                    </select>
                  </label>
                </div>

                {pesoPrevio && (
                  <p className="nota">
                    Peso registrado en la ficha: {pesoPrevio} kg. El valor que
                    anote aquí la actualiza.
                  </p>
                )}
              </fieldset>

              {/* ---------- Examen y diagnóstico ---------- */}
              <fieldset className="bloque-clinico">
                <legend>Examen y diagnóstico</legend>

                <label>Hallazgos del examen físico
                  <textarea name="examenFisico" value={datos.examenFisico}
                            onChange={cambiar} rows={3} maxLength={1000}
                            placeholder="Mucosas, llenado capilar, auscultación, palpación abdominal, piel y pelaje, dentadura…" />
                </label>

                <label>Diagnóstico
                  <textarea name="diagnostico" value={datos.diagnostico}
                            onChange={cambiar} required rows={2} maxLength={500}
                            placeholder="Presuntivo o definitivo. Si está pendiente de exámenes, indíquelo." />
                </label>
              </fieldset>

              {/* ---------- Plan terapéutico ---------- */}
              <fieldset className="bloque-clinico">
                <legend>Plan terapéutico</legend>

                <label>Tratamiento indicado
                  <textarea name="tratamiento" value={datos.tratamiento}
                            onChange={cambiar} rows={2} maxLength={500} />
                </label>

                <label>Medicamentos prescritos
                  <textarea name="medicamentos" value={datos.medicamentos}
                            onChange={cambiar} rows={2} maxLength={500}
                            placeholder="Nombre, dosis, vía y duración" />
                </label>

                <div className="fila-doble">
                  <label>Vacunas aplicadas
                    <input name="vacunasAplicadas" value={datos.vacunasAplicadas}
                           onChange={cambiar} maxLength={255} />
                  </label>
                  <label>Desparasitación
                    <input name="desparasitacion" value={datos.desparasitacion}
                           onChange={cambiar} maxLength={255} />
                  </label>
                </div>

                <div className="fila-doble">
                  <label>Próximo control
                    <input name="proximoControl" type="date"
                           value={datos.proximoControl} onChange={cambiar} />
                  </label>
                  <label>Observaciones
                    <input name="observaciones" value={datos.observaciones}
                           onChange={cambiar} maxLength={500}
                           placeholder="Recomendaciones al propietario" />
                  </label>
                </div>
              </fieldset>

              <p className="nota">
                Una vez guardada, la historia clínica no se edita: una
                corrección se hace agregando un registro nuevo. Así la
                evolución del paciente queda trazable.
              </p>
            </>
          )}

          {error && <p className="aviso-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="btn-plano" onClick={onCerrar}>
              Cancelar
            </button>
            <button type="submit" className="btn-solido" disabled={guardando}>
              {guardando ? 'Guardando…'
                : soloConfirmar ? 'Marcar atendida' : 'Guardar historia clínica'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
