-- ============================================================================
-- SISTEMA VETCARE - SCRIPT DE BASE DE DATOS MySQL 8.x
-- Proyecto ADSO - SENA | Fase 2: Modelo de datos relacional
-- Contenido: creacion de esquema, tablas, restricciones, indices,
--            triggers, vistas y datos semilla de prueba.
-- Convenciones: snake_case, InnoDB, utf8mb4.
-- ============================================================================

DROP DATABASE IF EXISTS vetcare_db;
CREATE DATABASE vetcare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE vetcare_db;

-- ============================================================================
-- 1. TABLAS MAESTRAS (sin dependencias)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: dueno (cliente propietario de mascotas)
-- ----------------------------------------------------------------------------
CREATE TABLE dueno (
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tipo_documento   ENUM('CC','CE','TI','PASAPORTE') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20)  NOT NULL,
    nombres          VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(80)  NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    correo           VARCHAR(120) NOT NULL,
    direccion        VARCHAR(150) NULL,
    activo           TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_dueno_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT uk_dueno_correo    UNIQUE (correo)
) ENGINE = InnoDB
  COMMENT = 'Clientes propietarios de mascotas';

-- ----------------------------------------------------------------------------
-- Tabla: empleado (personal de la veterinaria)
-- El atributo rol discrimina el subtipo (herencia aplanada: single table)
-- ----------------------------------------------------------------------------
CREATE TABLE empleado (
    id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tipo_documento       ENUM('CC','CE','PASAPORTE') NOT NULL DEFAULT 'CC',
    numero_documento     VARCHAR(20) NOT NULL,
    nombres              VARCHAR(80) NOT NULL,
    apellidos            VARCHAR(80) NOT NULL,
    telefono             VARCHAR(20) NOT NULL,
    correo               VARCHAR(120) NOT NULL,
    rol                  ENUM('ADMIN','RECEPCION','VETERINARIO','ESTETICISTA')
                         NOT NULL,
    -- Atributos propios del subtipo VETERINARIO (NULL para los demas)
    tarjeta_profesional  VARCHAR(30) NULL,
    especialidad         VARCHAR(60) NULL,
    -- Atributos propios del subtipo ESTETICISTA (NULL para los demas)
    certificacion        VARCHAR(60) NULL,
    hora_inicio          TIME NOT NULL DEFAULT '08:00:00',
    hora_fin             TIME NOT NULL DEFAULT '18:00:00',
    activo               TINYINT(1) NOT NULL DEFAULT 1,
    creado_en            TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_empleado_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT uk_empleado_correo    UNIQUE (correo),
    CONSTRAINT chk_empleado_jornada  CHECK (hora_inicio < hora_fin),
    -- RN: un veterinario debe tener tarjeta profesional registrada
    CONSTRAINT chk_vet_tarjeta CHECK (
        rol <> 'VETERINARIO' OR tarjeta_profesional IS NOT NULL
    )
) ENGINE = InnoDB
  COMMENT = 'Empleados; el rol discrimina el subtipo de la jerarquia POO';

-- ----------------------------------------------------------------------------
-- Tabla: servicio (catalogo: consultas y servicios de spa)
-- ----------------------------------------------------------------------------
CREATE TABLE servicio (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(80) NOT NULL,
    tipo              ENUM('CONSULTA','SPA') NOT NULL,
    descripcion       VARCHAR(255) NULL,
    duracion_minutos  SMALLINT UNSIGNED NOT NULL DEFAULT 30,
    precio_base       DECIMAL(10,2) NOT NULL,
    -- Atributo propio del subtipo SPA (recargo por tamano de mascota)
    recargo_por_tamano DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo            TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_servicio_nombre UNIQUE (nombre),
    CONSTRAINT chk_servicio_precio   CHECK (precio_base >= 0),
    CONSTRAINT chk_servicio_duracion CHECK (duracion_minutos BETWEEN 10 AND 240)
) ENGINE = InnoDB
  COMMENT = 'Catalogo de servicios ofrecidos';

-- ----------------------------------------------------------------------------
-- Tabla: producto (inventario)
-- ----------------------------------------------------------------------------
CREATE TABLE producto (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    categoria      ENUM('ALIMENTO','MEDICAMENTO','ACCESORIO','HIGIENE','INSUMO')
                   NOT NULL,
    descripcion    VARCHAR(255) NULL,
    precio         DECIMAL(10,2) NOT NULL,
    stock_actual   INT NOT NULL DEFAULT 0,
    stock_minimo   INT NOT NULL DEFAULT 5,
    activo         TINYINT(1) NOT NULL DEFAULT 1,
    creado_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_producto_nombre  UNIQUE (nombre),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    -- RNF-08: el stock nunca puede ser negativo (defensa a nivel de BD)
    CONSTRAINT chk_producto_stock  CHECK (stock_actual >= 0),
    CONSTRAINT chk_producto_minimo CHECK (stock_minimo >= 0)
) ENGINE = InnoDB
  COMMENT = 'Productos e insumos del inventario';

-- ============================================================================
-- 2. TABLAS DEPENDIENTES (con claves foraneas)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: mascota (depende de dueno) - relacion 1:N
-- ----------------------------------------------------------------------------
CREATE TABLE mascota (
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dueno_id         BIGINT UNSIGNED NOT NULL,
    nombre           VARCHAR(60) NOT NULL,
    especie          ENUM('PERRO','GATO','AVE','ROEDOR','OTRO') NOT NULL,
    raza             VARCHAR(60) NULL,
    fecha_nacimiento DATE NULL,
    sexo             ENUM('M','H') NOT NULL,
    peso_kg          DECIMAL(5,2) NULL,
    tamano           ENUM('PEQUENO','MEDIANO','GRANDE') NOT NULL DEFAULT 'MEDIANO',
    activo           TINYINT(1) NOT NULL DEFAULT 1,
    creado_en        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mascota_dueno FOREIGN KEY (dueno_id)
        REFERENCES dueno (id)
        ON DELETE RESTRICT      -- RN-03: no se borra un dueno con mascotas
        ON UPDATE CASCADE,
    CONSTRAINT chk_mascota_peso CHECK (peso_kg IS NULL OR peso_kg > 0)
) ENGINE = InnoDB
  COMMENT = 'Mascotas asociadas a un dueno (RN-03)';

CREATE INDEX idx_mascota_dueno ON mascota (dueno_id);

-- ----------------------------------------------------------------------------
-- Tabla: usuario (credenciales de acceso; puede ser empleado o dueno)
-- ----------------------------------------------------------------------------
CREATE TABLE usuario (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt (RNF-03)',
    rol           ENUM('ADMIN','RECEPCION','VETERINARIO','ESTETICISTA','CLIENTE')
                  NOT NULL,
    empleado_id   BIGINT UNSIGNED NULL,
    dueno_id      BIGINT UNSIGNED NULL,
    activo        TINYINT(1) NOT NULL DEFAULT 1,
    creado_en     TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_usuario_dueno FOREIGN KEY (dueno_id)
        REFERENCES dueno (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- Exclusividad: un usuario es empleado O cliente, nunca ambos
    CONSTRAINT chk_usuario_vinculo CHECK (
        (empleado_id IS NOT NULL AND dueno_id IS NULL) OR
        (empleado_id IS NULL AND dueno_id IS NOT NULL)
    )
) ENGINE = InnoDB
  COMMENT = 'Cuentas de acceso al sistema con rol (RF-14)';

-- ----------------------------------------------------------------------------
-- Tabla: cita (nucleo transaccional del sistema)
-- ----------------------------------------------------------------------------
CREATE TABLE cita (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    mascota_id    BIGINT UNSIGNED NOT NULL,
    servicio_id   BIGINT UNSIGNED NOT NULL,
    empleado_id   BIGINT UNSIGNED NOT NULL,
    fecha_hora    DATETIME NOT NULL,
    estado        ENUM('PENDIENTE','CONFIRMADA','ATENDIDA','CANCELADA','NO_ASISTIO')
                  NOT NULL DEFAULT 'PENDIENTE',
    precio_final  DECIMAL(10,2) NULL COMMENT 'Resultado de calcularPrecio() polimorfico',
    observaciones VARCHAR(255) NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cita_mascota  FOREIGN KEY (mascota_id)
        REFERENCES mascota (id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicio (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN-02 (defensa parcial): un profesional no puede tener dos citas
    -- que inicien exactamente a la misma hora. El cruce por duracion
    -- se valida en la capa de servicio (Spring Boot).
    CONSTRAINT uk_cita_empleado_horario UNIQUE (empleado_id, fecha_hora)
) ENGINE = InnoDB
  COMMENT = 'Reservas de consulta y spa (RF-06, RF-07)';

CREATE INDEX idx_cita_fecha    ON cita (fecha_hora);
CREATE INDEX idx_cita_estado   ON cita (estado);
CREATE INDEX idx_cita_mascota  ON cita (mascota_id);

-- ----------------------------------------------------------------------------
-- Tabla: movimiento_inventario (entradas y salidas de stock)
-- ----------------------------------------------------------------------------
CREATE TABLE movimiento_inventario (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id  BIGINT UNSIGNED NOT NULL,
    usuario_id   BIGINT UNSIGNED NOT NULL COMMENT 'Responsable (RNF-10)',
    tipo         ENUM('ENTRADA','SALIDA') NOT NULL,
    cantidad     INT NOT NULL,
    motivo       VARCHAR(150) NOT NULL,
    -- Atributo del subtipo ENTRADA
    proveedor    VARCHAR(100) NULL,
    -- Atributo del subtipo SALIDA
    destino      ENUM('VENTA','CONSUMO_INTERNO','BAJA') NULL,
    fecha        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mov_usuario  FOREIGN KEY (usuario_id)
        REFERENCES usuario (id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0),
    -- Coherencia subtipo-atributo
    CONSTRAINT chk_mov_salida CHECK (tipo <> 'SALIDA' OR destino IS NOT NULL)
) ENGINE = InnoDB
  COMMENT = 'Trazabilidad de entradas y salidas de inventario (RF-12, RNF-10)';

CREATE INDEX idx_mov_producto ON movimiento_inventario (producto_id);
CREATE INDEX idx_mov_fecha    ON movimiento_inventario (fecha);

-- ============================================================================
-- 3. TRIGGERS: integridad del stock (RN-05, RNF-08)
--    Defensa en profundidad: la capa de servicio valida primero, pero la BD
--    garantiza la regla aunque un cliente distinto acceda a los datos.
-- ============================================================================

DELIMITER $$

CREATE TRIGGER trg_mov_inventario_before_insert
BEFORE INSERT ON movimiento_inventario
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;

    SELECT stock_actual INTO v_stock
    FROM producto
    WHERE id = NEW.producto_id
    FOR UPDATE;                 -- bloqueo de fila: evita condiciones de carrera

    IF NEW.tipo = 'SALIDA' AND NEW.cantidad > v_stock THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'RN-05: la salida supera el stock disponible';
    END IF;
END$$

CREATE TRIGGER trg_mov_inventario_after_insert
AFTER INSERT ON movimiento_inventario
FOR EACH ROW
BEGIN
    IF NEW.tipo = 'ENTRADA' THEN
        UPDATE producto
        SET stock_actual = stock_actual + NEW.cantidad
        WHERE id = NEW.producto_id;
    ELSE
        UPDATE producto
        SET stock_actual = stock_actual - NEW.cantidad
        WHERE id = NEW.producto_id;
    END IF;
END$$

DELIMITER ;

-- ============================================================================
-- 4. VISTAS: consultas frecuentes preempaquetadas
-- ============================================================================

-- Productos que requieren reposicion (RF-13)
CREATE VIEW v_productos_stock_bajo AS
SELECT p.id,
       p.nombre,
       p.categoria,
       p.stock_actual,
       p.stock_minimo,
       (p.stock_minimo - p.stock_actual) AS unidades_faltantes
FROM producto p
WHERE p.activo = 1
  AND p.stock_actual <= p.stock_minimo;

-- Agenda del dia por profesional (RF-08)
CREATE VIEW v_agenda_dia AS
SELECT c.id            AS cita_id,
       c.fecha_hora,
       c.estado,
       CONCAT(e.nombres, ' ', e.apellidos) AS profesional,
       e.rol           AS rol_profesional,
       s.nombre        AS servicio,
       s.tipo          AS tipo_servicio,
       s.duracion_minutos,
       m.nombre        AS mascota,
       m.especie,
       CONCAT(d.nombres, ' ', d.apellidos) AS dueno,
       d.telefono      AS telefono_dueno
FROM cita c
JOIN empleado e ON e.id = c.empleado_id
JOIN servicio s ON s.id = c.servicio_id
JOIN mascota  m ON m.id = c.mascota_id
JOIN dueno    d ON d.id = m.dueno_id
WHERE DATE(c.fecha_hora) = CURDATE()
  AND c.estado IN ('PENDIENTE','CONFIRMADA');

-- Servicios mas solicitados (insumo del RF-19)
CREATE VIEW v_servicios_mas_solicitados AS
SELECT s.id,
       s.nombre,
       s.tipo,
       COUNT(c.id) AS total_citas
FROM servicio s
LEFT JOIN cita c ON c.servicio_id = s.id AND c.estado = 'ATENDIDA'
GROUP BY s.id, s.nombre, s.tipo
ORDER BY total_citas DESC;

-- ============================================================================
-- 5. DATOS SEMILLA (para desarrollo y demostraciones)
-- ============================================================================

INSERT INTO dueno (tipo_documento, numero_documento, nombres, apellidos,
                   telefono, correo, direccion) VALUES
('CC','1035871234','Laura','Gomez Rios','3001112233','laura.gomez@mail.com','Cra 45 # 30-12, Medellin'),
('CC','1128456789','Carlos','Perez Mesa','3014445566','carlos.perez@mail.com','Cll 10 # 40-25, Envigado'),
('CE','987654','Ana','Duarte Silva','3027778899','ana.duarte@mail.com','Cra 80 # 12-05, Medellin');

INSERT INTO empleado (tipo_documento, numero_documento, nombres, apellidos,
                      telefono, correo, rol, tarjeta_profesional,
                      especialidad, certificacion) VALUES
('CC','71234567','Marta','Restrepo Lopez','3105551111','marta.restrepo@vetcare.com','ADMIN',NULL,NULL,NULL),
('CC','71345678','Julian','Osorio Cano','3105552222','julian.osorio@vetcare.com','RECEPCION',NULL,NULL,NULL),
('CC','71456789','Andres','Zapata Ruiz','3105553333','andres.zapata@vetcare.com','VETERINARIO','TP-2019-4432','Medicina general',NULL),
('CC','71567890','Sofia','Cardona Vega','3105554444','sofia.cardona@vetcare.com','VETERINARIO','TP-2021-8810','Dermatologia',NULL),
('CC','71678901','Paula','Mejia Arango','3105555555','paula.mejia@vetcare.com','ESTETICISTA',NULL,NULL,'Groomer certificada ABC');

INSERT INTO servicio (nombre, tipo, descripcion, duracion_minutos,
                      precio_base, recargo_por_tamano) VALUES
('Consulta general','CONSULTA','Valoracion medica general',30,60000,0),
('Consulta dermatologica','CONSULTA','Valoracion de piel y pelaje',40,85000,0),
('Vacunacion','CONSULTA','Aplicacion de biologicos',20,45000,0),
('Bano medicado','SPA','Bano con shampoo dermatologico',45,40000,10000),
('Bano y peluqueria','SPA','Bano completo mas corte de pelo',60,55000,15000),
('Corte de unas','SPA','Corte y limado de unas',15,15000,0);

INSERT INTO producto (nombre, categoria, descripcion, precio,
                      stock_actual, stock_minimo) VALUES
('Concentrado premium perro 10kg','ALIMENTO','Bulto alimento adulto',145000,0,3),
('Concentrado gato 3kg','ALIMENTO','Alimento gato adulto',52000,0,5),
('Antipulgas pipeta','MEDICAMENTO','Pipeta antipulgas dosis unica',38000,0,10),
('Shampoo medicado 500ml','HIGIENE','Shampoo dermatologico',32000,0,4),
('Collar mediano','ACCESORIO','Collar ajustable talla M',25000,0,5),
('Jeringas 5ml x100','INSUMO','Caja de jeringas desechables',28000,0,2);

INSERT INTO usuario (username, password_hash, rol, empleado_id, dueno_id) VALUES
-- Hash BCrypt de ejemplo (la app generara los reales): "Password123*"
('admin',   '$2a$10$abcdefghijklmnopqrstuvICKq0DpRrDGVcpOXHk3sYqWm9r0hBz2', 'ADMIN',       1, NULL),
('recepcion','$2a$10$abcdefghijklmnopqrstuvICKq0DpRrDGVcpOXHk3sYqWm9r0hBz2','RECEPCION',   2, NULL),
('avet',    '$2a$10$abcdefghijklmnopqrstuvICKq0DpRrDGVcpOXHk3sYqWm9r0hBz2', 'VETERINARIO', 3, NULL),
('laura',   '$2a$10$abcdefghijklmnopqrstuvICKq0DpRrDGVcpOXHk3sYqWm9r0hBz2', 'CLIENTE',  NULL, 1);

INSERT INTO mascota (dueno_id, nombre, especie, raza, fecha_nacimiento,
                     sexo, peso_kg, tamano) VALUES
(1,'Rocky','PERRO','Labrador','2022-03-15','M',28.50,'GRANDE'),
(1,'Misu','GATO','Criollo','2023-07-01','H',4.20,'PEQUENO'),
(2,'Toby','PERRO','Beagle','2021-11-20','M',12.00,'MEDIANO'),
(3,'Kira','PERRO','Poodle','2024-01-10','H',6.80,'PEQUENO');

-- Entradas iniciales de inventario (el trigger actualiza el stock)
INSERT INTO movimiento_inventario (producto_id, usuario_id, tipo, cantidad,
                                   motivo, proveedor, destino) VALUES
(1, 1, 'ENTRADA', 10, 'Compra inicial', 'Distribuidora Animal S.A.S', NULL),
(2, 1, 'ENTRADA', 15, 'Compra inicial', 'Distribuidora Animal S.A.S', NULL),
(3, 1, 'ENTRADA', 30, 'Compra inicial', 'Laboratorios PetMed', NULL),
(4, 1, 'ENTRADA',  8, 'Compra inicial', 'Laboratorios PetMed', NULL),
(5, 1, 'ENTRADA', 12, 'Compra inicial', 'Accesorios Caninos Ltda', NULL),
(6, 1, 'ENTRADA',  5, 'Compra inicial', 'Insumos Clinicos S.A.', NULL);

-- Salidas de ejemplo (ventas)
INSERT INTO movimiento_inventario (producto_id, usuario_id, tipo, cantidad,
                                   motivo, proveedor, destino) VALUES
(3, 2, 'SALIDA', 2, 'Venta a cliente Laura Gomez', NULL, 'VENTA'),
(4, 2, 'SALIDA', 1, 'Uso en bano medicado de Rocky', NULL, 'CONSUMO_INTERNO');

-- Citas de ejemplo
INSERT INTO cita (mascota_id, servicio_id, empleado_id, fecha_hora,
                  estado, precio_final, observaciones) VALUES
(1, 1, 3, '2026-08-14 09:00:00', 'CONFIRMADA', 60000, 'Chequeo anual'),
(2, 3, 4, '2026-08-14 10:00:00', 'PENDIENTE',  45000, 'Refuerzo triple felina'),
(3, 5, 5, '2026-08-14 11:00:00', 'CONFIRMADA', 70000, 'Bano y corte, talla M: 55000 + 15000'),
(4, 4, 5, '2026-08-15 09:30:00', 'PENDIENTE',  40000, NULL),
(1, 1, 3, '2026-08-10 09:00:00', 'ATENDIDA',   60000, 'Cita historica atendida');

-- ============================================================================
-- 6. CONSULTAS DE VERIFICACION (usar en la sustentacion)
-- ============================================================================

-- 6.1 Stock resultante tras los movimientos (debe reflejar entradas - salidas)
SELECT nombre, stock_actual, stock_minimo FROM producto ORDER BY id;

-- 6.2 Productos en alerta de reposicion (RF-13)
SELECT * FROM v_productos_stock_bajo;

-- 6.3 Servicios mas solicitados (RF-19)
SELECT * FROM v_servicios_mas_solicitados;

-- 6.4 Historial de citas de una mascota con datos del dueno
SELECT c.fecha_hora, s.nombre AS servicio, c.estado, c.precio_final,
       CONCAT(e.nombres,' ',e.apellidos) AS profesional
FROM cita c
JOIN servicio s ON s.id = c.servicio_id
JOIN empleado e ON e.id = c.empleado_id
WHERE c.mascota_id = 1
ORDER BY c.fecha_hora DESC;

-- 6.5 Trazabilidad de inventario de un producto (RNF-10)
SELECT m.fecha, m.tipo, m.cantidad, m.motivo,
       u.username AS responsable
FROM movimiento_inventario m
JOIN usuario u ON u.id = m.usuario_id
WHERE m.producto_id = 3
ORDER BY m.fecha;
