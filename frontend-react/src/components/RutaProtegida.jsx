// ============================================================
// Antes: agendar.html llamaba verificarSesion() y redirigía
// manualmente con window.location. Ahora: componente que
// envuelve cualquier ruta privada.
//
// La prop `roles` restringe además por perfil (RF-14). Es una
// comodidad de navegación, no un control de seguridad: quien
// decide de verdad es el backend con @PreAuthorize, porque el
// token puede usarse sin pasar por esta interfaz.
// ============================================================
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RutaProtegida({ children, roles }) {
  const { usuario, cargando } = useAuth();
  const ubicacion = useLocation();

  if (cargando) return null; // aún verificando la sesión

  if (!usuario) {
    // Se conserva el patrón ?redirect= del código base
    return <Navigate to={`/ingresar?redirect=${ubicacion.pathname}`} replace />;
  }

  if (roles && !roles.includes(usuario.rol)) {
    // Sesión válida pero perfil equivocado: se devuelve al inicio
    // en vez de mostrar un error, que no aportaría nada al usuario.
    return <Navigate to="/" replace />;
  }

  return children;
}
