// Tarjeta de servicio (página Servicios).
// Antes: 9 bloques .service-card escritos a mano en servicios.html.
export default function ServiceCard({ nombre, descripcion, icono }) {
  return (
    <div className="service-card">
      <div className="icon-wrap">{icono}</div>
      <h2>{nombre}</h2>
      <p>{descripcion}</p>
    </div>
  );
}
