// Pie de página.
// Rediseño: deja de ser una sola línea de copyright y pasa a cumplir
// su función real — dar los datos que alguien busca al final de la
// página: dónde quedamos, cuándo abrimos y cómo escribirnos.
import { Link } from 'react-router-dom';
import { CORREO, DIRECCION, HORARIO, WHATSAPP, enlaceWhatsApp } from '../data/negocio';

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div>
          <p className="footer-marca">El Hogar de Las Mascotas</p>
          <p>{DIRECCION}</p>
          <p style={{ marginTop: '0.5rem' }}>{HORARIO}</p>
        </div>

        <div>
          <h3>Atención</h3>
          <ul>
            <li><Link to="/servicios">Servicios</Link></li>
            <li><Link to="/agendar">Agendar cita</Link></li>
            <li><Link to="/productos">Productos</Link></li>
          </ul>
        </div>

        <div>
          <h3>Escríbenos</h3>
          <ul>
            <li>
              <a href={enlaceWhatsApp('Hola, quiero más información')} target="_blank" rel="noreferrer">
                WhatsApp {WHATSAPP}
              </a>
            </li>
            <li><a href={`mailto:${CORREO}`}>{CORREO}</a></li>
            <li><Link to="/contacto">Formulario de contacto</Link></li>
          </ul>
        </div>
      </div>

      <div className="footer-base">
        © 2026 Veterinaria El Hogar de Las Mascotas. Itagüí, Antioquia.
      </div>
    </footer>
  );
}
