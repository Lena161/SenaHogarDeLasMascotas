// Tarjeta de servicio.
// Rediseño: el ícono deja de ir en un círculo con degradado y pasa a
// una almohadilla sobria; la tarjeta se distingue por borde, no por
// sombra, y al pasar el cursor cambia el borde en lugar de levantarse.
export default function ServiceCard({ nombre, descripcion, icono }) {
  return (
    <article className="service-card">
      <div className="icon-wrap">{icono}</div>
      <h2>{nombre}</h2>
      <p>{descripcion}</p>
    </article>
  );
}
