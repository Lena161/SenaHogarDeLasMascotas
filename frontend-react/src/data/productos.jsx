// ============================================================
// Catálogo de productos.
// Antes: arreglo PRODUCTOS quemado en productos.html.
// Ahora (v1 de la migración): mismo arreglo como módulo de datos,
// replicando el sitio 1:1. En cuanto exista el backend definitivo,
// esta constante se reemplaza por una llamada a
// GET /api/v1/productos (inventario real de MySQL, RF-11) y este
// archivo se elimina — cambio documentado en el plan de migración.
// ============================================================
import {
  IconoConsulta, IconoBano, IconoLaboratorio, IconoTienda
} from '../components/Iconos';

export const PRODUCTOS = [
  {
    id: 1, categoria: 'Alimento',
    nombre: 'Alimento Premium para Perro Adulto',
    desc: 'Bulto de 15kg, fórmula balanceada para razas medianas y grandes.',
    precio: '$85.000', icono: <IconoConsulta />
  },
  {
    id: 2, categoria: 'Alimento',
    nombre: 'Alimento Premium para Gato Adulto',
    desc: 'Bulto de 7.5kg, cuidado urinario y pelo brillante.',
    precio: '$78.000', icono: <IconoConsulta />
  },
  {
    id: 3, categoria: 'Accesorios',
    nombre: 'Correa y Collar Ajustable',
    desc: 'Resistente, cómoda y ajustable para perros de todos los tamaños.',
    precio: '$35.000', icono: <IconoTienda />
  },
  {
    id: 4, categoria: 'Accesorios',
    nombre: 'Cama Ortopédica para Mascotas',
    desc: 'Espuma de alta densidad, ideal para mascotas mayores o con artrosis.',
    precio: '$120.000', icono: <IconoTienda />
  },
  {
    id: 5, categoria: 'Higiene',
    nombre: 'Shampoo Antipulgas',
    desc: 'Fórmula suave, elimina pulgas y garrapatas cuidando la piel.',
    precio: '$28.000', icono: <IconoBano />
  },
  {
    id: 6, categoria: 'Higiene',
    nombre: 'Arena Sanitaria para Gatos',
    desc: 'Alta absorción y control de olores, bolsa de 10kg.',
    precio: '$32.000', icono: <IconoLaboratorio />
  },
  {
    id: 7, categoria: 'Juguetes',
    nombre: 'Dispensador Interactivo de Premios',
    desc: 'Estimula la mente de tu mascota mientras juega y come.',
    precio: '$45.000', icono: <IconoTienda />
  },
  {
    id: 8, categoria: 'Transporte',
    nombre: 'Transportadora para Mascotas',
    desc: 'Ventilada y segura, ideal para viajes y visitas al veterinario.',
    precio: '$95.000', icono: <IconoTienda />
  }
];
