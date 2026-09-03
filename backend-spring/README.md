# API REST — Veterinaria El Hogar de Las Mascotas (Spring Boot)

Backend definitivo del proyecto (Fase 3). Reemplaza al prototipo Express
conservando sus contratos y flujos, e implementa el dominio completo de
las Fases 1 y 2: mascotas como entidad, estados de cita, roles,
validaciones de agenda e inventario con trazabilidad.

## Requisitos

- Java 17+ (JDK)
- Maven 3.9+ (o el wrapper de tu IDE)
- MySQL 8 con el esquema `vetcare_db` creado por el script de la Fase 2
  (`vetcare_bd.sql`)

## Puesta en marcha

```bash
# 1. Crear la base de datos (si no existe)
mysql -u root -p < vetcare_bd.sql

# 2. Ajustar credenciales en src/main/resources/application.properties
#    (spring.datasource.username / password)

# 3. Compilar y ejecutar
mvn spring-boot:run
# → API en http://localhost:8080
```

### Usuario administrador de prueba

El script de la Fase 2 siembra usuarios con hashes de ejemplo no
funcionales. Para probar los endpoints de personal, ejecuta este SQL
(crea la cuenta `admin` con contraseña `Admin123*`, hash BCrypt real):

```sql
UPDATE usuario
SET password_hash = '$2a$10$ySN7cuBPTiz/g4VqvjP8fu9joCNP82RMuA0OFoTmLJyRWkb2MAt/m'
WHERE username = 'admin';
```

Los clientes se registran directamente por `POST /api/v1/auth/registro`.

## Pruebas

```bash
mvn test
```

Incluye dos suites que evidencian los pilares POO sin necesidad de base
de datos: `InventarioPolimorfismoTest` (polimorfismo de `aplicar()` y
`calcularPrecio()`, RN-05, RF-13, RN-04) y `CitaEstadosTest`
(encapsulamiento de la máquina de estados, RN-06).

## Prueba rápida con curl

```bash
# Registro de cliente (retorna el token)
curl -X POST http://localhost:8080/api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombres":"Laura","apellidos":"Gomez","numeroDocumento":"1035871299",
       "telefono":"3001112233","correo":"laura@test.com","password":"secreto1"}'

# Con el token recibido:
TOKEN="...pegar aqui..."

# Crear mascota
curl -X POST http://localhost:8080/api/v1/mascotas \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Rocky","especie":"PERRO","sexo":"M","tamano":"GRANDE"}'

# Reservar cita (ids segun los datos semilla de la Fase 2)
curl -X POST http://localhost:8080/api/v1/citas \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"mascotaId":1,"servicioId":1,"empleadoId":3,"fechaHora":"2026-08-20T09:00:00"}'
```

## Estructura (arquitectura en capas — patron MVC + Repository + DTO)

```
controller/  → traduce HTTP ↔ servicios; @PreAuthorize por rol
service/     → TODAS las reglas de negocio (RN-01, RN-02, RN-04, RF-20…)
repository/  → interfaces Spring Data (patron Repository)
entity/      → dominio POO: herencia, polimorfismo, encapsulamiento
dto/         → contratos de entrada/salida (patron DTO)
config/      → seguridad JWT, CORS, BCrypt
exception/   → errores de negocio → respuestas JSON uniformes
```

## Conmutación del frontend React

En `frontend-react/vite.config.js` cambiar el proxy a
`http://localhost:8080` y en `src/services/api.js` ajustar las rutas al
prefijo `/api/v1` y agregar el encabezado `Authorization: Bearer <token>`
(el token llega en la respuesta de login/registro y se guarda en el
AuthContext).
