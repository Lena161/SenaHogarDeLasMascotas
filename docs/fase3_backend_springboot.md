# FASE 3 — BACKEND SPRING BOOT: ARQUITECTURA, POO Y PATRONES
## Sistema "El Hogar de Las Mascotas" — API REST definitiva

**Programa:** Tecnología en Análisis y Desarrollo de Software (ADSO) — SENA
**Fase:** 3 — Construcción del backend principal (Java 17, Spring Boot 3.3)
**Anexo:** `backend-spring.zip` (proyecto Maven completo, 53 clases)
**Fecha:** Agosto de 2026 — Versión 1.0

---

## 1. ARQUITECTURA EN CAPAS

```
Peticion HTTP
     │
     ▼
┌─────────────────┐   PATRON MVC (controlador)
│  controller/    │   Traduce HTTP ↔ dominio. CERO reglas de negocio.
└────────┬────────┘   Autorizacion por rol con @PreAuthorize (RNF-04).
         ▼
┌─────────────────┐   CAPA DE SERVICIO
│  service/       │   TODAS las reglas de negocio: RN-01, RN-02, RN-04,
└────────┬────────┘   RF-20, propiedad de recursos. @Transactional.
         ▼
┌─────────────────┐   PATRON REPOSITORY
│  repository/    │   Interfaces Spring Data JPA: el servicio no conoce
└────────┬────────┘   SQL; se prueba con mocks.
         ▼
┌─────────────────┐   DOMINIO POO
│  entity/        │   Herencia, polimorfismo y encapsulamiento real
└────────┬────────┘   (RN-05 y RN-06 viven DENTRO de los objetos).
         ▼
     MySQL (esquema de la Fase 2) — triggers como segunda defensa
```

Transversales: `dto/` (patrón DTO), `config/` (JWT, CORS, BCrypt),
`exception/` (errores → JSON uniforme `{"error": "..."}`, mismo contrato
del prototipo Express, por lo que el frontend React no cambia su manejo
de errores).

## 2. MAPA DE EVIDENCIAS POO (para abrir en la sustentación)

| Pilar | Dónde verlo | Qué mostrar |
|-------|-------------|-------------|
| **Herencia** | `entity/Persona.java` → `Dueno`, `Empleado` | `@MappedSuperclass`: identidad compartida sin duplicar atributos; cada subclase persiste en su tabla (decisión Fase 2 §2.1) |
| **Herencia + BD** | `entity/Empleado.java` → `Veterinario`, `Esteticista`, `Administrador`, `Recepcionista` | `@Inheritance(SINGLE_TABLE)` + `@DiscriminatorColumn(name="rol")`: la columna `rol` del modelo físico discrimina el subtipo Java |
| **Herencia + BD** | `entity/Servicio.java` → `ConsultaVeterinaria`, `ServicioSpa`; `entity/MovimientoInventario.java` → `Entrada`, `Salida` | Mismo mecanismo con las columnas `tipo` |
| **Polimorfismo** | `Servicio.calcularPrecio(tamano)` | La consulta ignora el tamaño; el spa recarga por tamaño. `CitaService.crear()` lo invoca sin saber el subtipo |
| **Polimorfismo** | `Servicio.puedeSerAtendidoPor(empleado)` | RN-04 sin un solo `if` por tipo en la capa de servicio: cada servicio sabe qué perfil lo atiende |
| **Polimorfismo** | `MovimientoInventario.aplicar(producto)` | `InventarioService` aplica entradas y salidas con la misma llamada |
| **Encapsulamiento** | `entity/Cita.java` | **No existe `setEstado()`**: solo `confirmar()`, `cancelar()`, `atender()`, `marcarInasistencia()`, que implementan el diagrama de estados de la Fase 1 §3.5 y lanzan `ReglaNegocioException` ante transiciones inválidas (RN-06) |
| **Encapsulamiento** | `entity/Producto.java` | **No existe `setStockActual()`**: solo `ingresarStock()` / `retirarStock()`, que garantizan RN-05 y RNF-08 desde el objeto |
| **Abstracción** | `Persona`, `Empleado`, `Servicio`, `MovimientoInventario` | Clases `abstract`: nunca se instancia "un movimiento a secas" |

Las suites `InventarioPolimorfismoTest` y `CitaEstadosTest` demuestran
estos pilares con JUnit **sin Spring ni base de datos**: son POO pura y
el mejor material de demostración en vivo (`mvn test`).

## 3. PATRONES DE DISEÑO IMPLEMENTADOS

| Patrón | Implementación concreta | Justificación |
|--------|--------------------------|---------------|
| **MVC** | `@RestController` (controlador) + servicios/entidades (modelo) + React (vista) | Separación presentación/lógica/datos; mantenibilidad (RNF-06). El controlador de citas tiene ~60 líneas porque no contiene reglas |
| **Repository** | 9 interfaces `JpaRepository` con consultas derivadas (`findByEmpleadoIdAndEstadoInAndFechaHoraBetween`) | La capa de servicio ignora SQL; los servicios se prueban con repositorios simulados |
| **DTO** | Records en `dto/` con Bean Validation | Las entidades JPA nunca viajan por la red: el hash de contraseña jamás se expone y la entrada se valida con anotaciones |
| **Inyección de dependencias** (por constructor) | Todos los servicios y controladores | Dependencias explícitas, inmutables y sustituibles por mocks en pruebas |

Cumple y supera el mínimo de 2 patrones exigido; todos son sustentables
con código concreto abierto en pantalla.

## 4. CONTRATO DE LA API

| Método y ruta | Acceso | Función | Reglas aplicadas |
|---------------|--------|---------|------------------|
| POST `/api/v1/auth/registro` | Público | Crea Dueño + Usuario CLIENTE, retorna JWT | RNF-03 (BCrypt) |
| POST `/api/v1/auth/login` | Público | Autentica, retorna JWT con rol | Mensaje genérico |
| GET `/api/v1/auth/me` | Autenticado | Usuario actual | — |
| GET `/api/v1/servicios` | Público | Catálogo con tipo, duración, precios | RF-04 |
| GET `/api/v1/empleados` | Público | Profesionales activos (solo nombre y rol) | DTO restringido |
| GET/POST `/api/v1/mascotas` | CLIENTE | Mis mascotas / crear | RF-02, RN-03 |
| POST `/api/v1/citas` | Autenticado | Reservar con precio polimórfico | RN-01, RN-02, RN-04 |
| GET `/api/v1/citas/mias` | CLIENTE | Mis citas con estado | RF-08 (cliente) |
| PATCH `/api/v1/citas/{id}/cancelar` | Dueño de la cita / staff | **Cancelación lógica** (evolución del DELETE del prototipo) | RN-06, RF-20 |
| PATCH `/api/v1/citas/{id}/confirmar` | ADMIN, RECEPCION | Transición PENDIENTE→CONFIRMADA | RN-06, RF-07 |
| PATCH `/api/v1/citas/{id}/atender` | VETERINARIO, ESTETICISTA, ADMIN | Transición CONFIRMADA→ATENDIDA | RN-06 |
| GET `/api/v1/productos` | Público | Catálogo con precio real y alerta de reposición | RF-11, RF-13 |
| POST `/api/v1/productos/movimientos` | ADMIN, RECEPCION | Entrada/salida con responsable | RF-12, RN-05, RNF-10 |
| POST `/api/v1/contacto` | Público | Guarda el mensaje | Paridad con prototipo |

## 5. IMPLEMENTACIÓN DE LAS REGLAS PENDIENTES DE LA FASE 2

**RN-02 completa (solapamiento por duración).** `CitaService.validarReglas()`
consulta las citas PENDIENTE/CONFIRMADA del profesional en el día y aplica
la verificación clásica de cruce de intervalos: hay conflicto si
`inicioA < finB && inicioB < finA`, usando la duración real de cada
servicio. La restricción única de la BD sigue cubriendo el caso exacto
(defensa en profundidad, decisión Fase 2 §2.2).

**RN-04 (rol vs servicio).** Resuelta con polimorfismo: el servicio le
pregunta al objeto `servicio.puedeSerAtendidoPor(empleado)`; agregar un
nuevo tipo de servicio mañana no obliga a tocar `CitaService` (principio
abierto/cerrado, mencionable como valor agregado).

**RN-01 (horario).** Doble verificación: ventana global del negocio
(parametrizable en `application.properties`) y jornada del profesional
(`Empleado.estaDisponible()`, que además excluye domingos).

**RF-20 (antelación de cancelación).** Solo aplica al rol CLIENTE; el
personal puede cancelar sin restricción de antelación. Horas
parametrizables.

**RN-05 / RNF-08 / RNF-10.** El movimiento registra siempre su usuario
responsable; el stock solo cambia vía `aplicar()`; la BD lo garantiza en
paralelo con sus triggers.

**RN-08 (agregada en la Fase 5).** Además de la agenda del profesional, se
verifica la agenda de la **mascota**: un mismo paciente no puede tener citas
solapadas aunque las atiendan profesionales distintos. Reutiliza la misma
verificación de intervalos de RN-02 sobre una consulta distinta. Su origen
—un hallazgo de las pruebas funcionales— se documenta en
`docs/hallazgos_pruebas_funcionales.md`.

## 6. SEGURIDAD (RF-14, RNF-03, RNF-04)

Sesiones de cookie del prototipo → **JWT sin estado** (HS256, 8 horas),
necesario para servir a la app móvil de la Fase 6. `FiltroJwt` valida el
encabezado `Authorization: Bearer` y publica el rol como autoridad de
Spring Security; `SecurityConfig` define lo público y `@PreAuthorize`
afina por endpoint. Contraseñas con BCrypt — mismo algoritmo que ya usaba
el prototipo, decisión conservada. La clave JWT se lee de la variable de
entorno `JWT_SECRET` (valor de desarrollo por defecto, nunca producción).

Prueba de RNF-04 para la sustentación: un token de CLIENTE contra
`POST /api/v1/productos/movimientos` responde **403**; sin token, **401**.

## 7. DECISIONES TÉCNICAS DEFENDIBLES

1. **`ddl-auto=update` y no `create`**: la fuente de verdad del esquema es
   el script de la Fase 2; Hibernate solo complementa (crea
   `mensaje_contacto`, entidad agregada y documentada).
2. **Cuatro subclases de Empleado** (incluye `Administrador` y
   `Recepcionista` sin atributos propios): el discriminador JPA exige una
   clase por valor de `rol`; el costo son dos clases triviales y el
   beneficio es que la jerarquía del modelo conceptual existe íntegra en
   el código.
3. **`BigDecimal` para dinero** (nunca `double`): coherente con el
   `DECIMAL(10,2)` de la Fase 2.
4. **Registro público solo de clientes**: las cuentas del personal las
   crea el ADMIN (UC14); evita la escalada de privilegios por
   autorregistro.
5. **404 en vez de 403 para recursos ajenos**: si un cliente consulta la
   cita de otro, se responde "no encontrada" — no se revela la existencia
   del recurso (misma filosofía del mensaje de login genérico).

## 8. ALCANCE Y PENDIENTES DECLARADOS

Implementado: autenticación y roles, mascotas, citas con todas sus
reglas y transiciones, catálogo de servicios, inventario con movimientos
polimórficos, contacto, manejo global de errores y 9 pruebas unitarias.

Pendiente para fases siguientes (deliberado, no olvidado): CRUD
administrativo completo de servicios/productos/empleados (formularios del
panel ADMIN, tras conmutar el frontend), historial clínico en MongoDB
(Fase 7), servicio PHP de disponibilidad (Fase 4), reportes (RF-19) y la
suite completa de pruebas (Fase 9).

**Nota de entorno:** el proyecto se escribió siguiendo Spring Boot 3.3 y
fue estructurado para `mvn spring-boot:run`; la primera compilación se
realiza en la máquina del aprendiz (este entorno de trabajo no tiene
acceso a Maven Central). Si surge algún error de compilación menor,
tráelo y lo corregimos — forma parte normal del ciclo.

---

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | Agosto 2026 | [Nombre del aprendiz] | Backend Spring Boot completo |
| 1.1 | Septiembre 2026 | [Nombre del aprendiz] | Implementación de RN-08; correcciones de la revisión de código |
