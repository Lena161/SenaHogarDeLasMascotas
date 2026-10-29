-- ============================================================
-- ACTUALIZACION v3 - El Hogar de Las Mascotas
-- Formato completo de la historia clinica
--
-- Ejecutar DESPUES de actualizacion_v2.sql.
-- No borra informacion: solo agrega columnas a la tabla
-- historial_clinico.
-- ============================================================

USE vetcare_db;

-- ------------------------------------------------------------
-- Campos del formato clinico veterinario
--
-- Las constantes vitales se guardan en columnas propias y no
-- dentro del texto del examen fisico porque su valor esta en
-- poder compararlas entre consultas: una temperatura o un peso
-- solo significan algo frente a los de la vez anterior. Eso
-- exige que sean dato, no prosa.
-- ------------------------------------------------------------

ALTER TABLE historial_clinico
    ADD COLUMN anamnesis              VARCHAR(1000) NULL AFTER motivo,
    ADD COLUMN temperatura_c          DOUBLE        NULL AFTER anamnesis,
    ADD COLUMN frecuencia_cardiaca    INT           NULL AFTER temperatura_c,
    ADD COLUMN frecuencia_respiratoria INT          NULL AFTER frecuencia_cardiaca,
    ADD COLUMN condicion_corporal     INT           NULL AFTER peso_kg,
    ADD COLUMN examen_fisico          VARCHAR(1000) NULL AFTER condicion_corporal,
    ADD COLUMN medicamentos           VARCHAR(500)  NULL AFTER tratamiento,
    ADD COLUMN desparasitacion        VARCHAR(255)  NULL AFTER vacunas_aplicadas,
    ADD COLUMN proximo_control        DATE          NULL AFTER desparasitacion;

-- Rangos plausibles. Son amplios a proposito: no les corresponde
-- decidir si un valor es normal para la especie, solo impedir que
-- un error de digitacion entre a la historia como dato clinico.
ALTER TABLE historial_clinico
    ADD CONSTRAINT chk_historial_temperatura
        CHECK (temperatura_c IS NULL OR (temperatura_c BETWEEN 25 AND 45)),
    ADD CONSTRAINT chk_historial_fc
        CHECK (frecuencia_cardiaca IS NULL OR (frecuencia_cardiaca BETWEEN 10 AND 400)),
    ADD CONSTRAINT chk_historial_fr
        CHECK (frecuencia_respiratoria IS NULL OR (frecuencia_respiratoria BETWEEN 5 AND 200)),
    ADD CONSTRAINT chk_historial_condicion
        CHECK (condicion_corporal IS NULL OR (condicion_corporal BETWEEN 1 AND 5));

-- ------------------------------------------------------------
-- Vista de apoyo: proximos controles pendientes.
-- Permite a la clinica saber a quien hay que llamar.
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_controles_pendientes AS
SELECT h.proximo_control,
       m.id                                   AS mascota_id,
       m.nombre                               AS mascota,
       CONCAT(d.nombres, ' ', d.apellidos)    AS dueno,
       d.telefono,
       h.motivo                               AS motivo_consulta_previa
  FROM historial_clinico h
  JOIN mascota m ON m.id = h.mascota_id
  JOIN dueno   d ON d.id = m.dueno_id
 WHERE h.proximo_control IS NOT NULL
   AND h.proximo_control >= CURDATE()
   AND m.activo = 1
 ORDER BY h.proximo_control ASC;

-- ------------------------------------------------------------
-- Se completa el registro de ejemplo con el formato nuevo
-- ------------------------------------------------------------
UPDATE historial_clinico
   -- Se usa CONCAT y no el operador de tuberia: en MySQL y MariaDB
   -- ese operador es un OR logico, no una concatenacion de texto.
   SET anamnesis = CONCAT('El propietario refiere que el paciente se encuentra activo, ',
                          'con apetito normal y sin cambios de comportamiento. ',
                          'Asiste a control anual de rutina.'),
       temperatura_c = 38.4,
       frecuencia_cardiaca = 92,
       frecuencia_respiratoria = 24,
       condicion_corporal = 3,
       examen_fisico = CONCAT('Mucosas rosadas y humedas, tiempo de llenado capilar menor ',
                              'a dos segundos. Auscultacion cardiopulmonar sin hallazgos. ',
                              'Palpacion abdominal no dolorosa. Piel y pelaje en buen estado. ',
                              'Dentadura con sarro leve.'),
       medicamentos = 'No se prescriben medicamentos',
       desparasitacion = 'Antiparasitario oral de amplio espectro, dosis unica',
       proximo_control = DATE_ADD(DATE(fecha), INTERVAL 6 MONTH)
 WHERE cita_id = 5;

-- ------------------------------------------------------------
-- Verificacion
-- ------------------------------------------------------------
SELECT COUNT(*) AS columnas_historial
  FROM information_schema.columns
 WHERE table_schema = 'vetcare_db' AND table_name = 'historial_clinico';
-- Debe decir 20.

SELECT * FROM v_controles_pendientes;
