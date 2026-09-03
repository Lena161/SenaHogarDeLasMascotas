// ============================================================
// Catálogo de servicios de la página Servicios.
// Antes: 9 tarjetas escritas a mano en servicios.html.
// Ahora: datos + un componente ServiceCard. En la versión
// definitiva estos datos vendrán de GET /api/v1/servicios
// (con tipo, duración y precio del modelo de la Fase 2).
// ============================================================
import {
  IconoConsulta, IconoVacuna, IconoDesparasitacion, IconoUrgencia,
  IconoBano, IconoSpa, IconoCirugia, IconoDental, IconoLaboratorio
} from '../components/Iconos';

export const SERVICIOS_DETALLE = [
  {
    nombre: 'Consulta Veterinaria',
    descripcion: 'Evaluación completa del estado de salud de tu mascota a cargo de nuestros veterinarios certificados. Diagnóstico, seguimiento y recomendaciones personalizadas.',
    icono: <IconoConsulta />
  },
  {
    nombre: 'Vacunación',
    descripcion: 'Planes de vacunación preventivos para perros y gatos. Protegemos a tu mascota contra enfermedades infecciosas con los biológicos de mayor calidad.',
    icono: <IconoVacuna />
  },
  {
    nombre: 'Desparasitación',
    descripcion: 'Tratamiento interno y externo contra parásitos como pulgas, garrapatas y helmintos. Mantenemos a tu mascota libre de parasitosis para una vida más saludable.',
    icono: <IconoDesparasitacion />
  },
  {
    nombre: 'Atención de Urgencias No Vitales',
    descripcion: 'Atendemos heridas leves, irritaciones, vómito ocasional, cojeras y otros problemas que requieren atención pronta pero no representan riesgo inmediato de vida.',
    icono: <IconoUrgencia />
  },
  {
    nombre: 'Baños Medicados',
    descripcion: 'Tratamientos dermatológicos con champús y productos terapéuticos para el control de hongos, bacterias, sarna y otras afecciones cutáneas de tu mascota.',
    icono: <IconoBano />
  },
  {
    nombre: 'Spa y Peluquería',
    descripcion: 'Baño, secado, corte de pelo, limpieza de oídos, corte de uñas y más. Dejamos a tu mascota limpia, cómoda y con una apariencia impecable.',
    icono: <IconoSpa />
  },
  {
    nombre: 'Cirugías de Esterilización',
    descripcion: 'Realizamos procedimientos de castración y esterilización para caninos y felinos con anestesia segura, monitoreo constante y recuperación acompañada.',
    icono: <IconoCirugia />
  },
  {
    nombre: 'Profilaxis Dental',
    descripcion: 'Limpieza dental profesional con ultrasonido para eliminar el sarro y la placa bacteriana. Prevenimos enfermedades periodontales y mejoramos la salud bucal de tu mascota.',
    icono: <IconoDental />
  },
  {
    nombre: 'Exámenes de Laboratorio',
    descripcion: 'Hemogramas, uroanálisis, coprologías, química sanguínea y más. Resultados precisos y rápidos para apoyar el diagnóstico y seguimiento clínico de tu mascota.',
    icono: <IconoLaboratorio />
  }
];
