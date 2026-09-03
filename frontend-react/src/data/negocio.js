// ============================================================
// Datos de negocio conservados TAL CUAL del código base.
// SE CONSERVA: teléfonos, correo, dirección, horario.
// ============================================================
export const WHATSAPP = '+573042083475';
export const CORREO = 'hogardelasmascotas2020@gmail.com';
export const DIRECCION = 'Carrera 50 #52-52, Barrio Los Naranjos, Itagüí - Antioquia';
export const HORARIO = 'Lunes a Sábado: 8:30 a.m. – 6:00 p.m.';

export function enlaceWhatsApp(mensaje) {
  return `whatsapp://send?phone=${WHATSAPP}&text=${encodeURIComponent(mensaje)}`;
}
