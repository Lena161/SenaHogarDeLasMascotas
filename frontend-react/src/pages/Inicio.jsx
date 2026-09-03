// Migración de index.html: hero con imagen, buscador, intro y
// tarjetas de servicios destacados. Contenidos conservados 1:1.
import { Link, useNavigate } from 'react-router-dom';
import { IconoConsulta, IconoSpa, IconoTienda, IconoVacuna, IconoBusqueda } from '../components/Iconos';
import { enlaceWhatsApp } from '../data/negocio';

const DESTACADOS = [
  { icono: <IconoConsulta />, titulo: 'Consultas Veterinarias', texto: 'Cuidado experto para la salud y el bienestar de tus mascotas.' },
  { icono: <IconoSpa />, titulo: 'Spa y Peluquería', texto: 'Servicios profesionales para que tu mascota luzca increíble.' },
  { icono: <IconoTienda />, titulo: 'Tienda Online', texto: 'Una amplia gama de productos de calidad para tu compañero.' },
  { icono: <IconoVacuna />, titulo: 'Cotizar Cirugías o Examenes de Laboratorio', texto: 'Solicita una cotización para cirugías o exámenes de laboratorio.' }
];

export default function Inicio() {
  const navegar = useNavigate();

  function manejarBusqueda(e) {
    // Decisión documentada en el plan de migración (§6.2): el buscador
    // era decorativo en el código base; ahora lleva a la página de
    // productos (cuando el catálogo venga de la API, se pasará el
    // término como filtro).
    e.preventDefault();
    navegar('/productos');
  }

  return (
    <main>
      <div className="hero-banner">
        <img src="/img/Imgjscam1.jpg" alt="Veterinaria El Hogar de Las Mascotas" />
        <div className="hero-overlay">
          <h1>Veterinaria El Hogar de Las Mascotas</h1>
          <p>Cuidamos a tus amigos peludos con amor y profesionalismo.</p>
          <Link to="/agendar" className="btn-hero">Agendar Cita</Link>
        </div>
      </div>

      <form role="search" className="search-bar" onSubmit={manejarBusqueda}>
        <IconoBusqueda />
        <input required type="text" placeholder="Buscar productos..." />
        <button type="submit">Buscar</button>
      </form>

      <div className="intro">
        <p>En Veterinaria El Hogar de Las Mascotas nos dedicamos a brindar el mejor cuidado para tus amigos peludos. Nuestro equipo de profesionales está comprometido con la salud y el bienestar de tus mascotas.</p>
        <p>Consultas de rutina, vacunaciones, tratamientos especializados y productos de alta calidad, todo en un solo lugar.</p>
      </div>

      <section className="services-section">
        <div className="section-header">
          <h2>Cuidado completo para tu mejor amigo</h2>
          <p>Productos, servicios veterinarios y spa. Todo lo que tu mascota necesita.</p>
        </div>

        <div className="cards-grid">
          {DESTACADOS.map((d) => (
            <article className="card" key={d.titulo}>
              <div className="icon-wrap">{d.icono}</div>
              <h3>{d.titulo}</h3>
              <p>{d.texto}</p>
            </article>
          ))}
        </div>

        <div style={{ textAlign: 'center' }}>
          <a href={enlaceWhatsApp('Hola me interesa saber más sobre sus servicios')} target="_blank" rel="noreferrer" className="whatsapp-btn">
            💬 Contactar por WhatsApp
          </a>
        </div>
      </section>
    </main>
  );
}
