-- =====================================================================
--  Control de asistencia - Podologia Loayza
--  03_datos_prueba.sql : 7 sedes, ~98 trabajadoras y dos semanas
-- =====================================================================
--  AVISO IMPORTANTE SOBRE LOS NOMBRES
--  ----------------------------------
--  TODOS los nombres de trabajadoras de este archivo son INVENTADOS, y
--  ademas se generan combinando listas de nombres y apellidos comunes.
--  El repositorio es publico y el video de la sustentacion se sube al
--  aula virtual, asi que ningun nombre real de la empresa entra aqui.
--
--  Las 7 sedes y sus coordenadas SI son las reales: son direcciones de
--  locales comerciales, no datos de personas.
--
--  POR QUE 98 Y NO 12
--  ------------------
--  El cliente dice 10-20 trabajadoras por sede, siete sedes, y mas
--  sedes en camino. Un juego de datos con 12 personas no prueba nada
--  sobre el cambio de turno: las consultas del dashboard y de la
--  exportacion parecen instantaneas con cualquier cosa. Con 14 por
--  sede, lo que se mide es lo que va a pasar en produccion.
--
--  Ejecutar despues de 02_procedimientos.sql:
--      mysql -u root -p asistencia_loayza < 03_datos_prueba.sql
-- =====================================================================

USE asistencia_loayza;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE incidencia;
TRUNCATE TABLE jornada_diaria;
TRUNCATE TABLE marcacion;
TRUNCATE TABLE trabajadora;
TRUNCATE TABLE administrador;
TRUNCATE TABLE sede;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  Sedes
-- ---------------------------------------------------------------------
--  Coordenadas obtenidas de los Plus Codes con tools/pluscode.py.
--  Cada celda del codigo mide ~14 m, asi que la precision es de nivel
--  edificio. Las dos sedes mas cercanas -San Borja y Miraflores- estan
--  a 3,3 km, de sobra para que una futura validacion por GPS no tenga
--  ninguna ambiguedad.
--
--  Los ids van explicitos aqui, pero la columna es autoincremental: la
--  octava sede sera un INSERT, no una migracion.
-- =====================================================================
INSERT INTO sede (id, nombre, plus_code, latitud, longitud) VALUES
  (1, 'Los Olivos',  '57V42W5M+M4', -11.990812, -77.067188),
  (2, 'La Molina',   '57V4W2JR+RG', -12.067937, -76.958687),
  (3, 'San Borja',   '57V4VXRX+RG', -12.107938, -77.001188),
  (4, 'Lince',       '57V4WX87+CM', -12.083937, -77.035812),
  (5, 'San Miguel',  '57V4WWF4+47', -12.077187, -77.094313),
  (6, 'Surco',       '57V4R2X6+FC', -12.151312, -76.988937),
  (7, 'Miraflores',  '57V4VXHC+JF', -12.120938, -77.028813);

-- =====================================================================
--  Administradora
-- ---------------------------------------------------------------------
--  Una sola cuenta para las 7 sedes, por decision del usuario.
--
--  El hash es BCrypt real, coste 10, de la contrasena "loayza2026".
--  Sirve para probar el login en desarrollo y NO se sube al servidor:
--  alli se genera otro y esta fila se reemplaza. Aqui esta en claro en
--  un comentario justamente porque es de juguete; la del cliente nunca
--  se escribe en ningun archivo.
-- =====================================================================
INSERT INTO administrador (id, usuario, clave, nombre) VALUES
  (1, 'admin', '$2b$10$373dxSyXqfwtP5C.uoC8A./dvVF/bkQKmv4iu4p2tN9iA3cWf8v3W', 'Administracion Loayza');

-- =====================================================================
--  Plantilla: 98 trabajadoras  (NOMBRES FICTICIOS, 14 por sede)
-- ---------------------------------------------------------------------
--  NO HAY COLUMNA DE CARGO NI DE TIPO DE JORNADA. El cliente lo quito:
--  el sistema registra horas, y ni el puesto ni el contrato cambian una
--  sola de las horas registradas.
--
--  Se generan cruzando tres listas y ordenando por MD5, que es
--  deterministico: el mismo juego de datos sale igual en la maquina de
--  las dos y en el servidor. Se incluyen tildes y enies a proposito,
--  para que un problema de codificacion se note en la primera prueba y
--  no el dia de la sustentacion.
-- =====================================================================
INSERT INTO trabajadora (nombre, documento, sede_habitual)
SELECT x.nombre,
       LPAD(10200000 + x.rn, 8, '0'),
       ((x.rn - 1) % 7) + 1                       -- reparte 14 por sede
  FROM (
        SELECT CONCAT(n.v, ' ', a.v, ' ', b.v) AS nombre,
               ROW_NUMBER() OVER (ORDER BY MD5(CONCAT(n.v, a.v, b.v))) AS rn
          FROM (SELECT 'Milagros' v UNION ALL SELECT 'Rocío'   UNION ALL SELECT 'Liliana'
                UNION ALL SELECT 'Jessica'    UNION ALL SELECT 'Yesenia' UNION ALL SELECT 'Carmen'
                UNION ALL SELECT 'Gladys'     UNION ALL SELECT 'Verónica' UNION ALL SELECT 'Doris'
                UNION ALL SELECT 'Elizabeth'  UNION ALL SELECT 'Norma'   UNION ALL SELECT 'Patricia'
                UNION ALL SELECT 'Sandra'     UNION ALL SELECT 'Marisol') n
          CROSS JOIN
               (SELECT 'Quispe' v  UNION ALL SELECT 'Hancco'  UNION ALL SELECT 'Ñahui'
                UNION ALL SELECT 'Farfán'     UNION ALL SELECT 'Huamán'  UNION ALL SELECT 'Ipanaqué'
                UNION ALL SELECT 'Pariona'    UNION ALL SELECT 'Anccasi' UNION ALL SELECT 'Sulca'
                UNION ALL SELECT 'Ramos'      UNION ALL SELECT 'Quintanilla' UNION ALL SELECT 'Laura') a
          CROSS JOIN
               (SELECT 'Ayala' v   UNION ALL SELECT 'Mamani'  UNION ALL SELECT 'Roldán'
                UNION ALL SELECT 'Ccopa'      UNION ALL SELECT 'Tito'    UNION ALL SELECT 'Vílchez'
                UNION ALL SELECT 'Cárdenas'   UNION ALL SELECT 'Puma'    UNION ALL SELECT 'Bendezú'
                UNION ALL SELECT 'Yupanqui'   UNION ALL SELECT 'Béjar'   UNION ALL SELECT 'Huaraca') b
       ) x
 WHERE x.rn <= 98;

-- La provisional: se registro escribiendo su nombre porque no estaba en
-- la lista. Marca igual que las demas, pero NO sale en el cronograma
-- exportado hasta que la administradora la confirme o la fusione con
-- alguien que ya existe.
INSERT INTO trabajadora (nombre, sede_habitual, provisional) VALUES
  ('Sandra Ccahuana', 4, TRUE);

-- =====================================================================
--  Dos semanas de marcaciones
-- ---------------------------------------------------------------------
--  POR QUE EL PROCEDIMIENTO ESCRIBE TAMBIEN jornada_diaria:
--  la regla de emparejar entradas con salidas vive en Java y solo ahi.
--  Este semillero NO la reimplementa: construye la celda a partir de
--  las MISMAS variables con las que construye las marcaciones. Sabe que
--  puso una entrada a las 9:03 y una salida a la 1:00 porque acaba de
--  ponerlas, no porque las haya vuelto a emparejar.
--
--  EL TURNO DE CADA UNA no sale de ninguna columna, porque ya no hay
--  columna que lo diga: es una variacion del juego de datos y nada mas.
--  Una de cada tres hace solo tarde. En el dominio real eso lo decide
--  el cuadrante de la sede, que este sistema no conoce ni necesita.
--
--  Las fechas se anclan a CURDATE(), no a fechas fijas: asi el juego de
--  datos sigue siendo "las dos ultimas semanas" el dia que se grabe el
--  video, sin tocar nada.
-- =====================================================================

DROP PROCEDURE IF EXISTS sp_tmp_semilla;

DELIMITER $$
CREATE PROCEDURE sp_tmp_semilla()
BEGIN
    DECLARE v_fin     INT DEFAULT 0;
    DECLARE v_trab    SMALLINT UNSIGNED;
    DECLARE v_sede    TINYINT  UNSIGNED;
    DECLARE v_tarde   INT;
    DECLARE v_lunes   DATE;
    DECLARE v_fecha   DATE;
    DECLARE v_dia     INT;
    DECLARE v_e1 DATETIME; DECLARE v_s1 DATETIME;
    DECLARE v_e2 DATETIME; DECLARE v_s2 DATETIME;
    DECLARE v_min INT; DECLARE v_turnos INT; DECLARE v_etq VARCHAR(400);

    DECLARE cur CURSOR FOR
        SELECT id, sede_habitual, (id % 3 = 0)
          FROM trabajadora
         WHERE provisional = FALSE
         ORDER BY id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fin = 1;

    -- El lunes de hace dos semanas. WEEKDAY() devuelve 0 para lunes,
    -- asi que esto cae en lunes cualquiera que sea el dia de hoy.
    SET v_lunes = CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY - INTERVAL 14 DAY;

    OPEN cur;
    bucle_trabajadoras: LOOP
        FETCH cur INTO v_trab, v_sede, v_tarde;
        IF v_fin = 1 THEN LEAVE bucle_trabajadoras; END IF;

        SET v_dia = 0;
        WHILE v_dia < 12 DO
            -- 12 dias habiles = dos semanas de lunes a sabado.
            -- El domingo se salta: los locales no abren.
            SET v_fecha = v_lunes + INTERVAL (FLOOR(v_dia / 6) * 7 + (v_dia MOD 6)) DAY;

            IF v_tarde = 1 THEN
                -- Un solo turno, de tarde.
                SET v_e1 = TIMESTAMP(v_fecha, '15:00:00') + INTERVAL FLOOR(RAND() * 9) MINUTE;
                SET v_s1 = TIMESTAMP(v_fecha, '20:00:00') + INTERVAL FLOOR(RAND() * 11) MINUTE;
                SET v_e2 = NULL; SET v_s2 = NULL;
                SET v_turnos = 1;
                SET v_min = TIMESTAMPDIFF(MINUTE, v_e1, v_s1);
                SET v_etq = CONCAT(DATE_FORMAT(v_e1, '%l:%i%p'), ' - ', DATE_FORMAT(v_s1, '%l:%i%p'));
            ELSE
                -- Dos turnos con el corte del almuerzo. Los minutos se
                -- suman turno a turno, nunca restando los extremos:
                -- 9-1 y 2-6 son 8 horas, no 9.
                SET v_e1 = TIMESTAMP(v_fecha, '09:00:00') + INTERVAL FLOOR(RAND() * 9) MINUTE;
                SET v_s1 = TIMESTAMP(v_fecha, '13:00:00') + INTERVAL FLOOR(RAND() * 7) MINUTE;
                SET v_e2 = TIMESTAMP(v_fecha, '14:00:00') + INTERVAL FLOOR(RAND() * 9) MINUTE;
                SET v_s2 = TIMESTAMP(v_fecha, '18:00:00') + INTERVAL FLOOR(RAND() * 13) MINUTE;
                SET v_turnos = 2;
                SET v_min = TIMESTAMPDIFF(MINUTE, v_e1, v_s1) + TIMESTAMPDIFF(MINUTE, v_e2, v_s2);
                -- Un par por linea: es literalmente el texto de la celda
                -- del Excel, con su salto de linea dentro.
                SET v_etq = CONCAT(
                    DATE_FORMAT(v_e1, '%l:%i%p'), ' - ', DATE_FORMAT(v_s1, '%l:%i%p'), '\n',
                    DATE_FORMAT(v_e2, '%l:%i%p'), ' - ', DATE_FORMAT(v_s2, '%l:%i%p'));
            END IF;

            INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, foto_ruta, foto_mini_ruta)
            VALUES (v_trab, v_sede, 'ENTRADA', v_e1,
                    CONCAT('fotos/', v_trab, '-', UNIX_TIMESTAMP(v_e1), '.jpg'),
                    CONCAT('mini/',  v_trab, '-', UNIX_TIMESTAMP(v_e1), '.jpg')),
                   (v_trab, v_sede, 'SALIDA',  v_s1,
                    CONCAT('fotos/', v_trab, '-', UNIX_TIMESTAMP(v_s1), '.jpg'),
                    CONCAT('mini/',  v_trab, '-', UNIX_TIMESTAMP(v_s1), '.jpg'));

            IF v_e2 IS NOT NULL THEN
                INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, foto_ruta, foto_mini_ruta)
                VALUES (v_trab, v_sede, 'ENTRADA', v_e2,
                        CONCAT('fotos/', v_trab, '-', UNIX_TIMESTAMP(v_e2), '.jpg'),
                        CONCAT('mini/',  v_trab, '-', UNIX_TIMESTAMP(v_e2), '.jpg')),
                       (v_trab, v_sede, 'SALIDA',  v_s2,
                        CONCAT('fotos/', v_trab, '-', UNIX_TIMESTAMP(v_s2), '.jpg'),
                        CONCAT('mini/',  v_trab, '-', UNIX_TIMESTAMP(v_s2), '.jpg'));
            END IF;

            INSERT INTO jornada_diaria
                (sede_id, fecha, trabajadora_id, primera_entrada, ultima_salida, turnos, minutos, etiqueta)
            VALUES (v_sede, v_fecha, v_trab, TIME(v_e1), TIME(IFNULL(v_s2, v_s1)), v_turnos, v_min, v_etq);

            SET v_dia = v_dia + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL sp_tmp_semilla();
DROP PROCEDURE sp_tmp_semilla;

-- =====================================================================
--  Los casos raros
-- ---------------------------------------------------------------------
--  Un juego de datos donde todo cuadra no prueba nada. Estos seis casos
--  son los que el dashboard tiene que saber pintar, y son justamente
--  los que se ensenan en el video.
--
--  Fijarse en que NINGUNO de ellos es un rechazo: los seis se
--  guardaron. Esa es toda la tesis de ADR-012.
-- =====================================================================

SET @sab = CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY - INTERVAL 2 DAY;  -- sabado pasado
SET @vie = @sab - INTERVAL 1 DAY;
SET @jue = @sab - INTERVAL 2 DAY;

-- ---------------------------------------------------------------------
--  1. Olvido de salida.  La trabajadora 3 entro el sabado y no cerro.
--     La celda queda "3:0XPM -", con el hueco A LA VISTA: el sistema no
--     inventa la hora que falta. Asi lo pidio el usuario.
-- ---------------------------------------------------------------------
DELETE FROM marcacion
 WHERE trabajadora_id = 3 AND fecha = @sab AND tipo = 'SALIDA';

UPDATE jornada_diaria j
   SET j.ultima_salida = NULL,
       j.turnos        = 0,
       j.minutos       = 0,
       j.etiqueta      = CONCAT(DATE_FORMAT(j.primera_entrada, '%l:%i%p'), ' -')
 WHERE j.trabajadora_id = 3 AND j.fecha = @sab;

-- ---------------------------------------------------------------------
--  2. Misma persona, dos sedes el mismo dia.  La trabajadora 1 cubrio
--     una tarde en Miraflores ademas de su turno habitual.
--     Ninguna regla del sistema puede asumir una entrada y una salida
--     por dia: este caso es el que lo demuestra.
-- ---------------------------------------------------------------------
INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, notas, foto_ruta, foto_mini_ruta) VALUES
  (1, 7, 'ENTRADA', TIMESTAMP(@vie, '19:05:00'), 'Apoyo en Miraflores', 'fotos/1-apoyo-e.jpg', 'mini/1-apoyo-e.jpg'),
  (1, 7, 'SALIDA',  TIMESTAMP(@vie, '21:40:00'), NULL,                  'fotos/1-apoyo-s.jpg', 'mini/1-apoyo-s.jpg');

INSERT INTO jornada_diaria
    (sede_id, fecha, trabajadora_id, primera_entrada, ultima_salida, turnos, minutos, etiqueta)
VALUES (7, @vie, 1, '19:05:00', '21:40:00', 1, 155, '7:05PM - 9:40PM');

-- ---------------------------------------------------------------------
--  3. Marcacion encolada sin senal, ACEPTADA.  La trabajadora 5 marco
--     el jueves con el telefono sin datos; llego 38 minutos despues.
--     momento es lo que dijo el telefono, momento_recibido es cuando
--     llego de verdad. El sistema nunca afirma una hora que no puede
--     probar: si la puso el dispositivo, lo dice, y la incidencia queda
--     anotada para que una persona la mire.
-- ---------------------------------------------------------------------
UPDATE marcacion
   SET fuente_hora      = 'DISPOSITIVO',
       momento_recibido = momento + INTERVAL 38 MINUTE,
       notas            = 'Se envio sin senal, quedo en cola'
 WHERE trabajadora_id = 5 AND fecha = @jue AND tipo = 'ENTRADA'
 ORDER BY momento LIMIT 1;

INSERT INTO incidencia (momento, marcacion_id, trabajadora_id, sede_id, tipo, detalle)
SELECT m.momento_recibido, m.id, m.trabajadora_id, m.sede_id, 'HORA_DEL_TELEFONO',
       'La hora la puso el telefono; llego 38 min despues'
  FROM marcacion m
 WHERE m.trabajadora_id = 5 AND m.fecha = @jue AND m.fuente_hora = 'DISPOSITIVO';

-- ---------------------------------------------------------------------
--  4. La provisional.  Escribio su nombre porque no estaba en la lista
--     y marco normalmente. Aparece en el dashboard y en
--     sp_provisionales, pero NO en el cronograma exportado.
-- ---------------------------------------------------------------------
SET @prov = (SELECT id FROM trabajadora WHERE provisional = TRUE LIMIT 1);

INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, notas, foto_ruta, foto_mini_ruta) VALUES
  (@prov, 4, 'ENTRADA', TIMESTAMP(@sab, '08:58:00'), 'No aparecia en la lista', 'fotos/p-e.jpg', 'mini/p-e.jpg'),
  (@prov, 4, 'SALIDA',  TIMESTAMP(@sab, '14:12:00'), NULL,                      'fotos/p-s.jpg', 'mini/p-s.jpg');

INSERT INTO jornada_diaria
    (sede_id, fecha, trabajadora_id, primera_entrada, ultima_salida, turnos, minutos, etiqueta)
VALUES (4, @sab, @prov, '08:58:00', '14:12:00', 1, 314, '8:58AM - 2:12PM');

-- ---------------------------------------------------------------------
--  5. Correccion de la administradora.  La trabajadora 7 olvido marcar
--     la salida del jueves y se puso a mano.
--     Es una marcacion NUEVA, no una edicion: el hecho sigue siendo
--     inmutable y queda escrito quien lo puso y por que. Sin foto,
--     porque una correccion manual no puede tenerla, y en el dashboard
--     las dos cosas se distinguen a simple vista.
-- ---------------------------------------------------------------------
UPDATE marcacion
   SET origen         = 'ADMINISTRADORA',
       corregida_por  = 1,
       motivo         = 'Olvido de salida confirmado por telefono con la sede',
       foto_ruta      = NULL,
       foto_mini_ruta = NULL
 WHERE trabajadora_id = 7 AND fecha = @jue AND tipo = 'SALIDA'
 ORDER BY momento DESC LIMIT 1;

-- ---------------------------------------------------------------------
--  6. Marcaciones ACEPTADAS con algo raro.  Ninguna se rechazo: las
--     cuatro estan en marcacion y ademas tienen su incidencia.
--
--     Este bloque es el que hay que ensenar en la sustentacion. Antes
--     estas cuatro filas eran rechazos y la marcacion se perdia; ahora
--     la hora trabajada se guarda siempre y lo unico que se pierde, a
--     lo sumo, es la foto.
-- ---------------------------------------------------------------------

-- 6a. Doble envio del mismo segundo: el boton se pulso dos veces.
--     Se guardan LAS DOS. Decidir cual sobra es decidir por ella.
INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, foto_ruta, foto_mini_ruta)
VALUES (11, 4, 'ENTRADA', TIMESTAMP(@vie, '09:02:00'), 'fotos/11-d1.jpg', 'mini/11-d1.jpg');
SET @m6a = LAST_INSERT_ID();
INSERT INTO incidencia (momento, marcacion_id, ip, trabajadora_id, sede_id, tipo, detalle)
VALUES (TIMESTAMP(@vie, '09:02:00'), @m6a, INET6_ATON('190.237.12.44'), 11, 4,
        'ENVIO_DUPLICADO', 'Segundo envio identico a los 4 s');

-- 6b. La foto no se pudo guardar. La MARCACION SI.
INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, foto_ruta, foto_mini_ruta)
VALUES (13, 6, 'ENTRADA', TIMESTAMP(@sab, '09:01:00'), NULL, NULL);
SET @m6b = LAST_INSERT_ID();
INSERT INTO incidencia (momento, marcacion_id, ip, trabajadora_id, sede_id, tipo, detalle)
VALUES (TIMESTAMP(@sab, '09:01:00'), @m6b, INET6_ATON('181.65.200.7'), 13, 6,
        'FOTO_NO_GUARDADA', 'La camara devolvio un archivo vacio');

-- 6c. Foto con dimensiones absurdas: se descarto la FOTO, no la hora.
INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, foto_ruta, foto_mini_ruta)
VALUES (17, 5, 'SALIDA', TIMESTAMP(@sab, '16:40:00'), NULL, NULL);
SET @m6c = LAST_INSERT_ID();
INSERT INTO incidencia (momento, marcacion_id, ip, trabajadora_id, sede_id, tipo, detalle)
VALUES (TIMESTAMP(@sab, '16:40:00'), @m6c, INET6_ATON('2001:1388:5::9'), 17, 5,
        'FOTO_SOSPECHOSA', '9000x9000 px; se guardo la marcacion sin foto');

-- 6d. Encolada tres horas. ANTES esto se rechazaba por COLA_VENCIDA.
--     Ahora entra, se marca como hora del telefono y se ensena el
--     desfase: que decida una persona, no un umbral.
INSERT INTO marcacion (trabajadora_id, sede_id, tipo, momento, fuente_hora, momento_recibido, foto_ruta, foto_mini_ruta)
VALUES (23, 1, 'SALIDA', TIMESTAMP(@sab, '14:30:00'), 'DISPOSITIVO', TIMESTAMP(@sab, '17:50:00'),
        'fotos/23-cola.jpg', 'mini/23-cola.jpg');
SET @m6d = LAST_INSERT_ID();
INSERT INTO incidencia (momento, marcacion_id, ip, trabajadora_id, sede_id, tipo, detalle)
VALUES (TIMESTAMP(@sab, '17:50:00'), @m6d, INET6_ATON('38.25.14.90'), 23, 1,
        'COLA_MUY_ANTIGUA', 'Estuvo 3 h 20 min en la cola del telefono');

-- =====================================================================
--  Resumen de lo sembrado
-- =====================================================================
SELECT 'sedes'                    AS tabla, COUNT(*) AS filas FROM sede
UNION ALL SELECT 'trabajadoras',           COUNT(*) FROM trabajadora
UNION ALL SELECT '  provisionales',        COUNT(*) FROM trabajadora WHERE provisional
UNION ALL SELECT 'administradores',        COUNT(*) FROM administrador
UNION ALL SELECT 'marcaciones',            COUNT(*) FROM marcacion
UNION ALL SELECT '  con hora del telefono',COUNT(*) FROM marcacion WHERE fuente_hora = 'DISPOSITIVO'
UNION ALL SELECT '  sin foto',             COUNT(*) FROM marcacion WHERE foto_ruta IS NULL
UNION ALL SELECT '  puestas a mano',       COUNT(*) FROM marcacion WHERE origen = 'ADMINISTRADORA'
UNION ALL SELECT 'celdas de cronograma',   COUNT(*) FROM jornada_diaria
UNION ALL SELECT '  con turno abierto',    COUNT(*) FROM jornada_diaria WHERE ultima_salida IS NULL
UNION ALL SELECT 'incidencias',            COUNT(*) FROM incidencia
UNION ALL SELECT 'MARCACIONES RECHAZADAS', 0;
