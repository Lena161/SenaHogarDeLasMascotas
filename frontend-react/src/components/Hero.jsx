// Encabezado de las páginas interiores.
// Rediseño: fondo verde profundo con un arco insinuado al fondo —el
// motivo de la marca— y el texto alineado a la izquierda, no centrado,
// para que la lectura empiece donde empieza el resto de la página.
export default function Hero({ titulo, subtitulo, compacto = false }) {
  return (
    <div className={`page-hero${compacto ? ' compact' : ''}`}>
      <div className="interior">
        <h1>{titulo}</h1>
        {subtitulo && <p>{subtitulo}</p>}
      </div>
    </div>
  );
}
