// Antes: cinco héroes casi idénticos (services-hero, contacto-hero,
// nosotros-hero, productos-hero, agendar-hero), uno por página.
// Ahora: un componente parametrizado que conserva el mismo gradiente
// y tipografía de la versión original.
export default function Hero({ titulo, subtitulo, compacto = false }) {
  return (
    <div className={`page-hero${compacto ? ' compact' : ''}`}>
      <h1>{titulo}</h1>
      {subtitulo && <p>{subtitulo}</p>}
    </div>
  );
}
