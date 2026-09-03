// Migración de nosotros.html: historia, misión, visión, valores y equipo.
import Hero from '../components/Hero';
import { IconoCorazon, IconoEscudo, IconoReloj, IconoCheck, IconoConsulta, IconoSpa, IconoLaboratorio } from '../components/Iconos';

const VALORES = [
  { icono: <IconoCorazon />, titulo: 'Amor por los animales', texto: 'Cada mascota es tratada con cariño, paciencia y respeto.' },
  { icono: <IconoEscudo />, titulo: 'Compromiso', texto: 'Responsabilidad y seguimiento en cada tratamiento.' },
  { icono: <IconoReloj />, titulo: 'Puntualidad', texto: 'Respetamos tu tiempo y el de tu mascota.' },
  { icono: <IconoCheck />, titulo: 'Calidad profesional', texto: 'Equipo capacitado y en constante actualización.' }
];

const EQUIPO = [
  { icono: <IconoConsulta />, nombre: '[Nombre del veterinario]', cargo: 'Médico Veterinario' },
  { icono: <IconoLaboratorio />, nombre: '[Nombre del auxiliar]', cargo: 'Auxiliar Veterinario' },
  { icono: <IconoSpa />, nombre: '[Nombre del groomer]', cargo: 'Spa y Peluquería' }
];

export default function Nosotros() {
  return (
    <main>
      <Hero titulo="Sobre Nosotros"
            subtitulo="Una familia de amantes de los animales, comprometida con el bienestar de tu mejor amigo." />

      <div className="nosotros-section">
        <h2>Nuestra Historia</h2>
        <p>Veterinaria El Hogar de Las Mascotas nació del amor por los animales y las ganas de ofrecerles a las familias de nuestra comunidad un lugar cercano y confiable para el cuidado de sus mascotas. Desde nuestros inicios, hemos crecido gracias a la confianza de quienes nos visitan, siempre con el mismo compromiso: tratar a cada paciente como si fuera de la familia.</p>

        <h2>Misión</h2>
        <p>Brindar atención veterinaria integral, ética y de calidad, combinando conocimiento profesional con calidez humana, para mejorar la salud y calidad de vida de las mascotas y la tranquilidad de sus familias.</p>

        <h2>Visión</h2>
        <p>Ser reconocidos como el hogar veterinario de referencia en la región, destacándonos por la excelencia en el servicio, la innovación en nuestros tratamientos y el trato humano hacia cada paciente y su familia.</p>
      </div>

      <div className="nosotros-section" style={{ marginTop: 0 }}>
        <h2 style={{ textAlign: 'center' }}>Nuestros Valores</h2>
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
            <h2>Nuestro Equipo</h2>
            <p>Profesionales dedicados al cuidado de tu mascota.</p>
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
