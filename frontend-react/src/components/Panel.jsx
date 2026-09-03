// Antes: el bloque .panel se repetía en agendar, contacto e ingresar.
export default function Panel({ titulo, children }) {
  return (
    <div className="panel">
      {titulo && <h2>{titulo}</h2>}
      {children}
    </div>
  );
}
