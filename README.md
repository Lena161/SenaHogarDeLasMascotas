# Veterinaria El Hogar de Las Mascotas — Proyecto ADSO (SENA)

Sistema de gestión web y móvil para una veterinaria real, desarrollado
como proyecto formativo del programa Tecnología en Análisis y Desarrollo
de Software. El proyecto evoluciona en tres etapas, todas presentes en
este repositorio:

1. **Sitio informativo** (HTML/CSS/JS vanilla) → `legacy/frontend/`
2. **Prototipo dinámico** (Express + persistencia JSON) → `legacy/backend/`
3. **Sistema definitivo** (en curso):
   - Frontend SPA: React (Vite + React Router) → `frontend-react/`
   - API REST: Java 17 + Spring Boot 3.3 → `backend-spring/`
   - Base de datos: MySQL (script validado) → `database/vetcare_bd.sql`

La carpeta `legacy/` se conserva deliberadamente: es la evidencia del
código base desde el cual se sustenta la evolución ante los instructores.

## Estructura

```
docs/            Documentación de ingeniería (Fases 1-3, análisis de
                 migración y guía de integración)
database/        Script MySQL: esquema, triggers, vistas y datos semilla
legacy/          Código original (etapas 1 y 2), funcional e intacto
frontend-react/  SPA React migrada desde legacy/frontend
backend-spring/  API definitiva que reemplaza a legacy/backend
```

## Puesta en marcha rápida

Cada módulo tiene su propio README con instrucciones detalladas. Orden
recomendado de verificación (guía completa en
`docs/guia_integracion_monorepo.md`, sección 3):

```bash
# 1. Prototipo original (etapas 1-2)
cd legacy/backend && npm install && node server.js   # → :3000

# 2. Frontend React contra el prototipo
cd frontend-react && npm install && npm run dev      # → :5173

# 3. Base de datos definitiva
mysql -u root -p < database/vetcare_bd.sql

# 4. Backend definitivo
cd backend-spring && mvn spring-boot:run             # → :8080
```

## Próximas fases

Servicio PHP de disponibilidad (`servicio-php/`), conmutación del
frontend a la API definitiva, app móvil React Native (`app-movil/`),
historial clínico en MongoDB, Docker Compose y CI con GitHub Actions.

## Autor

Ana Milena Pájaro Paternina — Ficha 3134530 — SENA
