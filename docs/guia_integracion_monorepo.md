# GUÍA DE INTEGRACIÓN — UNIFICAR TODO EL PROYECTO EN UN REPOSITORIO
## Veterinaria "El Hogar de Las Mascotas" — ADSO SENA

**Objetivo:** consolidar el código original, el prototipo Express, los
entregables de las Fases 1–3 y los proyectos React y Spring Boot en un
único repositorio Git cuya propia estructura narre la evolución del
proyecto — el argumento central de tu sustentación.

---

## 1. ESTRUCTURA FINAL DEL REPOSITORIO

```
SenaHogarDeLasMascotas/
│
├── README.md                  ← portada del proyecto (plantilla en §6)
│
├── docs/                      ← toda la documentación de sustentación
│   ├── fase1_requisitos_veterinaria.md
│   ├── fase2_modelo_er_casos_uso.md
│   ├── fase3_backend_springboot.md
│   └── analisis_codigo_base_migracion_react.md
│
├── database/
│   └── vetcare_bd.sql         ← script MySQL validado (Fase 2)
│
├── legacy/                    ← TU CÓDIGO ORIGINAL, intacto (Etapas 1 y 2)
│   ├── frontend/              ← los 7 .html, style.css, script.js, Imgjscam1.jpg
│   └── backend/               ← server.js, db.js, package.json (prototipo Express)
│
├── frontend-react/            ← SPA React (Etapa 3, en curso)
│
└── backend-spring/            ← API Spring Boot (Etapa 3, en curso)
```

**Por qué `legacy/` y no borrar:** ante los instructores, esa carpeta ES
la prueba física de que el proyecto evolucionó desde un código base real.
Cada documento de `docs/` referencia archivos que están ahí. Borrarla
sería borrar tu mejor argumento.

**Ojo con el prototipo:** `server.js` sirve el frontend desde
`path.join(__dirname, '..', 'frontend')` — la estructura `legacy/backend`
y `legacy/frontend` respeta exactamente esa ruta relativa, así que el
prototipo sigue funcionando sin tocar una línea.

---

## 2. PASO A PASO EN TU MÁQUINA (Windows)

### Paso 0 — Respaldo y control de versiones

```bash
# Copia de seguridad simple antes de mover nada
# (copiar la carpeta SenaHogarDeLasMascotas completa a otro lugar)

cd C:\Users\USUARIO\Desktop\SenaHogarDeLasMascotas
git init                      # si aún no es un repositorio
git add .
git commit -m "docs: estado original del proyecto (etapas 1 y 2)"
git tag etapa-2-prototipo     # marca el punto de partida
```

Ese primer commit congela tu código base tal como estaba: desde aquí,
todo cambio queda trazado.

### Paso 1 — Reorganizar en la estructura del monorepo

```bash
mkdir legacy legacy\frontend legacy\backend docs database

# Mover el sitio original
move *.html legacy\frontend\
move style.css legacy\frontend\
move script.js legacy\frontend\
move Imgjscam1.jpg legacy\frontend\

# Mover el prototipo Express (incluye su package-lock si existe)
move server.js legacy\backend\
move db.js legacy\backend\
move package.json legacy\backend\
move package-lock.json legacy\backend\
# si existe la carpeta data\ del prototipo, tambien:
move data legacy\backend\data
```

### Paso 2 — Incorporar los entregables de las fases

Copia los archivos que te he entregado:

- Los 4 documentos `.md` → `docs/`
- `vetcare_bd.sql` → `database/`
- Descomprime `frontend-react.zip` → queda la carpeta `frontend-react/`
- Descomprime `backend-spring.zip` → queda la carpeta `backend-spring/`

### Paso 3 — `.gitignore` en la raíz

```gitignore
# Dependencias y compilados
node_modules/
dist/
target/

# Datos locales del prototipo
legacy/backend/data/db.json

# Entorno
.env
*.log
```

### Paso 4 — Commit de la integración

```bash
git add .
git commit -m "refactor: estructura monorepo (legacy + docs + react + spring)"
git tag etapa-3-integracion
```

---

## 3. ORDEN DE PUESTA EN MARCHA (verificar que todo corre)

Ejecuta estas verificaciones en orden; cada una es independiente y te
dice exactamente qué pieza funciona:

**3.1 El prototipo sigue vivo (nada se rompió al mover):**
```bash
cd legacy\backend
npm install
node server.js          → http://localhost:3000 muestra el sitio original
```

**3.2 React contra el prototipo (integración ya validada):**
```bash
# Terminal A: el prototipo corriendo (paso 3.1)
# Terminal B:
cd frontend-react
npm install
npm run dev             → http://localhost:5173
# Registrarse, agendar y cancelar una cita debe funcionar completo
```

**3.3 La base de datos definitiva:**
```bash
mysql -u root -p < database\vetcare_bd.sql
# Verificar: USE vetcare_db; SELECT * FROM v_productos_stock_bajo;
```

**3.4 El backend Spring Boot (primera compilación):**
```bash
cd backend-spring
mvn spring-boot:run     → http://localhost:8080
# Probar el registro con el curl del README del backend
# Si aparece un error de compilación, tráelo y lo corregimos
```

**3.5 Conmutación React → Spring Boot** (solo cuando 3.4 funcione):
requiere tres cambios en `frontend-react/`, todos concentrados donde el
plan de migración lo previó:

1. `vite.config.js`: proxy `'/api': 'http://localhost:8080'`
2. `src/services/api.js`: rutas al prefijo `/api/v1` (según la tabla de
   mapeo del documento de análisis §8) y encabezado
   `Authorization: Bearer <token>` en las peticiones autenticadas
3. `src/context/AuthContext.jsx`: guardar el token que retorna
   login/registro y pasarlo al cliente HTTP

Esta conmutación es una mini-fase en sí misma (incluye la nueva página
"Mis mascotas", porque la cita definitiva exige `mascotaId`): pídemela
cuando tu Spring Boot arranque y la generamos completa sobre tu código.

---

## 4. FLUJO DE TRABAJO GIT DE AQUÍ EN ADELANTE

- Rama `main`: siempre estable (lo que mostrarías hoy a un instructor).
- Rama `develop`: integración del trabajo en curso.
- Ramas por funcionalidad: `feature/conmutacion-api`, `feature/php-disponibilidad`,
  `feature/app-movil`, etc. → se fusionan a `develop` y de ahí a `main`.
- Un tag por hito: `etapa-2-prototipo`, `etapa-3-integracion`,
  `fase-4-php`, `fase-5-conmutacion`… Los tags te permiten mostrar en la
  sustentación el estado exacto del proyecto en cada momento
  (`git checkout <tag>`).
- Commits en español con prefijo (`feat:`, `fix:`, `docs:`, `refactor:`),
  como se definió en la Fase 1 §6.3.

---

## 5. CÓMO ENCAJA CADA PIEZA (mapa mental para la sustentación)

```
legacy/frontend  ──migración documentada──▶  frontend-react
     (docs/analisis_codigo_base_migracion_react.md)

legacy/backend   ──reemplazo justificado──▶  backend-spring
     (contratos conservados; limitaciones resueltas: §4.3 del análisis)

docs/fase1  ─▶ requisitos que ambos implementan (RF-xx, RN-xx)
docs/fase2  ─▶ database/vetcare_bd.sql + entidades JPA de backend-spring
docs/fase3  ─▶ mapa de evidencias POO y patrones de backend-spring
```

Pendientes que se sumarán como carpetas hermanas cuando lleguen sus
fases: `servicio-php/` (Fase 4), `app-movil/` (Fase 6), `docker/` con el
`docker-compose.yml` en la raíz (Fase 8) y `.github/workflows/` (CI).
La estructura ya los espera sin reorganizar nada.

---

## 6. PLANTILLA DEL README.md RAÍZ

```markdown
# Veterinaria El Hogar de Las Mascotas — Proyecto ADSO (SENA)

Sistema de gestión web y móvil para una veterinaria real, desarrollado
como proyecto formativo. Evolución en tres etapas:

1. **Sitio informativo** (HTML/CSS/JS) → `legacy/frontend`
2. **Prototipo dinámico** (Express + JSON) → `legacy/backend`
3. **Sistema definitivo** (en curso):
   - Frontend SPA: React → `frontend-react/`
   - API REST: Java 17 + Spring Boot 3.3 → `backend-spring/`
   - Base de datos: MySQL → `database/vetcare_bd.sql`

## Documentación
Toda la ingeniería del proyecto está en `docs/`: requisitos (Fase 1),
modelo de datos y casos de uso (Fase 2), backend y patrones (Fase 3) y
el análisis de migración del código base.

## Puesta en marcha
Ver el README de cada módulo (`frontend-react/README.md`,
`backend-spring/README.md`).

## Autor
[Nombre] — Ficha [número] — Tecnología en Análisis y Desarrollo de Software
```

---

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Agosto 2026 | Guía de integración del monorepo |
