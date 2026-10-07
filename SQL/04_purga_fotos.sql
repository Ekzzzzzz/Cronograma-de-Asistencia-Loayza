-- =====================================================================
--  Control de asistencia - Podologia Loayza
--  04_purga_fotos.sql : el evento nocturno de los 3 meses
-- =====================================================================
--  El usuario fijo la retencion de fotos en 3 meses. Eso no es un
--  detalle tecnico: es la promesa que la empresa le hace a la
--  trabajadora sobre su propia cara. Una promesa que solo se cumple si
--  alguien la ejecuta sin que nadie se acuerde, y por eso vive aqui y
--  no en un recordatorio de calendario.
--
--  QUE SE BORRA Y QUE NO
--  ---------------------
--    - La marcacion NO se borra nunca. Es evidencia de jornada laboral.
--    - La MINIATURA NO se borra: sigue probando que hubo una foto.
--    - La foto COMPLETA si: es el dato personal, y es el que caduca.
--
--  QUE HACE ESTE ARCHIVO Y QUE NO
--  ------------------------------
--  MySQL no guarda las imagenes, solo la clave del blob. Asi que esto
--  levanta la bandera foto_marcacion.purgada y nada mas; el borrado del archivo
--  real lo hace Java leyendo lo que esto marco. Separarlo tiene una
--  ventaja concreta: si el borrado del blob falla -la nube no responde,
--  la credencial caduco- la bandera ya esta puesta y el proceso se
--  puede reintentar sin volver a calcular que tocaba borrar.
--
--  Ejecutar despues de 02_procedimientos.sql, como root:
--      mysql -u root -p asistencia_loayza < 04_purga_fotos.sql
-- =====================================================================

USE asistencia_loayza;

-- ---------------------------------------------------------------------
--  El planificador de eventos viene APAGADO en MySQL por defecto.
--  Sin esta linea el evento se crea y no se ejecuta nunca, que es el
--  peor de los fallos posibles: silencioso.
--
--  SET GLOBAL solo dura hasta el siguiente reinicio del servidor. Para
--  que sobreviva hay que anadir en my.ini, bajo [mysqld]:
--      event_scheduler = ON
--  En el servidor de Oracle va en el my.cnf del despliegue.
-- ---------------------------------------------------------------------
SET GLOBAL event_scheduler = ON;

DROP EVENT IF EXISTS ev_purga_fotos;

DELIMITER $$

CREATE EVENT ev_purga_fotos
    ON SCHEDULE EVERY 1 DAY
    -- A las 3 de la manana: los locales cerraron hace horas y no hay
    -- nadie marcando. La purga toca la tabla que mas crece, asi que no
    -- se pone a competir con la rafaga del cambio de turno.
    STARTS (CURDATE() + INTERVAL 1 DAY + INTERVAL 3 HOUR)
    ON COMPLETION PRESERVE
    COMMENT 'Marca como purgadas las fotos de mas de 90 dias'
DO
BEGIN
    DECLARE v_afectadas INT DEFAULT 0;

    -- 90 dias = los 3 meses que pidio el usuario. El numero esta aqui y
    -- tambien en application.yml (asistencia.foto.retencion-dias), que
    -- es lo que Java usa para borrar los blobs. Los dos tienen que
    -- decir lo mismo: si un dia cambia, cambia en los dos sitios.
    CALL sp_purgar_fotos(90, v_afectadas);
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
--  Como se comprueba que esto se ejecuto
-- ---------------------------------------------------------------------
--  El evento no escribe ningun log propio, y es deliberado: la unica
--  tabla que habria servido es intento_rechazado, que alimenta el panel
--  de rechazos del endpoint publico. Meter ahi una tarea nocturna
--  ensuciaria justo la pantalla donde la administradora busca abusos.
--
--  Para saber si corrio hay dos sitios sin inventar nada:
--    - last_executed de la consulta de abajo, que lo lleva el motor.
--    - SELECT COUNT(*) FROM foto_marcacion WHERE purgada, que dice
--      cuanto lleva purgado en total.
-- ---------------------------------------------------------------------
SELECT @@event_scheduler AS planificador;

SELECT event_name, status, interval_value, interval_field, starts, last_executed
  FROM information_schema.events
 WHERE event_schema = 'asistencia_loayza';
