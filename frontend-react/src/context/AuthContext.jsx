// ============================================================
// Contexto de autenticación — VERSION CONMUTADA a JWT.
//
// Antes: la cookie de sesión viajaba sola; /api/me decía quién eras.
// Ahora: el login/registro retorna un token; se guarda en
// localStorage para sobrevivir recargas de página, y /auth/me
// lo valida al montar la aplicación para restaurar la sesión.
// ============================================================
import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);

  // Restaurar sesión al montar: si hay token guardado, /auth/me lo valida
  useEffect(() => {
    if (!localStorage.getItem('token')) {
      setCargando(false);
      return;
    }
    api.me()
      .then((u) => setUsuario(u))
      .catch(() => {
        localStorage.removeItem('token'); // token vencido o inválido
        setUsuario(null);
      })
      .finally(() => setCargando(false));
  }, []);

  function guardarSesion(respuesta) {
    // La respuesta del backend trae: { id, nombre, username, rol, token }
    localStorage.setItem('token', respuesta.token);
    setUsuario({ id: respuesta.id, nombre: respuesta.nombre, rol: respuesta.rol });
  }

  async function iniciarSesion(datos) {
    guardarSesion(await api.login(datos));
  }

  async function registrarse(datos) {
    guardarSesion(await api.registro(datos));
  }

  function cerrarSesion() {
    // JWT sin estado: basta con descartar el token
    localStorage.removeItem('token');
    setUsuario(null);
  }

  return (
    <AuthContext.Provider value={{ usuario, cargando, iniciarSesion, registrarse, cerrarSesion }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}