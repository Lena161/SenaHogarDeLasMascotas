// Tarjeta de producto.
// Antes: template string con innerHTML en productos.html.
// Nota de sustentación: la función escapeHtml() desapareció porque
// JSX escapa los valores automáticamente (mejora de seguridad
// heredada del framework).
import { enlaceWhatsApp } from '../data/negocio';

export default function ProductCard({ categoria, nombre, desc, precio, icono }) {
  return (
    <article className="producto-card">
      <div className="icon-wrap">{icono}</div>
      <span className="categoria">{categoria}</span>
      <h3>{nombre}</h3>
      <p className="desc">{desc}</p>
      <div className="precio">{precio}</div>
      <a className="whatsapp-btn" href={enlaceWhatsApp(`Hola, me interesa el producto: ${nombre}`)} target="_blank" rel="noreferrer">
        💬 Consultar
      </a>
    </article>
  );
}
