// Página de Servicios.
// Rediseño: los servicios se agrupan por tipo de atención. Cada grupo
// abre con un título y una nota que explica qué reúne, de modo que la
// estructura de la página transmita información y no solo orden visual.
import { Link } from 'react-router-dom';
import Hero from '../components/Hero';
import ServiceCard from '../components/ServiceCard';
import { IconoWhatsApp } from '../components/Iconos';
import { SERVICIOS_DETALLE, GRUPOS } from '../data/serviciosDetalle';
import { enlaceWhatsApp } from '../data/negocio';

export default function Servicios() {
  return (
    <main>
      <Hero
        titulo="Todo lo que tu mascota necesita, en un solo lugar"
        subtitulo="Desde el control de rutina hasta la cirugía programada y el día de spa. Te contamos qué hacemos en cada caso."
      />

      <div className="servicios-cuerpo">
        {GRUPOS.map((grupo) => {
          const servicios = SERVICIOS_DETALLE.filter((s) => s.grupo === grupo.id);
          return (
            <section className="grupo-servicio" key={grupo.id}>
              <div className="grupo-titulo">
                <h2>{grupo.titulo}</h2>
                <span>{grupo.nota}</span>
              </div>
              <div className="services-list">
                {servicios.map((s) => (
                  <ServiceCard
                    key={s.nombre}
                    nombre={s.nombre}
                    descripcion={s.descripcion}
                    icono={s.icono}
                  />
                ))}
              </div>
            </section>
          );
        })}
      </div>

      <div className="cta-section">
        <h2>¿Agendamos?</h2>
        <p>Reserva en línea y elige el horario que te quede mejor.</p>
        <div className="cta-buttons">
          <Link to="/agendar" className="btn-miel">Agendar cita</Link>
          <a
            href={enlaceWhatsApp('Hola, quiero saber más sobre sus servicios')}
            target="_blank"
            rel="noreferrer"
            className="whatsapp-btn"
          >
            <IconoWhatsApp /> Preguntar por WhatsApp
          </a>
        </div>
      </div>
    </main>
  );
}
