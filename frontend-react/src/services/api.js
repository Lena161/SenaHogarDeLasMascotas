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

/**
 * Arma la cadena de consulta omitiendo lo que venga vacio: el backend
 * aplica sus propios valores por defecto cuando el parametro no llega,
 * y enviar "fecha=undefined" produciria un 400.
 */
function parametros(objeto) {
  const query = new URLSearchParams();
  Object.entries(objeto).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') {
      query.append(clave, valor);
    }
  });
  return query.toString();
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
  enviarContacto: (datos) => peticion('/contacto', { method: 'POST', body: JSON.stringify(datos) }),

  // ==========================================================
  // Panel del personal
  // ==========================================================

  // --- Agenda y estados de cita (RF-07, RF-08) ---
  agendaDelDia: (fecha, estado) =>
    peticion(`/citas?${parametros({ fecha, estado })}`),
  miAgenda: (desde, hasta) =>
    peticion(`/citas/agenda?${parametros({ desde, hasta })}`),
  confirmarCita: (id) => peticion(`/citas/${id}/confirmar`, { method: 'PATCH' }),
  marcarInasistencia: (id) => peticion(`/citas/${id}/inasistencia`, { method: 'PATCH' }),
  // Si el servicio genera historia clinica, datos debe traer motivo
  // y diagnostico; si no (un bano), basta con llamar sin cuerpo.
  registrarAtencion: (id, datos) =>
    peticion(`/citas/${id}/atencion`, { method: 'POST', body: JSON.stringify(datos || {}) }),

  // --- Clientes y pacientes (RF-03) ---
  buscarClientes: (q) => peticion(`/duenos?${parametros({ q })}`),
  detalleCliente: (id) => peticion(`/duenos/${id}`),
  crearCliente: (datos) => peticion('/duenos', { method: 'POST', body: JSON.stringify(datos) }),
  actualizarCliente: (id, datos) =>
    peticion(`/duenos/${id}`, { method: 'PUT', body: JSON.stringify(datos) }),
  desactivarCliente: (id) => peticion(`/duenos/${id}`, { method: 'DELETE' }),

  mascotasDeCliente: (duenoId) => peticion(`/mascotas?${parametros({ duenoId })}`),
  // Busqueda de pacientes por nombre del paciente o de su dueno:
  // el profesional recuerda cualquiera de los dos.
  buscarPacientes: (q) => peticion(`/mascotas/buscar?${parametros({ q })}`),
  crearMascotaPara: (duenoId, datos) =>
    peticion(`/mascotas?${parametros({ duenoId })}`,
      { method: 'POST', body: JSON.stringify(datos) }),
  actualizarMascota: (id, datos) =>
    peticion(`/mascotas/${id}`, { method: 'PUT', body: JSON.stringify(datos) }),
  desactivarMascota: (id) => peticion(`/mascotas/${id}`, { method: 'DELETE' }),

  // --- Historia clinica (RF-09, RF-10) ---
  historialDeMascota: (id) => peticion(`/mascotas/${id}/historial`),
  citasDeMascota: (id) => peticion(`/mascotas/${id}/citas`),
  registrarHistorialDirecto: (id, datos) =>
    peticion(`/mascotas/${id}/historial`, { method: 'POST', body: JSON.stringify(datos) }),

  // --- Inventario (RF-11, RF-12, RF-13) ---
  productosTodos: () => peticion('/productos?todos=true'),
  productosPorReponer: () => peticion('/productos/por-reponer'),
  crearProducto: (datos) => peticion('/productos', { method: 'POST', body: JSON.stringify(datos) }),
  actualizarProducto: (id, datos) =>
    peticion(`/productos/${id}`, { method: 'PUT', body: JSON.stringify(datos) }),
  descontinuarProducto: (id) => peticion(`/productos/${id}`, { method: 'DELETE' }),
  reactivarProducto: (id) => peticion(`/productos/${id}/reactivar`, { method: 'PATCH' }),
  movimientosDeProducto: (id) => peticion(`/productos/${id}/movimientos`),
  registrarMovimiento: (datos) =>
    peticion('/productos/movimientos', { method: 'POST', body: JSON.stringify(datos) }),

  // --- Catalogo de servicios (RF-04) ---
  serviciosTodos: () => peticion('/servicios?todos=true'),
  crearServicio: (datos) => peticion('/servicios', { method: 'POST', body: JSON.stringify(datos) }),
  actualizarServicio: (id, datos) =>
    peticion(`/servicios/${id}`, { method: 'PUT', body: JSON.stringify(datos) }),
  desactivarServicio: (id) => peticion(`/servicios/${id}`, { method: 'DELETE' }),
  reactivarServicio: (id) => peticion(`/servicios/${id}/reactivar`, { method: 'PATCH' })
};
