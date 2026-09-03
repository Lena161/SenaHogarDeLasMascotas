// ============================================================
// Antes: agendar.html llamaba verificarSesion() y redirigía
// manualmente con window.location. Ahora: componente que
// envuelve cualquier ruta privada.
// ============================================================
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RutaProtegida({ children }) {
  const { usuario, cargando } = useAuth();
  const ubicacion = useLocation();

  if (cargando) return null; // aún verificando la sesión

  if (!usuario) {
    // Se conserva el patrón ?redirect= del código base
    return <Navigate to={`/ingresar?redirect=${ubicacion.pathname}`} replace />;
  }
  return children;
}
