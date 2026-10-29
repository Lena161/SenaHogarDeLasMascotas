// Página Nosotros.
// Rediseño: historia, misión y visión dejan de ser un muro de texto
// corrido y se leen en tres columnas paralelas; el equipo se presenta
// bajo el mismo arco de la portada, repitiendo el motivo de la marca.
import Hero from '../components/Hero';
import {
  IconoCorazon, IconoEscudo, IconoReloj, IconoCheck,
  IconoConsulta, IconoSpa, IconoLaboratorio
} from '../components/Iconos';

const VALORES = [
  { icono: <IconoCorazon />, titulo: 'Amor por los animales', texto: 'Cada mascota es tratada con cariño, paciencia y respeto.' },
  { icono: <IconoEscudo />, titulo: 'Compromiso', texto: 'Responsabilidad y seguimiento en cada tratamiento.' },
  { icono: <IconoReloj />, titulo: 'Puntualidad', texto: 'Respetamos tu tiempo y el de tu mascota.' },
  { icono: <IconoCheck />, titulo: 'Calidad profesional', texto: 'Equipo capacitado y en constante actualización.' }
];

const EQUIPO = [
  { icono: <IconoConsulta />, nombre: '[Nombre del veterinario]', cargo: 'Médico veterinario' },
  { icono: <IconoLaboratorio />, nombre: '[Nombre del auxiliar]', cargo: 'Auxiliar veterinario' },
  { icono: <IconoSpa />, nombre: '[Nombre del groomer]', cargo: 'Spa y peluquería' }
];

export default function Nosotros() {
  return (
    <main>
      <Hero
        titulo="Una familia que cuida de otra familia"
        subtitulo="Somos una clínica de barrio en Itagüí, y tratamos a cada paciente como si fuera nuestro."
      />

      <div className="nosotros-section">
        <div className="relato">
          <div>
            <h2>Nuestra historia</h2>
            <p>
              Nacimos del amor por los animales y de las ganas de ofrecerle a las familias
              de nuestra comunidad un lugar cercano y confiable para el cuidado de sus
              mascotas. Hemos crecido gracias a la confianza de quienes nos visitan.
            </p>
          </div>
          <div>
            <h2>Misión</h2>
            <p>
              Brindar atención veterinaria integral, ética y de calidad, combinando
              conocimiento profesional con calidez humana, para mejorar la salud y la
              calidad de vida de las mascotas y la tranquilidad de sus familias.
            </p>
          </div>
          <div>
            <h2>Visión</h2>
            <p>
              Ser reconocidos como el hogar veterinario de referencia en la región, por la
              excelencia en el servicio, la innovación en nuestros tratamientos y el trato
              humano hacia cada paciente y su familia.
            </p>
          </div>
        </div>
      </div>

      <div className="nosotros-section">
        <div className="section-header">
          <h2>En qué creemos</h2>
          <p>Cuatro cosas que no negociamos, por más lleno que esté el consultorio.</p>
        </div>
        <div className="valores-grid">
          {VALORES.map((v) => (
            <div className="valor-card" key={v.titulo}>
              <div className="icon-wrap">{v.icono}</div>
              <h3>{v.titulo}</h3>
              <p>{v.texto}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="equipo-section">
        <div className="equipo-inner">
          <div className="section-header">
            <h2>Quiénes te atienden</h2>
            <p>El equipo que vas a encontrar cuando llegues con tu mascota.</p>
          </div>
          <div className="equipo-grid">
            {EQUIPO.map((m) => (
              <div className="equipo-card" key={m.cargo}>
                <div className="avatar">{m.icono}</div>
                <h3>{m.nombre}</h3>
                <span>{m.cargo}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}
