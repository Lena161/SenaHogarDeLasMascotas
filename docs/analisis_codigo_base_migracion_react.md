# ANÁLISIS DEL CÓDIGO BASE Y PLAN DE MIGRACIÓN A REACT
## Veterinaria "El Hogar de Las Mascotas" — Evolución del código existente

**Programa:** Tecnología en Análisis y Desarrollo de Software (ADSO) — SENA
**Fase:** Preparación de la migración frontend (Fase 5 del cronograma)
**Insumo:** Código base entregado por el aprendiz (7 páginas HTML, CSS, JS y prototipo backend Node.js)
**Fecha:** Agosto de 2026
**Versión:** 1.0

---

## TABLA DE CONTENIDO

1. Inventario del código base
2. Hallazgo clave: el proyecto ya tuvo una primera evolución
3. Análisis técnico del frontend existente
4. Análisis técnico del backend prototipo (Node.js/Express)
5. Brechas frente al modelo diseñado en Fases 1 y 2
6. Plan de migración: mapeo página → componente React
7. Qué se conserva, qué se refactoriza y por qué
8. Mapeo de endpoints: prototipo → API definitiva
9. Riesgos de la migración y orden de trabajo recomendado

---

# 1. INVENTARIO DEL CÓDIGO BASE

| Archivo | Tipo | Rol en el sistema actual |
|---------|------|--------------------------|
| `index.html` | Página | Inicio: hero con imagen, buscador (decorativo), intro, 4 tarjetas de servicios destacados, CTA WhatsApp |
| `servicios.html` | Página | Catálogo de 9 servicios en tarjetas estáticas + sección CTA |
| `productos.html` | Página | Catálogo de 8 productos **renderizado dinámicamente por JS** desde un arreglo local; consulta por WhatsApp |
| `nosotros.html` | Página | Historia, misión, visión, valores y equipo |
| `contacto.html` | Página | Información de contacto + formulario que consume `POST /api/contacto` |
| `ingresar.html` | Página | Login y registro con pestañas; consume `/api/login` y `/api/register` |
| `agendar.html` | Página | **Ruta protegida**: verifica sesión y redirige a ingresar; formulario de cita + listado "Mis citas" con cancelación |
| `script.js` | JS global | Actualiza el header según la sesión (`/api/me`), maneja logout |
| `style.css` | Estilos | Sistema de diseño con variables CSS (paleta, sombras, radios), header, hero, cards, botones, footer |
| `server.js` | Backend | API Express: autenticación con sesiones, citas, servicios, contacto |
| `db.js` | Backend | Persistencia en archivo `data/db.json` con IDs autoincrementales |
| `package.json` | Config | Dependencias: express, express-session, bcryptjs |
| `Imgjscam1.jpg` | Recurso | Imagen del hero de inicio |

(`settings_local.json` y `package-lock.json` son archivos de entorno/lock sin
impacto en el análisis funcional.)

---

# 2. HALLAZGO CLAVE: EL PROYECTO YA TUVO UNA PRIMERA EVOLUCIÓN

El código entregado **no es un sitio puramente estático**: ya incorpora un
backend prototipo en Node.js/Express con autenticación real (hash BCrypt,
sesiones de 8 horas), una API JSON y persistencia en archivo. Esto
reconstruye la línea evolutiva completa del proyecto, que es exactamente lo
que los instructores piden sustentar:

```
Etapa 1 (origen)        Etapa 2 (código base actual)      Etapa 3 (este proyecto)
─────────────────       ──────────────────────────────    ─────────────────────────────
Sitio HTML/CSS/JS   →   + Backend Express prototipo   →   Frontend React (SPA)
informativo             + Sesiones y BCrypt               + API Spring Boot (MySQL)
                        + Citas básicas en db.json        + Servicio PHP disponibilidad
                        + Formulario de contacto          + MongoDB historial clínico
                                                          + App móvil React Native
```

**Cómo sustentarlo:** la Etapa 2 demuestra que el aprendiz ya identificó la
necesidad de dinamizar el sitio y validó el flujo cliente-servidor
(registro → login → agendar → cancelar). La Etapa 3 no desecha ese
aprendizaje: **conserva los contratos de la API y los flujos de usuario ya
probados**, y reemplaza la implementación por el stack definitivo del
proyecto (Spring Boot + MySQL) que resuelve las limitaciones del prototipo
(sección 4.3). El prototipo Express queda documentado como artefacto de la
evolución, no como código muerto.

---

# 3. ANÁLISIS TÉCNICO DEL FRONTEND EXISTENTE

## 3.1 Fortalezas a conservar

**a) Sistema de diseño con variables CSS.** `style.css` define tokens
(`--primary: #b5446e`, `--shadow`, `--radius`, etc.) y componentes visuales
coherentes (header, cards, botones, hero). Esto es oro para la migración:
las variables se trasladan **sin cambios** al CSS global de React y toda la
identidad visual se preserva. La marca no cambia; cambia la arquitectura.

**b) Renderizado dinámico ya presente.** `productos.html` genera sus
tarjetas con `PRODUCTOS.map(...)` sobre un arreglo de objetos, y
`agendar.html` renderiza "Mis citas" desde la API. Es decir, **el código
base ya piensa en datos → plantilla**, que es el modelo mental de React. La
migración de estas páginas es casi una traducción directa de template
strings a JSX.

**c) Buenas prácticas de seguridad frontend.** El código usa una función
`escapeHtml()` para prevenir inyección de HTML al interpolar datos. Punto
fuerte de sustentación: **React hace este escape automáticamente en JSX**,
por lo que la migración elimina la necesidad de esa función manual y reduce
la superficie de error (se documenta como mejora obtenida por el cambio de
tecnología).

**d) Protección de rutas ya resuelta conceptualmente.** `agendar.html`
verifica la sesión (`/api/me`) y redirige a `ingresar.html?redirect=...` si
no hay usuario. Ese patrón se convierte en un componente `<RutaProtegida>`
con React Router — misma lógica, mejor encapsulada.

**e) Estado de sesión compartido.** `script.js` actualiza el header en
todas las páginas según la sesión. En React esto se centraliza en un
`AuthContext`: el problema ya estaba identificado y resuelto de forma
artesanal; la migración lo formaliza.

## 3.2 Debilidades que la migración corrige

| Problema detectado | Evidencia | Cómo lo corrige la migración |
|--------------------|-----------|------------------------------|
| Header, footer y estilos de formularios **duplicados en 7 archivos** | Cada HTML repite el `<header>` completo; los estilos de `.panel`, `.form-*` se repiten en los `<style>` de 4 páginas | Componentes `<Header/>`, `<Footer/>`, `<Panel/>` y CSS compartido: un solo punto de cambio |
| Buscador de inicio **no funcional** | `<form role="search">` sin handler de submit | Se conecta al catálogo real de productos de la API (o se elimina justificadamente en la v1) |
| CSS embebido en `<style>` por página | `agendar.html`, `contacto.html`, `ingresar.html`, `nosotros.html`, `productos.html`, `servicios.html` | Estilos por componente (un archivo CSS por componente o CSS Modules) |
| Productos **quemados en el JS del cliente** | Arreglo `PRODUCTOS` en `productos.html` | Se consumen de `GET /api/v1/productos` (inventario real de MySQL, RF-11) |
| Manipulación manual del DOM y re-render completo | `citasList.innerHTML = citas.map(...)`, listeners re-adjuntados tras cada render | Estado declarativo con `useState`/`useEffect`: React re-renderiza solo lo necesario |
| Navegación con recarga completa de página | Enlaces `<a href="*.html">` | React Router (SPA): navegación instantánea manteniendo el estado de sesión en memoria |

---

# 4. ANÁLISIS TÉCNICO DEL BACKEND PROTOTIPO (Node.js/Express)

## 4.1 Qué implementa hoy

| Endpoint | Método | Función | Observación |
|----------|--------|---------|-------------|
| `/api/register` | POST | Crea usuario con BCrypt (salt 10) e inicia sesión | Valida campos y contraseña ≥ 6 |
| `/api/login` | POST | Autentica y crea sesión de 8 h | Mensaje de error genérico (buena práctica) |
| `/api/logout` | POST | Destruye la sesión | — |
| `/api/me` | GET | Retorna el usuario en sesión | Usado por el header dinámico |
| `/api/servicios` | GET | Lista de 9 servicios | **Arreglo quemado en el código** |
| `/api/citas` | GET/POST | Lista y crea citas del usuario | Cita = texto libre de mascota + servicio + fecha/hora |
| `/api/citas/:id` | DELETE | Elimina la cita | **Borrado físico**, sin estado CANCELADA |
| `/api/contacto` | POST | Guarda mensajes de contacto | Sin gestión posterior |

## 4.2 Aciertos del prototipo (conservar como decisiones)

- **Contraseñas con BCrypt desde el inicio** — coincide con RNF-03; se mantiene el mismo algoritmo en Spring Security.
- **Validación de propiedad**: un usuario solo lista y borra **sus** citas (`cita.userId === req.session.userId`). El concepto se conserva en Spring con el usuario del token.
- **Mensaje de login genérico** ("correo o contraseña incorrectos") — no revela cuál campo falló.
- **Contratos JSON simples y coherentes** — la forma de las peticiones/respuestas sirve de referencia para los DTOs de Spring Boot.
- **Fecha mínima = hoy** en el formulario de agendar (validación de usabilidad que el frontend React conserva).

## 4.3 Limitaciones estructurales (por qué se reemplaza, no se extiende)

1. **Persistencia en archivo JSON**: sin transacciones, sin integridad
   referencial, sin concurrencia segura (dos escrituras simultáneas pueden
   perder datos). Incompatible con inventario y citas concurrentes
   (RNF-08, RN-02, RN-05).
2. **El dominio no coincide con el modelo validado**: la "mascota" es un
   texto libre por cita (no una entidad con dueño, especie, historial); no
   existen empleados ni asignación de profesional; el único estado de cita
   es `'pendiente'`; los servicios no tienen tipo, duración ni precio.
3. **Sin roles**: cualquier usuario autenticado es cliente; no hay ADMIN,
   RECEPCION, VETERINARIO ni ESTETICISTA (RF-14 imposible).
4. **Sin validación de disponibilidad**: se pueden crear citas cruzadas
   ilimitadamente (RN-01, RN-02 sin cubrir).
5. **Cancelar = borrar**: se pierde la trazabilidad que exige el modelo
   (RN-06, borrado lógico de la Fase 2).
6. **Sesiones en memoria y secret en el código**: se pierden al reiniciar
   y no escalan a la app móvil; el stack definitivo usa JWT (RNF-03).
7. **Requisito académico**: el backend del proyecto debe evidenciar Java/
   Spring Boot, JPA y los patrones comprometidos; Express no cumple ese
   objetivo formativo.

**Frase de sustentación sugerida:** "El prototipo Express validó los flujos
de usuario con el menor costo posible; sus limitaciones de persistencia,
dominio y roles son precisamente los requisitos que el backend definitivo
en Spring Boot + MySQL resuelve. No se botó trabajo: se conservaron los
contratos, los flujos y las decisiones de seguridad."

---

# 5. BRECHAS FRENTE AL MODELO DISEÑADO EN FASES 1 Y 2

| Elemento del diseño | Estado en el código base | Acción |
|---------------------|--------------------------|--------|
| Entidad Mascota (RF-02) | Texto libre en la cita | Nueva página "Mis mascotas" en React; la cita referencia `mascotaId` |
| Empleado y asignación (RN-04) | No existe | El flujo de reserva agrega selección de profesional (o asignación automática) |
| Estados de cita (RF-07, RN-06) | Solo `'pendiente'`; cancelar borra | El botón "Cancelar" pasa a `PATCH` de estado; la lista muestra el estado con su color |
| Disponibilidad (RF-05) | No existe | El formulario consulta el servicio PHP antes de ofrecer horas |
| Servicios con precio/duración/tipo (RF-04) | Lista de nombres | Se consumen del catálogo real; la UI muestra precio y duración |
| Productos desde inventario (RF-11) | Quemados en el cliente | `GET /api/v1/productos` (solo activos, precio real) |
| Roles (RF-14) | No existen | El header y las rutas de React condicionan menús por rol del JWT |
| Panel interno de gestión | No existe | Nuevas rutas protegidas por rol (se construyen tras el backend) |

Las 9 tarjetas de servicios de `servicios.html` y las 9 opciones del
prototipo coinciden entre sí y son un insumo real del negocio: se usarán
para poblar el catálogo `servicio` de MySQL (ajustando la lista de la Fase
2 a estos nombres reales, con su tipo CONSULTA/SPA y precios que defina la
veterinaria).

---

# 6. PLAN DE MIGRACIÓN: MAPEO PÁGINA → COMPONENTE REACT

## 6.1 Estructura de proyecto propuesta

```
frontend-react/
├── public/
│   └── img/Imgjscam1.jpg          # imagen del hero (se conserva)
├── src/
│   ├── main.jsx                   # punto de entrada
│   ├── App.jsx                    # rutas (React Router)
│   ├── index.css                  # variables y estilos globales (de style.css)
│   ├── context/
│   │   └── AuthContext.jsx        # sesión: usuario, login, logout (antes: script.js)
│   ├── services/
│   │   └── api.js                 # cliente HTTP centralizado (fetch + token)
│   ├── components/                # reutilizables
│   │   ├── Header.jsx             # antes: <header> duplicado en 7 páginas
│   │   ├── Footer.jsx
│   │   ├── Hero.jsx               # antes: .services-hero repetido por página
│   │   ├── ServiceCard.jsx        # antes: .service-card / article.card
│   │   ├── ProductCard.jsx        # antes: template string en productos.html
│   │   ├── FormMessage.jsx        # antes: .cita-msg / .form-msg / .auth-msg (3 copias)
│   │   ├── Panel.jsx              # antes: .panel (4 copias)
│   │   └── RutaProtegida.jsx      # antes: verificarSesion() + redirect
│   └── pages/
│       ├── Inicio.jsx             # index.html
│       ├── Servicios.jsx          # servicios.html
│       ├── Productos.jsx          # productos.html
│       ├── Nosotros.jsx           # nosotros.html
│       ├── Contacto.jsx           # contacto.html
│       ├── Ingresar.jsx           # ingresar.html (tabs login/registro)
│       └── Agendar.jsx            # agendar.html (protegida)
```

## 6.2 Mapeo detallado

| Origen (código base) | Destino (React) | Tipo de migración |
|----------------------|-----------------|-------------------|
| `<header>` repetido ×7 | `Header.jsx` + `AuthContext` | **Refactorización** (deduplicación + estado global) |
| `script.js` (header dinámico, logout) | `AuthContext.jsx` | **Refactorización** (de manipulación DOM a contexto) |
| `style.css` `:root` y componentes base | `index.css` | **Conservación** (traslado casi literal) |
| `<style>` embebidos por página | CSS del componente correspondiente | **Refactorización** (deduplicar `.panel`, `.form-*`) |
| Hero de cada página (5 variantes casi idénticas) | `Hero.jsx` con props `titulo`, `subtitulo` | **Refactorización** (5 → 1 componente parametrizado) |
| Tarjetas de `servicios.html` (9) e `index.html` (4) | `ServiceCard.jsx` + arreglo de datos / API | **Refactorización** |
| `PRODUCTOS.map(...)` en `productos.html` | `Productos.jsx` con `useEffect` → API | **Refactorización** (misma idea, fuente de datos real) |
| `escapeHtml()` (3 copias) | — (JSX escapa por defecto) | **Eliminación justificada** (mejora de seguridad heredada del framework) |
| `verificarSesion()` + redirect en `agendar.html` | `RutaProtegida.jsx` | **Refactorización** |
| Formularios (login, registro, cita, contacto) con `fetch` | Componentes con `useState` + `services/api.js` | **Refactorización** (lógica conservada, ejecución declarativa) |
| Validación fecha mínima = hoy | Prop `min` del input en `Agendar.jsx` | **Conservación** |
| Enlaces y textos de WhatsApp, datos de contacto, misión/visión/valores | Mismos contenidos en sus páginas | **Conservación** (contenido de negocio intacto) |
| Buscador decorativo de `index.html` | Conectar a productos o retirar en v1 | **Decisión pendiente** (documentar la elegida) |

## 6.3 Ejemplo de traducción directa (para el documento de sustentación)

Antes (template string en `productos.html`):

```javascript
grid.innerHTML = PRODUCTOS.map((p) => `
  <article class="producto-card">
    <span class="categoria">${escapeHtml(p.categoria)}</span>
    <h3>${escapeHtml(p.nombre)}</h3>
    <div class="precio">${escapeHtml(p.precio)}</div>
  </article>`).join('');
```

Después (JSX en `Productos.jsx`, datos desde la API):

```jsx
{productos.map((p) => (
  <ProductCard key={p.id} categoria={p.categoria}
               nombre={p.nombre} precio={p.precio} />
))}
```

Puntos de sustentación: (1) la estructura visual y las clases CSS se
conservan; (2) `escapeHtml` desaparece porque JSX escapa automáticamente;
(3) la fuente de datos pasa de un arreglo quemado al inventario real; (4)
React reconcilia el DOM en lugar de reescribir `innerHTML` completo.

---

# 7. QUÉ SE CONSERVA, QUÉ SE REFACTORIZA Y POR QUÉ (RESUMEN EJECUTIVO)

**Se conserva (identidad y aprendizaje):** la paleta y tokens de diseño,
la estructura de navegación (6 secciones), todos los contenidos de negocio
(servicios reales, datos de contacto, misión/visión), la imagen del hero,
los flujos de usuario validados (registro → login → agendar → ver/cancelar
citas), las decisiones de seguridad (BCrypt, mensajes genéricos, escape de
datos — ahora delegado a React) y los contratos conceptuales de la API.

**Se refactoriza (arquitectura):** la duplicación de header/footer/estilos
se elimina con componentes; la manipulación imperativa del DOM pasa a
estado declarativo; la sesión pasa de script global a `AuthContext`; la
navegación multipágina pasa a SPA con React Router; los datos quemados
(productos, servicios) pasan a consumirse de la API.

**Se reemplaza (con justificación técnica y académica):** el backend
Express y el archivo `db.json` ceden su lugar a Spring Boot + MySQL +
MongoDB, porque el prototipo no soporta el dominio validado (mascotas,
empleados, estados, roles, disponibilidad, inventario) ni las garantías de
integridad del modelo de la Fase 2 — y porque el objetivo formativo del
backend es Java. Las sesiones de cookie pasan a JWT para servir también a
la app móvil.

---

# 8. MAPEO DE ENDPOINTS: PROTOTIPO → API DEFINITIVA

| Prototipo Express | API Spring Boot (Fase 3) | Cambios de contrato |
|-------------------|--------------------------|---------------------|
| `POST /api/register` | `POST /api/v1/auth/registro` | Crea `dueno` + `usuario` rol CLIENTE; retorna token JWT |
| `POST /api/login` | `POST /api/v1/auth/login` | Retorna JWT + rol (el frontend condiciona menús) |
| `POST /api/logout` | (frontend descarta el token) | JWT es sin estado; no requiere endpoint |
| `GET /api/me` | `GET /api/v1/auth/me` | Igual concepto, autenticado por token |
| `GET /api/servicios` | `GET /api/v1/servicios` | Objetos completos: id, nombre, tipo, duración, precio |
| `POST /api/citas` | `POST /api/v1/citas` | Recibe `mascotaId`, `servicioId`, `empleadoId`, fecha/hora; valida RN-01/02/04 |
| `GET /api/citas` | `GET /api/v1/citas/mias` | Incluye estado y datos del servicio/profesional |
| `DELETE /api/citas/:id` | `PATCH /api/v1/citas/{id}/cancelar` | **Cancelación lógica** con verificación de antelación (RF-20) |
| `POST /api/contacto` | `POST /api/v1/contacto` | Igual; los mensajes se gestionan en el panel ADMIN |
| — | `GET /disponibilidad.php` | Nuevo: servicio PHP consultado antes de ofrecer horarios |
| — | `GET /api/v1/productos` | Nuevo: catálogo público del inventario real |
| — | `GET/POST /api/v1/mascotas` | Nuevo: gestión de mascotas del dueño |

**Nota de arquitectura:** durante la construcción del backend definitivo,
el frontend React puede seguir apuntando al prototipo Express (los flujos
básicos son compatibles) y cambiar la URL base en `services/api.js` cuando
Spring Boot esté listo. Esto permite migrar frontend y backend en paralelo
sin bloqueo — otra ventaja de haber centralizado el cliente HTTP.

---

# 9. RIESGOS DE LA MIGRACIÓN Y ORDEN DE TRABAJO RECOMENDADO

| Riesgo | Mitigación |
|--------|------------|
| Romper la identidad visual al deduplicar CSS | Migrar `style.css` primero, verificar página por página contra el sitio original (comparación visual lado a lado) |
| Mezclar migración con funcionalidades nuevas | Regla: la v1 en React replica lo existente 1:1; mascotas, estados y panel interno entran después, con el backend listo |
| Sesión: cookie (prototipo) vs JWT (definitivo) | `AuthContext` y `api.js` encapsulan el mecanismo; el cambio será interno a esos dos archivos |
| CORS al separar frontend y backend | Configurar CORS en Express ahora (y en Spring Boot después) para el origen del dev server de React |

**Orden de trabajo sugerido (sprints de la Fase 5):**

1. Andamiaje del proyecto (Vite + React Router) + `index.css` con los tokens del `style.css` original.
2. `Header`, `Footer`, `Hero` y páginas estáticas: Inicio, Servicios, Nosotros (validación visual 1:1).
3. `AuthContext` + `services/api.js` + página Ingresar contra el prototipo Express.
4. Productos (aún con el arreglo local, luego API) y Contacto.
5. `RutaProtegida` + Agendar (formulario + Mis citas).
6. Conmutación al backend Spring Boot cuando la Fase 3 esté lista; se agregan Mascotas, estados de cita y disponibilidad PHP.

Cada paso deja la aplicación funcionando — criterio de "migración por
rebanadas verticales", fácil de mostrar en los seguimientos.

---

## CONTROL DE VERSIONES DEL DOCUMENTO

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | Agosto 2026 | [Nombre del aprendiz] | Análisis del código base y plan de migración |
