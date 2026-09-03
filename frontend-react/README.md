# Frontend React — Veterinaria El Hogar de Las Mascotas

Migración del sitio HTML/CSS/JS vanilla a React (Vite + React Router),
conservando la identidad visual y los flujos validados del código base.
Corresponde a los pasos 1–5 del plan de migración (documento
`analisis_codigo_base_migracion_react.md`).

## Requisitos

- Node.js 18 o superior
- El backend prototipo Express corriendo en el puerto 3000
  (el `server.js` del código base: `node server.js`)

## Puesta en marcha

```bash
# 1. Instalar dependencias
npm install

# 2. En otra terminal, levantar el backend prototipo
#    (desde la carpeta del backend del código base)
node server.js

# 3. Levantar el frontend en modo desarrollo
npm run dev
# → http://localhost:5173
```

El proxy configurado en `vite.config.js` redirige `/api` al backend en
`localhost:3000`, por lo que login, registro, citas y contacto funcionan
igual que en el sitio original. Cuando el backend Spring Boot esté listo
(Fase 3), solo se cambia el `target` del proxy a `http://localhost:8080`
y se ajusta `src/services/api.js`.

## Estructura

```
src/
├── main.jsx                # punto de entrada
├── App.jsx                 # rutas de la SPA (React Router)
├── index.css               # tokens y estilos migrados de style.css
├── context/AuthContext.jsx # sesión (antes: script.js + innerHTML)
├── services/api.js         # cliente HTTP centralizado
├── data/                   # contenido de negocio conservado 1:1
├── components/             # Header, Footer, Hero, tarjetas, RutaProtegida…
└── pages/                  # Inicio, Servicios, Productos, Nosotros,
                            # Contacto, Ingresar, Agendar (protegida)
```

## Decisiones de migración visibles en el código

- Cada archivo abre con un comentario "Antes / Ahora" que documenta qué
  se conservó y qué se refactorizó — insumo directo de la sustentación.
- `escapeHtml()` del código base desapareció: JSX escapa automáticamente.
- El botón "Cancelar" usa aún el `DELETE` del prototipo; el comentario en
  `services/api.js` registra que la API definitiva lo convierte en
  cancelación lógica (`PATCH /citas/{id}/cancelar`, regla RN-06).
- Los productos siguen siendo un arreglo local (réplica 1:1 del sitio);
  el comentario en `src/data/productos.jsx` documenta su reemplazo por
  `GET /api/v1/productos` cuando exista el inventario real.

## Scripts

- `npm run dev` — servidor de desarrollo con recarga
- `npm run build` — compilación de producción (carpeta `dist/`)
- `npm run preview` — sirve la compilación de producción
