# ANEXO — HALLAZGOS DE PRUEBAS FUNCIONALES
## Sistema "El Hogar de Las Mascotas" — Detección y corrección de defectos

**Programa:** Tecnología en Análisis y Desarrollo de Software (ADSO) — SENA
**Fase:** 5 — Pruebas funcionales del sistema integrado
**Fecha:** Septiembre de 2026
**Versión:** 1.0

---

## 1. PROPÓSITO DEL DOCUMENTO

Registrar los defectos y vacíos de requisitos detectados durante las pruebas
funcionales del sistema integrado (React + Spring Boot + MySQL), junto con su
análisis, corrección y las modificaciones que originaron en la documentación
de requisitos.

Este anexo evidencia el ciclo completo de aseguramiento de calidad:
**probar → detectar → analizar → documentar → corregir → verificar**, y
demuestra que la especificación de requisitos es un documento vivo que se
refina con el conocimiento que aporta la ejecución del sistema.

---

## 2. CONTEXTO DE LAS PRUEBAS

| Elemento | Descripción |
|----------|-------------|
| **Tipo de prueba** | Funcional, de caja negra, ejecutada manualmente sobre la interfaz web |
| **Entorno** | Windows 11, MySQL (XAMPP) 3306, Spring Boot 3.3 en :8080, React (Vite) en :5173 |
| **Datos** | Datos semilla del script `vetcare_bd.sql` más un cliente de prueba y su mascota |
| **Ejecutor** | Aprendiz desarrollador |
| **Casos de uso cubiertos** | UC01 (registro/login), UC02 (registrar mascota), UC04 (reservar cita), UC05 (cancelar cita) |

---

## 3. HALLAZGO H-01 — Citas simultáneas para la misma mascota

### 3.1 Descripción

Durante la ejecución del caso de uso UC04 se detectó que el sistema permitía
agendar **dos citas con horarios solapados para la misma mascota**, siempre
que los servicios fueran atendidos por profesionales distintos.

### 3.2 Pasos para reproducir

1. Autenticarse como cliente con una mascota registrada.
2. Reservar el servicio "Baño y peluquería" a las 09:05 con la esteticista.
3. Reservar el servicio "Vacunación" a las 09:06 con un veterinario, para la
   **misma** mascota.
4. **Resultado obtenido:** ambas citas se crean correctamente.
5. **Resultado esperado:** el sistema debe rechazar la segunda reserva.

### 3.3 Análisis de causa raíz

El defecto **no fue un error de implementación**, sino un **vacío en la
especificación de requisitos**. La regla RN-02, definida en la Fase 1,
protegía únicamente la agenda del profesional:

> RN-02: Un profesional no puede tener dos citas que se crucen en el tiempo.

La implementación en `CitaService.validarReglas()` cumplía esa regla de forma
correcta. Sin embargo, el análisis original omitió que **la mascota también es
un recurso físico limitado**: un mismo paciente no puede recibir dos servicios
de manera simultánea, aunque los presten profesionales diferentes en salas
distintas.

Este es un caso clásico de requisito implícito: el conocimiento del dominio lo
da por evidente, pero no quedó escrito, y por lo tanto no se implementó.

### 3.4 Regla de negocio incorporada

> **RN-08:** Una mascota no puede tener dos citas cuyos intervalos de tiempo se
> crucen, aunque sean de servicios distintos y con profesionales distintos,
> porque el paciente es un recurso físico único.

Incorporada al SRS (Fase 1, sección 2.5) en la versión 1.1 del documento.

### 3.5 Corrección implementada

**Capa:** servicio (`CitaService`), coherente con la restricción de diseño de
la Fase 1 §6.3: toda regla de negocio se implementa en la capa de servicio.

**Mecanismo:** se reutiliza la misma verificación de cruce de intervalos que ya
resolvía RN-02 —existe conflicto si `inicioA < finB && inicioB < finA`— pero
aplicada sobre las citas activas de la mascota en lugar de las del profesional.

**Archivos modificados:**

| Archivo | Cambio |
|---------|--------|
| `CitaRepository.java` | Nueva consulta derivada `findByMascotaIdAndEstadoInAndFechaHoraBetween` |
| `CitaService.java` | Nuevo bloque de validación RN-08 en `validarReglas()`; la firma del método recibe ahora la mascota |

**Mensaje al usuario:** el sistema informa la causa de forma explícita,
indicando qué cita bloquea el horario:

> RN-08: Rocky ya tiene una cita de Baño y peluquería de 09:00 a 11:30.
> Una mascota no puede estar en dos servicios a la vez.

### 3.6 Verificación de la corrección

| Caso de prueba | Entrada | Resultado esperado | Estado |
|----------------|---------|--------------------|--------|
| CP-01 | Dos servicios solapados, misma mascota, distintos profesionales | Rechazo con mensaje RN-08 | ☐ |
| CP-02 | Dos servicios solapados, mascotas distintas | Ambas citas se crean (prueba de control) | ☐ |
| CP-03 | Dos servicios consecutivos sin solapamiento, misma mascota | Ambas citas se crean | ☐ |
| CP-04 | Dos servicios solapados, mismo profesional | Rechazo con mensaje RN-02 (regresión) | ☐ |

*(Marcar cada casilla al ejecutar la prueba y adjuntar la evidencia
correspondiente.)*

La inclusión de CP-04 responde a una buena práctica de pruebas: verificar que
la corrección de un defecto no rompa el comportamiento que ya funcionaba
(prueba de regresión).

---

## 4. HALLAZGO H-02 — Duraciones de servicio no corresponden a la operación real

### 4.1 Descripción

Al analizar el hallazgo H-01 se detectó un segundo problema, esta vez de
**parametrización de datos**: las duraciones de los servicios sembradas en el
script de la Fase 2 subestimaban los tiempos reales de la veterinaria. El
servicio de baño, que en la operación real ocupa un mínimo de dos horas,
estaba registrado con 45 minutos.

### 4.2 Impacto

El impacto excede lo estético. La duración es el insumo del cálculo de
solapamiento: **una duración subestimada libera la agenda antes de tiempo** y
permite reservas que en la práctica se cruzarían, debilitando tanto RN-02 como
la nueva RN-08. Un dato mal parametrizado invalida una regla correctamente
implementada.

### 4.3 Regla de negocio incorporada

> **RN-09:** La duración registrada de cada servicio debe corresponder al
> tiempo real de ocupación del recurso, ya que es la base del cálculo de
> solapamiento (RN-02 y RN-08).

### 4.4 Corrección implementada

Script `database/ajuste_duraciones.sql`, que actualiza la tabla `servicio` con
las duraciones reales suministradas por la veterinaria. Los valores definitivos
deben validarse con el administrador del negocio antes de su ejecución en el
entorno de producción.

### 4.5 Lección de diseño

Este hallazgo confirma la pertinencia de haber declarado la duración como un
**atributo del servicio** en el modelo conceptual (Fase 1, §4.1) y no como una
constante del código: al ser un dato parametrizable, su corrección se resuelve
con una sentencia SQL y no exige recompilar ni redesplegar la aplicación.

---

## 5. SÍNTESIS Y APRENDIZAJES

| Aspecto | Conclusión |
|---------|------------|
| **Naturaleza de los hallazgos** | Ninguno fue un error de codificación: uno fue un vacío de requisitos y el otro un dato mal parametrizado |
| **Valor de las pruebas funcionales** | Ejecutar el sistema con datos y escenarios reales revela requisitos implícitos que el análisis en papel no captura |
| **Requisitos implícitos** | El conocimiento del dominio que "se da por obvio" es precisamente el que se omite en la especificación; las pruebas son el mecanismo para hacerlo explícito |
| **Arquitectura en capas** | Que ambas correcciones se resolvieran en un único punto —la capa de servicio y la tabla de parámetros— valida la decisión de concentrar las reglas de negocio fuera de controladores y frontend |
| **Documento vivo** | El SRS pasó a la versión 1.1: la especificación se refina con el conocimiento que aporta la ejecución |

---

## 6. TRAZABILIDAD DE LOS CAMBIOS

| Documento / Artefacto | Modificación |
|-----------------------|--------------|
| `docs/fase1_requisitos_veterinaria.md` | Versión 1.1: se agregan RN-08 y RN-09 a la sección 2.5, con nota de trazabilidad |
| `docs/hallazgos_pruebas_funcionales.md` | Documento nuevo (este anexo) |
| `backend-spring/.../repository/CitaRepository.java` | Consulta de citas por mascota |
| `backend-spring/.../service/CitaService.java` | Validación RN-08 |
| `database/ajuste_duraciones.sql` | Script nuevo: duraciones reales (RN-09) |

---

## 7. PUNTOS PARA LA SUSTENTACIÓN

Preguntas probables de los instructores y respuestas sugeridas:

**¿Por qué esta regla no estaba desde el principio?**
Porque es un requisito implícito del dominio. RN-02 protegía la agenda del
profesional, que era la restricción visible al modelar el proceso de reserva.
Que el paciente también sea un recurso limitado resulta evidente en la
operación diaria, pero nadie lo verbaliza en una entrevista. Las pruebas
funcionales con escenarios reales fueron el mecanismo que lo hizo explícito.

**¿Cómo garantiza que no volverá a ocurrir?**
Con la prueba de control CP-02 y la prueba de regresión CP-04, que verifican
tanto la nueva regla como que el comportamiento anterior se conserva. En la
Fase 9 estos casos se automatizarán como pruebas unitarias de `CitaService`.

**¿Qué demuestra este hallazgo sobre el proceso?**
Que la especificación de requisitos es iterativa. La versión 1.0 del SRS fue
correcta con la información disponible en su momento; la versión 1.1 incorpora
el conocimiento que solo se obtiene ejecutando el sistema. Detectar, documentar
y corregir el vacío es parte del trabajo de ingeniería, no una falla del
análisis inicial.

---

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | Septiembre 2026 | [Nombre del aprendiz] | Registro de los hallazgos H-01 y H-02 |
