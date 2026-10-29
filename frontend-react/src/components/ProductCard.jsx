// Tarjeta de producto.
// Rediseño: el precio pasa a la tipografía de títulos para que sea el
// dato que el ojo encuentra primero, y el botón de WhatsApp usa el
// ícono de la marca en lugar de un emoji.
import { IconoWhatsApp } from './Iconos';
import { enlaceWhatsApp } from '../data/negocio';

export default function ProductCard({ categoria, nombre, desc, precio, icono }) {
  return (
    <article className="producto-card">
      <div className="icon-wrap">{icono}</div>
      <span className="categoria">{categoria}</span>
      <h3>{nombre}</h3>
      <p className="desc">{desc}</p>
      <div className="precio">{precio}</div>
      <a
        className="whatsapp-btn"
        href={enlaceWhatsApp(`Hola, me interesa el producto: ${nombre}`)}
        target="_blank"
        rel="noreferrer"
      >
        <IconoWhatsApp /> Consultar
      </a>
    </article>
  );
}
