// Migración de servicios.html: hero, intro, 9 tarjetas y CTA.
import { Link } from 'react-router-dom';
import Hero from '../components/Hero';
import ServiceCard from '../components/ServiceCard';
import { SERVICIOS_DETALLE } from '../data/serviciosDetalle';
import { enlaceWhatsApp } from '../data/negocio';

export default function Servicios() {
  return (
    <main>
      <Hero titulo="Nuestros Servicios"
            subtitulo="Cuidado profesional e integral para la salud y el bienestar de tu mascota." />

      <div className="intro" style={{ marginTop: '2.5rem' }}>
        <p>En <strong>Veterinaria El Hogar de Las Mascotas</strong> contamos con un equipo de profesionales comprometidos con la salud, el bienestar y la felicidad de tu mascota.</p>
        <p>Ofrecemos una atención cálida, responsable y de alta calidad, desde controles preventivos hasta procedimientos especializados. Aquí encontrarás todo lo que tu compañero necesita en un solo lugar.</p>
      </div>

      <div className="services-list">
        {SERVICIOS_DETALLE.map((s) => (
          <ServiceCard key={s.nombre} nombre={s.nombre}
                       descripcion={s.descripcion} icono={s.icono} />
        ))}
      </div>

      <div className="cta-section">
        <h2>¿Listo para agendar?</h2>
        <p>Reserva una cita o contáctanos directamente por WhatsApp.</p>
        <div className="cta-buttons">
          <Link to="/agendar" className="btn-solid">Agendar Cita</Link>
          <a href={enlaceWhatsApp('Hola me interesa saber más sobre sus servicios')} target="_blank" rel="noreferrer" className="whatsapp-btn">💬 WhatsApp</a>
        </div>
      </div>
    </main>
  );
}
