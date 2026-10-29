// ============================================================
// Catálogo de servicios de la página Servicios.
//
// Rediseño: cada servicio declara ahora a qué GRUPO de atención
// pertenece. La página deja de ser una grilla de nueve tarjetas
// iguales y pasa a mostrar cómo trabaja realmente la clínica:
// atención del día a día, procedimientos, y estética.
// El agrupamiento refleja la distinción CONSULTA / SPA del modelo
// de datos de la Fase 2.
// ============================================================
import {
  IconoConsulta, IconoVacuna, IconoDesparasitacion, IconoUrgencia,
  IconoBano, IconoSpa, IconoCirugia, IconoDental, IconoLaboratorio
} from '../components/Iconos';

export const GRUPOS = [
  { id: 'clinica', titulo: 'Atención clínica', nota: 'Lo que tu mascota necesita de forma regular' },
  { id: 'procedimientos', titulo: 'Diagnóstico y cirugía', nota: 'Procedimientos con preparación y seguimiento' },
  { id: 'spa', titulo: 'Estética y spa', nota: 'Cuidado de piel, pelaje y uñas' }
];

export const SERVICIOS_DETALLE = [
  {
    grupo: 'clinica',
    nombre: 'Consulta Veterinaria',
    descripcion: 'Evaluación completa del estado de salud de tu mascota a cargo de nuestros veterinarios certificados. Diagnóstico, seguimiento y recomendaciones personalizadas.',
    icono: <IconoConsulta />
  },
  {
    grupo: 'clinica',
    nombre: 'Vacunación',
    descripcion: 'Planes de vacunación preventivos para perros y gatos. Protegemos a tu mascota contra enfermedades infecciosas con los biológicos de mayor calidad.',
    icono: <IconoVacuna />
  },
  {
    grupo: 'clinica',
    nombre: 'Desparasitación',
    descripcion: 'Tratamiento interno y externo contra parásitos como pulgas, garrapatas y helmintos. Mantenemos a tu mascota libre de parasitosis para una vida más saludable.',
    icono: <IconoDesparasitacion />
  },
  {
    grupo: 'clinica',
    nombre: 'Atención de Urgencias No Vitales',
    descripcion: 'Atendemos heridas leves, irritaciones, vómito ocasional, cojeras y otros problemas que requieren atención pronta pero no representan riesgo inmediato de vida.',
    icono: <IconoUrgencia />
  },
  {
    grupo: 'procedimientos',
    nombre: 'Exámenes de Laboratorio',
    descripcion: 'Hemogramas, uroanálisis, coprologías, química sanguínea y más. Resultados precisos y rápidos para apoyar el diagnóstico y seguimiento clínico de tu mascota.',
    icono: <IconoLaboratorio />
  },
  {
    grupo: 'procedimientos',
    nombre: 'Cirugías de Esterilización',
    descripcion: 'Realizamos procedimientos de castración y esterilización para caninos y felinos con anestesia segura, monitoreo constante y recuperación acompañada.',
    icono: <IconoCirugia />
  },
  {
    grupo: 'procedimientos',
    nombre: 'Profilaxis Dental',
    descripcion: 'Limpieza dental profesional con ultrasonido para eliminar el sarro y la placa bacteriana. Prevenimos enfermedades periodontales y mejoramos la salud bucal de tu mascota.',
    icono: <IconoDental />
  },
  {
    grupo: 'spa',
    nombre: 'Baños Medicados',
    descripcion: 'Tratamientos dermatológicos con champús y productos terapéuticos para el control de hongos, bacterias, sarna y otras afecciones cutáneas de tu mascota.',
    icono: <IconoBano />
  },
  {
    grupo: 'spa',
    nombre: 'Spa y Peluquería',
    descripcion: 'Baño, secado, corte de pelo, limpieza de oídos, corte de uñas y más. Dejamos a tu mascota limpia, cómoda y con una apariencia impecable.',
    icono: <IconoSpa />
  }
];
