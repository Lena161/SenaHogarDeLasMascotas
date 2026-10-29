// Portada del sitio.
// Rediseño: en vez de una foto a todo lo ancho con el texto encima
// (el patrón que traía el código base), la portada se abre en dos
// columnas: el mensaje y las acciones a la izquierda, la foto a la
// derecha recortada en ARCO —una puerta abierta—, que es el motivo
// visual tomado del propio nombre de la marca: "El Hogar".
import { Link, useNavigate } from 'react-router-dom';
import {
  IconoConsulta, IconoSpa, IconoTienda, IconoVacuna, IconoBusqueda, IconoWhatsApp
} from '../components/Iconos';
import { DIRECCION, HORARIO, enlaceWhatsApp } from '../data/negocio';

const DESTACADOS = [
  {
    icono: <IconoConsulta />,
    titulo: 'Consulta veterinaria',
    texto: 'Valoración, diagnóstico y seguimiento con veterinarios certificados.',
    a: '/servicios'
  },
  {
    icono: <IconoVacuna />,
    titulo: 'Vacunación y prevención',
    texto: 'Planes de vacunación y desparasitación al día para perros y gatos.',
    a: '/servicios'
  },
  {
    icono: <IconoSpa />,
    titulo: 'Spa y peluquería',
    texto: 'Baño, corte, limpieza de oídos y uñas. Tu mascota sale como nueva.',
    a: '/servicios'
  },
  {
    icono: <IconoTienda />,
    titulo: 'Tienda',
    texto: 'Alimento, accesorios e higiene con el precio y el stock del día.',
    a: '/productos'
  }
];

export default function Inicio() {
  const navegar = useNavigate();

  function manejarBusqueda(e) {
    e.preventDefault();
    navegar('/productos');
  }

  return (
    <main>
      {/* ── PORTADA ── */}
      <section className="portada">
        <div className="portada-texto">
          <h1>Tu mascota ya tiene un hogar aquí</h1>
          <p className="entrada">
            Clínica veterinaria y spa en Itagüí. Atendemos consulta, vacunación,
            cirugía y estética, y puedes reservar tu cita en línea en menos de un minuto.
          </p>

          <div className="portada-acciones">
            <Link to="/agendar" className="btn-miel">Agendar cita</Link>
            <Link to="/servicios" className="btn-linea">Ver servicios</Link>
          </div>

          <dl className="portada-datos">
            <div>
              <dt>Horario</dt>
              <dd>{HORARIO}</dd>
            </div>
            <div>
              <dt>Dónde estamos</dt>
              <dd>{DIRECCION}</dd>
            </div>
          </dl>
        </div>

        <div className="portada-arco">
          <img src="/img/Imgjscam1.jpg" alt="Cachorro corriendo por un sendero" />
        </div>
      </section>

      {/* ── CÓMO TE AYUDAMOS ── */}
      <section className="services-section">
        <div className="section-header">
          <h2>Cuidado completo para tu mejor amigo</h2>
          <p>
            Lo que atendemos todos los días, de la consulta de rutina al día de spa.
          </p>
        </div>

        <div className="cards-grid">
          {DESTACADOS.map((d) => (
            <Link to={d.a} style={{ textDecoration: 'none' }} key={d.titulo}>
              <article className="card">
                <div className="icon-wrap">{d.icono}</div>
                <h3>{d.titulo}</h3>
                <p>{d.texto}</p>
              </article>
            </Link>
          ))}
        </div>

        <form role="search" className="search-bar" onSubmit={manejarBusqueda} style={{ marginTop: '2.5rem' }}>
          <IconoBusqueda />
          <input required type="text" placeholder="Buscar en la tienda: alimento, antipulgas…" />
          <button type="submit">Buscar</button>
        </form>
      </section>

      {/* ── CITA ── */}
      <section className="cta-section">
        <h2>Reserva en línea, sin llamadas</h2>
        <p>Eliges servicio, profesional y horario. Te confirmamos la cita el mismo día.</p>
        <div className="cta-buttons">
          <Link to="/agendar" className="btn-miel">Agendar cita</Link>
          <a
            href={enlaceWhatsApp('Hola, quiero saber más sobre sus servicios')}
            target="_blank"
            rel="noreferrer"
            className="whatsapp-btn"
          >
            <IconoWhatsApp /> Escribir por WhatsApp
          </a>
        </div>
      </section>
    </main>
  );
}
