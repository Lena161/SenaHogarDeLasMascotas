// ============================================================
// Cliente HTTP centralizado — VERSION CONMUTADA a Spring Boot.
//
// Antes: rutas planas (/login, /citas) y sesion por cookie que el
// navegador enviaba solo.
// Ahora: prefijo /api/v1, y token JWT que ESTE archivo adjunta en
// el encabezado Authorization de cada peticion privada.
//
// El plan de migracion predijo que la conmutacion tocaria solo
// este archivo y el AuthContext (mas las paginas cuyo contrato
// cambio): esa prediccion es argumento de sustentacion.
// ============================================================

const BASE = '/api/v1'; // el proxy de Vite lo envia a :8080

function encabezados() {
  const cabeceras = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('token');
  if (token) {
    cabeceras['Authorization'] = `Bearer ${token}`; // JWT (RNF-03)
  }
  return cabeceras;
}

async function peticion(ruta, opciones = {}) {
  const res = await fetch(`${BASE}${ruta}`, {
    headers: encabezados(),
    ...opciones
  });
  // 401 = token ausente o vencido: se limpia la sesion local
  if (res.status === 401) {
    localStorage.removeItem('token');
  }
  const cuerpo = await res.json().catch(() => null);
  if (!res.ok) {
    const mensaje = (cuerpo && cuerpo.error) || 'Error de comunicación con el servidor.';
    throw new Error(mensaje);
  }
  return cuerpo;
}

export const api = {
  // --- Autenticación (el token llega en la respuesta) ---
  me: () => peticion('/auth/me'),
  login: (datos) => peticion('/auth/login', { method: 'POST', body: JSON.stringify(datos) }),
  registro: (datos) => peticion('/auth/registro', { method: 'POST', body: JSON.stringify(datos) }),
  // JWT es sin estado: cerrar sesión = descartar el token (no hay endpoint)

  // --- Catálogos públicos ---
  servicios: () => peticion('/servicios'),
  empleados: () => peticion('/empleados'),
  productos: () => peticion('/productos'),

  // --- Mascotas del cliente (RF-02) ---
  misMascotas: () => peticion('/mascotas'),
  crearMascota: (datos) => peticion('/mascotas', { method: 'POST', body: JSON.stringify(datos) }),

  // --- Citas ---
  misCitas: () => peticion('/citas/mias'),
  crearCita: (datos) => peticion('/citas', { method: 'POST', body: JSON.stringify(datos) }),
  // Evolución cumplida: cancelación LOGICA (RN-06), ya no DELETE
  cancelarCita: (id) => peticion(`/citas/${id}/cancelar`, { method: 'PATCH' }),

  // --- Contacto ---
  enviarContacto: (datos) => peticion('/contacto', { method: 'POST', body: JSON.stringify(datos) })
};
