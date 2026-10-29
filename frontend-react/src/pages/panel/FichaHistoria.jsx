// ============================================================
// Presentación de la historia clínica (RF-10).
//
// Se muestra como línea de tiempo del más reciente al más
// antiguo, con las constantes vitales destacadas: leer la
// evolución de un paciente es, sobre todo, comparar esos
// valores entre consultas.
//
// Componente aparte porque lo usan dos pantallas: la sección
// clínica de la veterinaria y la ficha del cliente.
// ============================================================

const CONDICION = {
  1: 'caquéctico', 2: 'delgado', 3: 'ideal', 4: 'sobrepeso', 5: 'obeso'
};

function fecha(valor) {
  return new Date(valor).toLocaleDateString('es-CO',
    { day: '2-digit', month: 'short', year: 'numeric' });
}

function fechaCorta(valor) {
  return new Date(valor + 'T00:00:00').toLocaleDateString('es-CO',
    { day: '2-digit', month: 'short', year: 'numeric' });
}

/** Una constante vital, omitida si no se tomó. */
function Constante({ etiqueta, valor, unidad }) {
  if (valor === null || valor === undefined) return null;
  return (
    <div className="constante">
      <dt>{etiqueta}</dt>
      <dd>{valor}<span>{unidad}</span></dd>
    </div>
  );
}

/** Un apartado de texto, omitido si está vacío. */
function Apartado({ titulo, texto }) {
  if (!texto) return null;
  return (
    <div className="apartado-clinico">
      <h4>{titulo}</h4>
      <p>{texto}</p>
    </div>
  );
}

export default function FichaHistoria({ registros, titulo }) {
  if (registros.length === 0) {
    return (
      <article className="tarjeta-panel">
        {titulo && <h3>{titulo}</h3>}
        <p className="panel-vacio">
          Este paciente aún no tiene atenciones registradas.
        </p>
      </article>
    );
  }

  return (
    <article className="tarjeta-panel">
      <h3>{titulo || `Historia clínica · ${registros.length} atención(es)`}</h3>

      <ol className="historia">
        {registros.map((h) => {
          const hayConstantes = h.temperaturaC || h.frecuenciaCardiaca
            || h.frecuenciaRespiratoria || h.pesoKg || h.condicionCorporal;

          return (
            <li key={h.id} className="atencion">
              <header className="atencion-cabecera">
                <div>
                  <span className="atencion-fecha">{fecha(h.fecha)}</span>
                  <h4>{h.motivo}</h4>
                </div>
                <small>
                  {h.veterinario}
                  {h.servicio ? ` · ${h.servicio}` : ' · sin cita previa'}
                </small>
              </header>

              {hayConstantes && (
                <dl className="constantes">
                  <Constante etiqueta="Temperatura" valor={h.temperaturaC} unidad="°C" />
                  <Constante etiqueta="FC" valor={h.frecuenciaCardiaca} unidad=" lpm" />
                  <Constante etiqueta="FR" valor={h.frecuenciaRespiratoria} unidad=" rpm" />
                  <Constante etiqueta="Peso" valor={h.pesoKg} unidad=" kg" />
                  {h.condicionCorporal && (
                    <div className="constante">
                      <dt>Condición</dt>
                      <dd>{h.condicionCorporal}<span>/5 · {CONDICION[h.condicionCorporal]}</span></dd>
                    </div>
                  )}
                </dl>
              )}

              <Apartado titulo="Anamnesis" texto={h.anamnesis} />
              <Apartado titulo="Examen físico" texto={h.examenFisico} />
              <Apartado titulo="Diagnóstico" texto={h.diagnostico} />
              <Apartado titulo="Tratamiento" texto={h.tratamiento} />
              <Apartado titulo="Medicamentos" texto={h.medicamentos} />
              <Apartado titulo="Vacunas aplicadas" texto={h.vacunasAplicadas} />
              <Apartado titulo="Desparasitación" texto={h.desparasitacion} />
              <Apartado titulo="Observaciones" texto={h.observaciones} />

              {h.proximoControl && (
                <p className="proximo-control">
                  Próximo control: {fechaCorta(h.proximoControl)}
                </p>
              )}
            </li>
          );
        })}
      </ol>
    </article>
  );
}
