-- ============================================================
-- ACTUALIZACION v2 - Veterinaria El Hogar de Las Mascotas
--
-- Aplica sobre una base vetcare_db que ya existe y tiene datos.
-- No borra informacion: agrega la tabla del historial clinico y
-- reemplaza el personal de ejemplo por el personal real.
--
-- Ejecutar completo en MySQL Workbench.
--
-- IMPORTANTE - revise antes de ejecutar los valores marcados con
-- la palabra REVISAR: son datos del personal que yo no conozco
-- (documentos, telefonos, correos, tarjeta profesional) y debe
-- reemplazarlos por los verdaderos.
-- ============================================================

USE vetcare_db;

-- ============================================================
-- 1. HISTORIAL CLINICO (RF-09, RF-10)
-- ============================================================
-- Decision revisada respecto de la Fase 1: el historial se
-- implementa relacional y no documental. El registro tiene
-- estructura fija, volumen bajo y tres relaciones fuertes
-- (mascota, cita, veterinario); separarlo en otro motor
-- obligaria a sostener la integridad referencial por codigo
-- sin ninguna ganancia a cambio.

CREATE TABLE IF NOT EXISTS historial_clinico (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    mascota_id        BIGINT UNSIGNED NOT NULL,
    cita_id           BIGINT UNSIGNED NULL,
    veterinario_id    BIGINT UNSIGNED NOT NULL,
    fecha             DATETIME     NOT NULL,
    motivo            VARCHAR(255) NOT NULL,
    diagnostico       VARCHAR(500) NOT NULL,
    tratamiento       VARCHAR(500) NULL,
    vacunas_aplicadas VARCHAR(255) NULL,
    observaciones     VARCHAR(500) NULL,
    peso_kg           DOUBLE       NULL,

    -- Una cita genera a lo sumo un registro clinico
    CONSTRAINT uk_historial_cita UNIQUE (cita_id),

    CONSTRAINT fk_historial_mascota FOREIGN KEY (mascota_id)
        REFERENCES mascota (id)  ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_historial_cita FOREIGN KEY (cita_id)
        REFERENCES cita (id)     ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_historial_vet FOREIGN KEY (veterinario_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE RESTRICT,

    CONSTRAINT chk_historial_peso CHECK (peso_kg IS NULL OR peso_kg > 0),

    -- La consulta mas frecuente es "el historial de este paciente,
    -- del mas reciente al mas antiguo" (RF-10). Se declara aqui y no
    -- con CREATE INDEX aparte para que el script pueda repetirse.
    KEY idx_historial_mascota_fecha (mascota_id, fecha)
) ENGINE = InnoDB
  COMMENT = 'Historia clinica por atencion (RF-09, RF-10)';

-- Vista de apoyo: ultima atencion de cada paciente.
CREATE OR REPLACE VIEW v_ultima_atencion AS
SELECT h.mascota_id,
       m.nombre                              AS mascota,
       MAX(h.fecha)                          AS ultima_atencion,
       COUNT(*)                              AS atenciones_registradas
  FROM historial_clinico h
  JOIN mascota m ON m.id = h.mascota_id
 GROUP BY h.mascota_id, m.nombre;


-- ============================================================
-- 2. PERSONAL REAL DE LA CLINICA
-- ============================================================
-- El equipo que opera el sistema son tres personas. El rol
-- RECEPCION permanece en el modelo para un crecimiento futuro
-- del equipo, pero hoy no tiene titular: la administradora
-- asume esas funciones, y por eso cada permiso de recepcion
-- incluye tambien a ADMIN.

-- 2.1 Se retiran los empleados de ejemplo. Es una baja LOGICA:
--     hay citas de prueba que los referencian y borrarlos
--     romperia esa trazabilidad.
UPDATE empleado
   SET activo = 0
 WHERE correo LIKE '%@vetcare.com';

-- 2.2 Alta del personal real.
--     REVISAR: documentos, telefonos, correos y tarjeta profesional.
INSERT INTO empleado (tipo_documento, numero_documento, nombres, apellidos,
                      telefono, correo, rol, tarjeta_profesional,
                      especialidad, certificacion, hora_inicio, hora_fin)
VALUES
('CC','00000001','Arlin','Gaviria','3042083475',
 'arlin.gaviria@hogardelasmascotas.com','VETERINARIO',
 'REVISAR-TP','Medicina general', NULL, '08:30:00','18:00:00'),

('CC','00000002','Candelaria','Silgado','3042083475',
 'candelaria.silgado@hogardelasmascotas.com','ESTETICISTA',
 NULL, NULL, 'Peluqueria canina y felina', '08:30:00','18:00:00'),

('CC','00000003','Milena','Pajaro','3042083475',
 'milena.pajaro@hogardelasmascotas.com','ADMIN',
 NULL, NULL, NULL, '08:30:00','18:00:00')
-- Se usa VALUES(columna) y no el alias de fila de MySQL 8.0.19:
-- XAMPP trae MariaDB, que no admite esa sintaxis mas reciente.
ON DUPLICATE KEY UPDATE
    nombres   = VALUES(nombres),
    apellidos = VALUES(apellidos),
    telefono  = VALUES(telefono),
    rol       = VALUES(rol),
    activo    = 1;


-- ============================================================
-- 3. CUENTAS DE ACCESO
-- ============================================================
-- El correo institucional ES el nombre de usuario: el formulario
-- web valida ese campo como correo y AuthService registra a los
-- clientes con el suyo, de modo que el criterio es uniforme.
--
-- Contrasena inicial de las tres cuentas:  Clave123*
-- Cambiela despues de la primera entrada.

SET @hash = '$2b$10$sZ5p3.ULT.Sdqhnu1qJbXuL.bdQDePTy2Fz2RaQQcYS1BVogeGF4O';

-- 3.1 Se desactivan las cuentas de ejemplo que traia el script
--     original. Su hash era un valor de relleno que no
--     correspondia a ninguna contrasena real.
UPDATE usuario
   SET activo = 0
 WHERE username IN ('admin', 'recepcion', 'avet');

-- 3.2 Una cuenta por cada empleado activo, con su correo como usuario.
INSERT INTO usuario (username, password_hash, rol, empleado_id, dueno_id)
SELECT LOWER(e.correo), @hash, e.rol, e.id, NULL
  FROM empleado e
 WHERE e.activo = 1
   AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.username = LOWER(e.correo));

-- 3.3 Cuenta de cliente para pruebas (Laura Gomez, duena 1 del
--     juego de datos). Permite demostrar el recorrido completo
--     del cliente sin inventar uno nuevo en cada prueba.
--     Se renombra la cuenta existente en vez de crear otra, para
--     no dejar dos usuarios apuntando al mismo cliente.
UPDATE usuario u
  JOIN dueno d ON d.id = u.dueno_id
   SET u.username      = LOWER(d.correo),
       u.password_hash = @hash,
       u.activo        = 1
 WHERE u.username = 'laura';


-- ============================================================
-- 4. DURACIONES REALES DE LOS SERVICIOS (RN-09)
-- ============================================================
-- La duracion es el insumo del calculo de solapamiento: si esta
-- subestimada, la agenda se libera antes de tiempo y admite
-- reservas que en la practica se cruzarian.
-- REVISAR con Arlin y Candelaria los tiempos verdaderos.

UPDATE servicio SET duracion_minutos = 120 WHERE nombre = 'Bano y peluqueria';
UPDATE servicio SET duracion_minutos =  90 WHERE nombre = 'Bano medicado';
UPDATE servicio SET duracion_minutos =  30 WHERE nombre = 'Corte de unas';


-- ============================================================
-- 5. VERIFICACION
-- ============================================================
SELECT 'Personal y cuentas' AS revision;
SELECT u.username AS usuario_para_iniciar_sesion,
       u.rol,
       COALESCE(CONCAT(e.nombres,' ',e.apellidos),
                CONCAT(d.nombres,' ',d.apellidos)) AS persona,
       u.activo,
       CHAR_LENGTH(u.password_hash) AS largo_hash
  FROM usuario u
  LEFT JOIN empleado e ON e.id = u.empleado_id
  LEFT JOIN dueno    d ON d.id = u.dueno_id
 WHERE u.activo = 1
 ORDER BY FIELD(u.rol,'ADMIN','RECEPCION','VETERINARIO','ESTETICISTA','CLIENTE');

SELECT 'Tabla de historial creada' AS revision;
SELECT COUNT(*) AS columnas_historial
  FROM information_schema.columns
 WHERE table_schema = 'vetcare_db' AND table_name = 'historial_clinico';

-- El largo del hash debe ser 60 en todas las filas y la tabla
-- historial_clinico debe reportar 11 columnas.
