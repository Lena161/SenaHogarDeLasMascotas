// Antes: .cita-msg / .form-msg / .auth-msg (tres copias del mismo patrón).
// Ahora: un componente único de mensajes de formulario.
export default function FormMessage({ mensaje }) {
  if (!mensaje) return null;
  return <div className={`form-msg ${mensaje.tipo}`}>{mensaje.texto}</div>;
}
