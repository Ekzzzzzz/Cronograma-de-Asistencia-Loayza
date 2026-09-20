-- =====================================================================
--  Control de asistencia - Podologia Loayza
--  02_procedimientos.sql : procedimientos almacenados
-- =====================================================================
--  RC-03 los exige, y aqui tienen trabajo real, no ceremonial.
--
--  DOS REGLAS QUE ORGANIZAN TODO ESTE ARCHIVO
--
--  1. NINGUN PROCEDIMIENTO PUEDE RECHAZAR UNA MARCACION (ADR-012).
--     Busca en este archivo un SIGNAL SQLSTATE, un CHECK o un IF que
--     impida un INSERT: no hay ninguno. Lo raro se guarda y se anota
--     en incidencia para que una persona lo mire.
--
--  2. NINGUN PROCEDIMIENTO DECIDE REGLAS DE NEGOCIO.
--     Los procedimientos CUENTAN, MIDEN y GUARDAN; quien decide es la
--     capa modelo, en Java. Por eso sp_contexto_marcacion se llama asi
--     y no sp_validar_marcacion: mide el contexto, no juzga.
--
--  Ejecutar despues de 01_esquema.sql:
--      mysql -u root -p asistencia_loayza < 02_procedimientos.sql
-- =====================================================================

USE asistencia_loayza;

DROP PROCEDURE IF EXISTS sp_contexto_marcacion;
DROP PROCEDURE IF EXISTS sp_registrar_marcacion;
DROP PROCEDURE IF EXISTS sp_actualizar_jornada;
DROP PROCEDURE IF EXISTS sp_registrar_incidencia;
DROP PROCEDURE IF EXISTS sp_incidencias;
DROP PROCEDURE IF EXISTS sp_marcar_incidencia_revisada;
DROP PROCEDURE IF EXISTS sp_registrar_provisional;
DROP PROCEDURE IF EXISTS sp_cronograma_semanal;
DROP PROCEDURE IF EXISTS sp_cronograma_diario;
DROP PROCEDURE IF EXISTS sp_dia_sede;
DROP PROCEDURE IF EXISTS sp_marcaciones_del_dia;
DROP PROCEDURE IF EXISTS sp_historial_trabajadora;
DROP PROCEDURE IF EXISTS sp_totales_horas;
DROP PROCEDURE IF EXISTS sp_provisionales;
DROP PROCEDURE IF EXISTS sp_autenticar_admin;
DROP PROCEDURE IF EXISTS sp_marcar_acceso_admin;
DROP PROCEDURE IF EXISTS sp_purgar_fotos;
-- Nombres viejos, por si quedo una base cargada antes de ADR-012.
DROP PROCEDURE IF EXISTS sp_validar_marcacion;
DROP PROCEDURE IF EXISTS sp_registrar_intento;
DROP PROCEDURE IF EXISTS sp_intentos_rechazados;

DELIMITER $$

-- =====================================================================
--  1. sp_contexto_marcacion
-- ---------------------------------------------------------------------
--  Se llamaba sp_validar_marcacion. Ya no valida nada, asi que tampoco
--  se llama asi: un nombre que miente sobre lo que hace una funcion es
--  la peor clase de documentacion.
--
--  Devuelve dos cosas, y ninguna sirve para rechazar:
--    - marcaciones_hoy : alimenta el contador que ve la trabajadora
--                        («Marcacion 3 de 4 de hoy»).
--    - ultimo_tipo     : para PRESELECCIONAR el boton de entrada o
--                        salida y ahorrarle un toque. Sugiere. Si ella
--                        pulsa el otro, se guarda el otro.
--
--  Es una sola consulta sobre idx_cronograma y se resuelve en el
--  indice. Importa que sea barata: se llama en cada marcacion, y en el
--  cambio de turno pueden llegar muchas a la vez.
-- =====================================================================
CREATE PROCEDURE sp_contexto_marcacion(
    IN p_trabajadora SMALLINT UNSIGNED,
    IN p_sede        TINYINT  UNSIGNED,
    IN p_momento     DATETIME
)
BEGIN
    SELECT
        COUNT(*)                                            AS marcaciones_hoy,
        (SELECT m2.tipo
           FROM marcacion m2
          WHERE m2.trabajadora_id = p_trabajadora
            AND m2.sede_id        = p_sede
            AND m2.fecha          = DATE(p_momento)
          ORDER BY m2.momento DESC, m2.id DESC
          LIMIT 1)                                          AS ultimo_tipo
      FROM marcacion m
     WHERE m.trabajadora_id = p_trabajadora
       AND m.sede_id        = p_sede
       AND m.fecha          = DATE(p_momento);
END$$

-- =====================================================================
--  2. sp_registrar_marcacion
-- ---------------------------------------------------------------------
--  Inserta la marcacion y devuelve, en la MISMA llamada, todas las
--  marcaciones de esa trabajadora en esa sede ese dia.
--
--  ESTE PROCEDIMIENTO NO PUEDE FALLAR por nada que haya hecho la
--  trabajadora. No mira cuantas lleva, no mira si la anterior fue una
--  entrada, no mira si trae foto. Inserta.
--
--  Por que devuelve la lista: emparejar N entradas con N salidas en
--  orden es logica de dominio y vive en Java (Jornada -> Turno, el
--  Composite). Si el emparejamiento se escribiera tambien aqui en SQL,
--  la misma regla existiria en dos lenguajes: Shotgun Surgery. Java
--  recibe la lista, arma los turnos y llama a sp_actualizar_jornada.
--  Las dos llamadas van dentro de la misma transaccion de Spring.
--
--  p_foto_ruta puede llegar NULL y es un caso NORMAL, no un error: si
--  la camara fallo o la imagen vino rota, la marcacion se guarda igual
--  y la incidencia se anota aparte. La hora trabajada no depende de
--  que la foto saliera bien.
-- =====================================================================
CREATE PROCEDURE sp_registrar_marcacion(
    IN  p_trabajadora     SMALLINT UNSIGNED,
    IN  p_sede            TINYINT  UNSIGNED,
    IN  p_tipo            VARCHAR(10),
    IN  p_momento         DATETIME,
    IN  p_fuente_hora     VARCHAR(12),
    IN  p_momento_recibido DATETIME,
    IN  p_notas           VARCHAR(500),
    IN  p_foto_ruta       VARCHAR(200),
    IN  p_foto_mini_ruta  VARCHAR(200),
    IN  p_origen          VARCHAR(16),
    IN  p_corregida_por   SMALLINT UNSIGNED,
    IN  p_motivo          VARCHAR(200),
    OUT p_id              BIGINT UNSIGNED,
    OUT p_marcaciones_hoy INT
)
BEGIN
    INSERT INTO marcacion (
        trabajadora_id, sede_id, tipo, momento,
        fuente_hora, momento_recibido, notas,
        foto_ruta, foto_mini_ruta,
        origen, corregida_por, motivo
    ) VALUES (
        p_trabajadora, p_sede, p_tipo, p_momento,
        IFNULL(p_fuente_hora, 'SERVIDOR'), p_momento_recibido, p_notas,
        p_foto_ruta, p_foto_mini_ruta,
        IFNULL(p_origen, 'TRABAJADORA'), p_corregida_por, p_motivo
    );

    SET p_id = LAST_INSERT_ID();

    SELECT COUNT(*) INTO p_marcaciones_hoy
      FROM marcacion
     WHERE trabajadora_id = p_trabajadora
       AND sede_id        = p_sede
       AND fecha          = DATE(p_momento);

    -- La materia prima del Composite: el dia entero, en orden.
    SELECT id, tipo, momento, fuente_hora, momento_recibido, origen
      FROM marcacion
     WHERE trabajadora_id = p_trabajadora
       AND sede_id        = p_sede
       AND fecha          = DATE(p_momento)
     ORDER BY momento, id;
END$$

-- =====================================================================
--  3. sp_actualizar_jornada
-- ---------------------------------------------------------------------
--  La otra mitad del registro. Java ya emparejo los turnos y trae la
--  celda del Excel resuelta: primera entrada, ultima salida, cuantos
--  turnos, cuantos minutos y el texto literal.
--
--  Es un UPSERT porque la fila se reescribe entera en cada marcacion
--  del dia. No se suma sobre lo anterior: se recalcula desde cero, que
--  es lo unico que sobrevive a una correccion manual a media tarde.
-- =====================================================================
CREATE PROCEDURE sp_actualizar_jornada(
    IN p_sede        TINYINT  UNSIGNED,
    IN p_fecha       DATE,
    IN p_trabajadora SMALLINT UNSIGNED,
    IN p_primera     TIME,
    IN p_ultima      TIME,
    IN p_turnos      TINYINT  UNSIGNED,
    IN p_minutos     SMALLINT UNSIGNED,
    IN p_etiqueta    VARCHAR(400)
)
BEGIN
    INSERT INTO jornada_diaria (
        sede_id, fecha, trabajadora_id,
        primera_entrada, ultima_salida, turnos, minutos, etiqueta
    ) VALUES (
        p_sede, p_fecha, p_trabajadora,
        p_primera, p_ultima, p_turnos, p_minutos, p_etiqueta
    )
    ON DUPLICATE KEY UPDATE
        primera_entrada = VALUES(primera_entrada),
        ultima_salida   = VALUES(ultima_salida),
        turnos          = VALUES(turnos),
        minutos         = VALUES(minutos),
        etiqueta        = VALUES(etiqueta);
END$$

-- =====================================================================
--  4. sp_registrar_incidencia
-- ---------------------------------------------------------------------
--  Anota algo que el sistema acepto y no entendio del todo.
--
--  Se llamaba sp_registrar_intento, cuando existian los rechazos. Ya
--  no rechaza nada, asi que tampoco registra rechazos: registra cosas
--  que merecen una mirada humana.
--
--  p_marcacion apunta a la marcacion que SI se guardo. Es lo que
--  permite que el dashboard ensene la incidencia junto a la hora, y no
--  en una lista aparte que nadie abre.
-- =====================================================================
CREATE PROCEDURE sp_registrar_incidencia(
    IN p_momento     DATETIME,
    IN p_marcacion   BIGINT UNSIGNED,
    IN p_ip          VARCHAR(45),
    IN p_trabajadora SMALLINT UNSIGNED,
    IN p_sede        TINYINT  UNSIGNED,
    IN p_tipo        VARCHAR(24),
    IN p_detalle     VARCHAR(200)
)
BEGIN
    INSERT INTO incidencia (momento, marcacion_id, ip, trabajadora_id, sede_id, tipo, detalle)
    VALUES (p_momento, p_marcacion, INET6_ATON(p_ip), p_trabajadora, p_sede, p_tipo, p_detalle);
END$$

-- =====================================================================
--  5. sp_incidencias
-- ---------------------------------------------------------------------
--  La lista que mira la administradora.
--
--  Es la contrapartida de no rechazar nada: si el sistema deja entrar
--  todo, TIENE que ensenar bien lo que entro. Sin esta pantalla, el
--  sistema aceptaria en silencio y nadie se enteraria de nada.
--
--  p_solo_pendientes = TRUE deja fuera lo que ya se reviso, para que la
--  lista no crezca sin fin y se vuelva un sitio al que nadie entra.
-- =====================================================================
CREATE PROCEDURE sp_incidencias(
    IN p_desde            DATETIME,
    IN p_hasta            DATETIME,
    IN p_solo_pendientes  BOOLEAN
)
BEGIN
    SELECT
        i.id,
        i.momento,
        i.tipo,
        i.detalle,
        i.revisada,
        i.marcacion_id,
        m.tipo             AS marcacion_tipo,
        m.momento          AS marcacion_momento,
        INET6_NTOA(i.ip)   AS ip,
        i.trabajadora_id,
        t.nombre           AS trabajadora,
        i.sede_id,
        s.nombre           AS sede
      FROM incidencia i
      LEFT JOIN marcacion   m ON m.id = i.marcacion_id
      LEFT JOIN trabajadora t ON t.id = i.trabajadora_id
      LEFT JOIN sede        s ON s.id = i.sede_id
     WHERE i.momento BETWEEN p_desde AND p_hasta
       AND (NOT p_solo_pendientes OR i.revisada = FALSE)
     ORDER BY i.momento DESC;
END$$

-- =====================================================================
--  6. sp_marcar_incidencia_revisada
-- =====================================================================
CREATE PROCEDURE sp_marcar_incidencia_revisada(
    IN p_id BIGINT UNSIGNED
)
BEGIN
    UPDATE incidencia SET revisada = TRUE WHERE id = p_id;
END$$

-- =====================================================================
--  7. sp_registrar_provisional
-- ---------------------------------------------------------------------
--  Nadie se queda sin marcar por no estar en la lista. Quien escribe
--  su nombre entra como provisional, marca igual, y despues la
--  administradora la confirma como alta nueva o la fusiona con quien
--  ya existe.
--
--  Si ya hay una provisional activa con ese mismo nombre, se REUSA en
--  vez de crear otra. Sin esto, alguien que marca entrada y salida
--  escribiendo su nombre las dos veces saldria como dos personas
--  distintas, cada una con medio turno.
-- =====================================================================
CREATE PROCEDURE sp_registrar_provisional(
    IN  p_nombre VARCHAR(80),
    IN  p_sede   TINYINT UNSIGNED,
    OUT p_id     SMALLINT UNSIGNED
)
BEGIN
    SET p_id = NULL;

    SELECT id INTO p_id
      FROM trabajadora
     WHERE provisional = TRUE
       AND activa      = TRUE
       AND nombre      = p_nombre
     ORDER BY id
     LIMIT 1;

    IF p_id IS NULL THEN
        INSERT INTO trabajadora (nombre, sede_habitual, provisional)
        VALUES (p_nombre, p_sede, TRUE);
        SET p_id = LAST_INSERT_ID();
    END IF;
END$$

-- =====================================================================
--  8. sp_cronograma_semanal        <-- EXPORTACION SEMANAL A EXCEL
-- ---------------------------------------------------------------------
--  Una sede, siete dias: lunes a domingo.
--
--  Devuelve la REJILLA COMPLETA, no solo los dias con marcaciones: cada
--  trabajadora de la sede por cada uno de los 7 dias, con la celda
--  vacia donde no marco. Es lo que hace que el Excel salga cuadrado sin
--  que Java tenga que rellenar huecos.
--
--  Los 7 dias se generan con una tabla derivada de 0 a 6 en vez de una
--  tabla calendario: siete filas no justifican una tabla.
--
--  Las provisionales quedan FUERA hasta que la administradora las
--  confirme: el cronograma que se entrega no lleva nombres a medio
--  verificar. Si aparecen en el dashboard, que es donde se resuelven.
-- =====================================================================
CREATE PROCEDURE sp_cronograma_semanal(
    IN p_sede  TINYINT UNSIGNED,
    IN p_lunes DATE
)
BEGIN
    SELECT
        t.id                                  AS trabajadora_id,
        t.nombre,
        d.fecha,
        DAYOFWEEK(d.fecha)                    AS dia_semana,
        j.primera_entrada,
        j.ultima_salida,
        IFNULL(j.turnos, 0)                   AS turnos,
        IFNULL(j.minutos, 0)                  AS minutos,
        IFNULL(j.etiqueta, '')                AS etiqueta
      FROM (
            SELECT DISTINCT tr.id, tr.nombre
              FROM trabajadora tr
              LEFT JOIN jornada_diaria jd
                     ON jd.trabajadora_id = tr.id
                    AND jd.sede_id        = p_sede
                    AND jd.fecha BETWEEN p_lunes AND p_lunes + INTERVAL 6 DAY
             WHERE tr.activa      = TRUE
               AND tr.provisional = FALSE
               AND (tr.sede_habitual = p_sede OR jd.trabajadora_id IS NOT NULL)
           ) t
      CROSS JOIN (
            SELECT p_lunes + INTERVAL n DAY AS fecha
              FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2
                    UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6) dias
           ) d
      LEFT JOIN jornada_diaria j
             ON j.sede_id        = p_sede
            AND j.fecha          = d.fecha
            AND j.trabajadora_id = t.id
     ORDER BY t.nombre, d.fecha;
END$$

-- =====================================================================
--  9. sp_cronograma_diario         <-- EXPORTACION DIARIA A EXCEL
-- ---------------------------------------------------------------------
--  Una sede, un dia. Devuelve las MISMAS COLUMNAS que la semanal, a
--  proposito: asi el generador de Excel es el mismo codigo y lo unico
--  que cambia es cuantas columnas de fecha pinta. Dos exportaciones,
--  un solo generador que mantener.
-- =====================================================================
CREATE PROCEDURE sp_cronograma_diario(
    IN p_sede  TINYINT UNSIGNED,
    IN p_fecha DATE
)
BEGIN
    SELECT
        t.id                   AS trabajadora_id,
        t.nombre,
        p_fecha                AS fecha,
        DAYOFWEEK(p_fecha)     AS dia_semana,
        j.primera_entrada,
        j.ultima_salida,
        IFNULL(j.turnos, 0)    AS turnos,
        IFNULL(j.minutos, 0)   AS minutos,
        IFNULL(j.etiqueta, '') AS etiqueta
      FROM trabajadora t
      LEFT JOIN jornada_diaria j
             ON j.trabajadora_id = t.id
            AND j.sede_id        = p_sede
            AND j.fecha          = p_fecha
     WHERE t.activa      = TRUE
       AND t.provisional = FALSE
       AND (t.sede_habitual = p_sede OR j.trabajadora_id IS NOT NULL)
     ORDER BY t.nombre;
END$$

-- =====================================================================
--  10. sp_dia_sede
-- ---------------------------------------------------------------------
--  La vista diaria del dashboard: cada trabajadora de una sede con
--  TODOS sus turnos de ese dia, y a la vista lo que no cuadra.
--
--  A diferencia del cronograma, aqui SI entran las provisionales: el
--  dashboard es justo el sitio donde la administradora las descubre.
-- =====================================================================
CREATE PROCEDURE sp_dia_sede(
    IN p_sede  TINYINT UNSIGNED,
    IN p_fecha DATE
)
BEGIN
    SELECT
        t.id                   AS trabajadora_id,
        t.nombre,
        t.provisional,
        j.primera_entrada,
        j.ultima_salida,
        IFNULL(j.turnos, 0)    AS turnos,
        IFNULL(j.minutos, 0)   AS minutos,
        IFNULL(j.etiqueta, '') AS etiqueta,
        -- Turno abierto: sigue dentro del local.
        --
        -- Esto decia (primera_entrada IS NOT NULL AND ultima_salida IS
        -- NULL), y estaba MAL para el caso central de este sistema.
        -- Quien hace 9-1, 2-6 y vuelve a entrar a las 7 tiene ultima
        -- salida a las 6 y esta dentro; la regla vieja decia que se
        -- habia ido. La regla correcta es: la ULTIMA marcacion del dia
        -- fue una entrada. Lo destapo JornadaTest.abiertoConSalidaPrevia.
        (SELECT m.tipo
           FROM marcacion m
          WHERE m.trabajadora_id = t.id
            AND m.sede_id        = p_sede
            AND m.fecha          = p_fecha
          ORDER BY m.momento DESC, m.id DESC
          LIMIT 1) = 'ENTRADA'                               AS turno_abierto,
        -- Cuantas de sus marcaciones de hoy traen hora del telefono.
        -- La advertencia se muestra SIEMPRE que exista desfase.
        (SELECT COUNT(*)
           FROM marcacion m
          WHERE m.trabajadora_id = t.id
            AND m.sede_id        = p_sede
            AND m.fecha          = p_fecha
            AND m.fuente_hora    = 'DISPOSITIVO')            AS marcaciones_con_hora_del_telefono,
        -- El desfase mas grande del dia, en minutos.
        (SELECT MAX(ABS(TIMESTAMPDIFF(MINUTE, m.momento, m.momento_recibido)))
           FROM marcacion m
          WHERE m.trabajadora_id = t.id
            AND m.sede_id        = p_sede
            AND m.fecha          = p_fecha
            AND m.momento_recibido IS NOT NULL)              AS desfase_maximo_minutos,
        -- Incidencias sin revisar de ese dia. Ahora que nada se
        -- rechaza, este numero es lo que hace el sistema vigilable.
        (SELECT COUNT(*)
           FROM incidencia i
          WHERE i.trabajadora_id = t.id
            AND i.sede_id        = p_sede
            AND DATE(i.momento)  = p_fecha
            AND i.revisada       = FALSE)                    AS incidencias_pendientes
      FROM jornada_diaria j
      JOIN trabajadora t ON t.id = j.trabajadora_id
     WHERE j.sede_id = p_sede
       AND j.fecha   = p_fecha
     ORDER BY j.primera_entrada IS NULL, j.primera_entrada, t.nombre;
END$$

-- =====================================================================
--  11. sp_marcaciones_del_dia
-- ---------------------------------------------------------------------
--  El listado crudo del dashboard, marcacion por marcacion, con la
--  MINIATURA y no la foto completa: una pantalla con cien fotos a
--  tamano real no la abre nadie dos veces.
--
--  p_sede admite NULL para ver todas las sedes de golpe.
-- =====================================================================
CREATE PROCEDURE sp_marcaciones_del_dia(
    IN p_sede  TINYINT UNSIGNED,
    IN p_fecha DATE
)
BEGIN
    SELECT
        m.id,
        m.trabajadora_id,
        t.nombre                AS trabajadora,
        t.provisional,
        m.sede_id,
        s.nombre                AS sede,
        m.tipo,
        m.momento,
        m.fuente_hora,
        m.momento_recibido,
        TIMESTAMPDIFF(MINUTE, m.momento, m.momento_recibido) AS desfase_minutos,
        m.notas,
        m.foto_mini_ruta,
        m.foto_purgada,
        (m.foto_ruta IS NULL)   AS sin_foto,
        m.origen,
        a.nombre                AS corregida_por,
        m.motivo,
        (SELECT COUNT(*) FROM incidencia i WHERE i.marcacion_id = m.id) AS incidencias
      FROM marcacion m
      JOIN trabajadora t  ON t.id = m.trabajadora_id
      JOIN sede s         ON s.id = m.sede_id
      LEFT JOIN administrador a ON a.id = m.corregida_por
     WHERE m.fecha = p_fecha
       AND (p_sede IS NULL OR m.sede_id = p_sede)
     ORDER BY m.momento, m.id;
END$$

-- =====================================================================
--  12. sp_historial_trabajadora
-- ---------------------------------------------------------------------
--  Todas las marcaciones de una persona en un rango, CRUZANDO TODAS
--  LAS SEDES. Es la consulta que contesta "donde estuvo esta semana", y
--  la unica del sistema que no filtra por sede: por eso marcacion tiene
--  el indice idx_trabajadora aparte del del cronograma.
-- =====================================================================
CREATE PROCEDURE sp_historial_trabajadora(
    IN p_trabajadora SMALLINT UNSIGNED,
    IN p_desde       DATE,
    IN p_hasta       DATE
)
BEGIN
    SELECT
        m.id,
        m.fecha,
        m.sede_id,
        s.nombre  AS sede,
        m.tipo,
        m.momento,
        m.fuente_hora,
        m.momento_recibido,
        m.origen,
        m.notas,
        m.foto_mini_ruta,
        m.foto_purgada
      FROM marcacion m
      JOIN sede s ON s.id = m.sede_id
     WHERE m.trabajadora_id = p_trabajadora
       AND m.fecha BETWEEN p_desde AND p_hasta
     ORDER BY m.momento, m.id;
END$$

-- =====================================================================
--  13. sp_totales_horas
-- ---------------------------------------------------------------------
--  Suma de minutos por trabajadora y sede en un rango.
--
--  Suma jornada_diaria.minutos, que ya viene turno a turno. NUNCA
--  restar ultima_salida menos primera_entrada: una jornada de 9-1 y 2-6
--  daria 9 horas en vez de las 8 reales.
--
--  p_sede admite NULL para totalizar todas las sedes.
-- =====================================================================
CREATE PROCEDURE sp_totales_horas(
    IN p_sede  TINYINT UNSIGNED,
    IN p_desde DATE,
    IN p_hasta DATE
)
BEGIN
    SELECT
        t.id            AS trabajadora_id,
        t.nombre,
        j.sede_id,
        s.nombre        AS sede,
        COUNT(*)        AS dias_trabajados,
        SUM(j.turnos)   AS turnos,
        SUM(j.minutos)  AS minutos,
        ROUND(SUM(j.minutos) / 60, 2) AS horas
      FROM jornada_diaria j
      JOIN trabajadora t ON t.id = j.trabajadora_id
      JOIN sede s        ON s.id = j.sede_id
     WHERE j.fecha BETWEEN p_desde AND p_hasta
       AND (p_sede IS NULL OR j.sede_id = p_sede)
     GROUP BY t.id, t.nombre, j.sede_id, s.nombre
     ORDER BY t.nombre, s.nombre;
END$$

-- =====================================================================
--  14. sp_provisionales
-- ---------------------------------------------------------------------
--  Las que se registraron escribiendo su nombre y esperan confirmacion.
--  Trae con cuantas marcaciones y donde, que es lo que la
--  administradora necesita para decidir si es un alta nueva o una que
--  ya existe mal escrita.
-- =====================================================================
CREATE PROCEDURE sp_provisionales()
BEGIN
    SELECT
        t.id,
        t.nombre,
        t.creada_en,
        s.nombre                 AS sede_habitual,
        COUNT(m.id)              AS marcaciones,
        MIN(m.momento)           AS primera_marcacion,
        MAX(m.momento)           AS ultima_marcacion
      FROM trabajadora t
      LEFT JOIN sede s      ON s.id = t.sede_habitual
      LEFT JOIN marcacion m ON m.trabajadora_id = t.id
     WHERE t.provisional = TRUE
       AND t.activa      = TRUE
     GROUP BY t.id, t.nombre, t.creada_en, s.nombre
     ORDER BY t.creada_en DESC;
END$$

-- =====================================================================
--  15. sp_autenticar_admin
-- ---------------------------------------------------------------------
--  DEVUELVE EL HASH, NO COMPARA CONTRASENAS. La verificacion BCrypt se
--  hace en Java, a proposito: un procedimiento que recibiera la
--  contrasena en claro la dejaria escrita en el log de consultas del
--  motor, y ahi ya no la borra nadie.
-- =====================================================================
CREATE PROCEDURE sp_autenticar_admin(
    IN p_usuario VARCHAR(40)
)
BEGIN
    SELECT id, usuario, clave, nombre, activo, ultimo_acceso
      FROM administrador
     WHERE usuario = p_usuario;
END$$

-- =====================================================================
--  16. sp_marcar_acceso_admin
-- ---------------------------------------------------------------------
--  Se llama DESPUES de que Java verifico el hash, nunca antes. Si se
--  llamara antes, un intento fallido dejaria escrito un acceso que no
--  ocurrio.
-- =====================================================================
CREATE PROCEDURE sp_marcar_acceso_admin(
    IN p_id SMALLINT UNSIGNED
)
BEGIN
    UPDATE administrador
       SET ultimo_acceso = NOW()
     WHERE id = p_id;
END$$

-- =====================================================================
--  17. sp_purgar_fotos
-- ---------------------------------------------------------------------
--  Marca como purgado lo que paso los 3 meses. NO borra la fila ni la
--  miniatura: la marcacion sigue siendo evidencia de jornada laboral y
--  la miniatura sigue probando que hubo foto.
--
--  Solo mueve la bandera en la base. El borrado del blob real lo hace
--  Java leyendo lo que esto marco, porque el archivo no vive en MySQL.
-- =====================================================================
CREATE PROCEDURE sp_purgar_fotos(
    IN  p_dias       SMALLINT UNSIGNED,
    OUT p_afectadas  INT
)
BEGIN
    UPDATE marcacion
       SET foto_purgada = TRUE
     WHERE foto_purgada = FALSE
       AND foto_ruta IS NOT NULL
       AND fecha < CURDATE() - INTERVAL p_dias DAY;

    SET p_afectadas = ROW_COUNT();
END$$

DELIMITER ;
