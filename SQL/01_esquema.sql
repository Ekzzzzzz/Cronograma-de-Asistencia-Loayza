-- =====================================================================
--  Control de asistencia - Podologia Loayza
--  01_esquema.sql : base de datos, usuario de la aplicacion y 6 tablas
-- =====================================================================
--  Motor  : MySQL 8.0
--  Cotejo : utf8mb4_0900_ai_ci  (acentos y enie sin sorpresas)
--  Zona    : America/Lima, la fija la conexion en application.yml
--
--  Ejecutar como root, una sola vez:
--      mysql -u root -p < 01_esquema.sql
--
--  El orden de los archivos importa:
--      01_esquema.sql -> 02_procedimientos.sql -> 03_datos_prueba.sql
--      -> 04_purga_fotos.sql
--
--  ---------------------------------------------------------------
--  LA REGLA QUE ORGANIZA TODO ESTE ESQUEMA (ADR-012)
--  ---------------------------------------------------------------
--  ESTE SISTEMA NO RECHAZA NINGUNA MARCACION. Ninguna. Las
--  trabajadoras marcan con prisa, entre paciente y paciente, y que a
--  alguien se le invalide una marcacion es el peor fallo posible del
--  sistema: le borra una hora de trabajo real.
--
--  Por eso aqui no hay ni un CHECK, ni un indice UNICO, ni una
--  restriccion que pueda hacer fallar un INSERT de marcacion. Doble
--  entrada, doble salida, doce marcaciones seguidas, una foto ilegible
--  o una marcacion que llega cinco horas tarde: todo entra. Lo que el
--  sistema hace con lo raro es ENSENARLO, en la tabla incidencia, para
--  que una persona lo mire. No decidirlo por ella.
-- =====================================================================

DROP DATABASE IF EXISTS asistencia_loayza;
CREATE DATABASE asistencia_loayza
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE asistencia_loayza;

-- ---------------------------------------------------------------------
--  Usuario de la aplicacion
-- ---------------------------------------------------------------------
--  La aplicacion NO entra como root. Entra como 'asistencia', que solo
--  puede tocar esta base. Si alguna vez se filtra la clave de la app,
--  lo que se pierde es esta base y nada mas.
--
--  Esta clave es la de DESARROLLO LOCAL, y por eso puede vivir en el
--  repositorio. En el servidor de Oracle se crea otra distinta y se
--  pasa por las variables de entorno DB_USER y DB_PASS, que es como
--  application.yml la espera. Nunca se escribe aqui.
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'asistencia'@'localhost' IDENTIFIED BY 'asistencia';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
  ON asistencia_loayza.* TO 'asistencia'@'localhost';
FLUSH PRIVILEGES;

-- =====================================================================
--  1. sede
-- =====================================================================
--  Hoy son 7, pero la empresa puede abrir mas. Por eso el id es
--  autoincremental y no un numero fijo del 1 al 7: dar de alta la
--  octava sede tiene que ser un INSERT, no una migracion.
-- =====================================================================
CREATE TABLE sede (
  id        TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre    VARCHAR(40)  NOT NULL UNIQUE,
  plus_code VARCHAR(16)  NULL,
  -- Se guardan aunque hoy no se usen: son el dato que necesitaria la
  -- validacion por GPS si algun dia entra. No cuestan nada.
  latitud   DECIMAL(9,6) NULL,
  longitud  DECIMAL(9,6) NULL,
  activa    BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- =====================================================================
--  2. trabajadora
-- =====================================================================
--  Entre 10 y 20 por sede: unas 100-140 hoy, mas las que traigan las
--  sedes nuevas.
--
--  NO HAY CARGO NI TIPO DE JORNADA, y es una decision del cliente
--  (ADR-012), no un olvido. El sistema registra horas trabajadas; si
--  alguien es podologa o cajera, y si tiene jornada completa o media,
--  no cambia ni una sola hora de las que se registran. Guardar un dato
--  que no se usa obliga a mantenerlo al dia para siempre y tienta a
--  inventar reglas con el.
-- =====================================================================
CREATE TABLE trabajadora (
  id            SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(80) NOT NULL,
  documento     VARCHAR(12) NULL UNIQUE,
  -- Preselecciona el desplegable para acortar el flujo. NO restringe:
  -- cualquiera puede marcar en cualquier sede.
  sede_habitual TINYINT UNSIGNED NULL,
  -- Baja logica, nunca DELETE: si alguien deja la empresa, sus
  -- marcaciones historicas tienen que seguir existiendo.
  activa        BOOLEAN NOT NULL DEFAULT TRUE,
  -- Se registro escribiendo su nombre porque no estaba en la lista.
  -- Marca igual; solo espera que la administradora la confirme.
  provisional   BOOLEAN NOT NULL DEFAULT FALSE,
  creada_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_trab_sede FOREIGN KEY (sede_habitual) REFERENCES sede(id),
  -- El dashboard pregunta "quien esta pendiente de confirmar" a diario.
  INDEX idx_provisionales (provisional, activa),
  -- El formulario busca por nombre mientras se escribe.
  INDEX idx_nombre (nombre)
) ENGINE=InnoDB;

-- =====================================================================
--  3. administrador
-- =====================================================================
--  Una sola cuenta para las 7 sedes, por decision del usuario.
-- =====================================================================
CREATE TABLE administrador (
  id            SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  usuario       VARCHAR(40) NOT NULL UNIQUE,
  -- CHAR(60) porque un hash BCrypt ocupa exactamente 60 caracteres.
  -- Nunca la contrasena en claro, nunca un hash sin sal.
  clave         CHAR(60)    NOT NULL,
  nombre        VARCHAR(80) NOT NULL,
  activo        BOOLEAN     NOT NULL DEFAULT TRUE,
  ultimo_acceso DATETIME    NULL
) ENGINE=InnoDB;

-- =====================================================================
--  4. marcacion
-- =====================================================================
--  La tabla que crece. Es el hecho inmutable del sistema: se inserta y
--  nunca se actualiza, y NUNCA se rechaza.
--
--  Merece la pena mirar lo que NO hay en esta tabla:
--    - ningun UNIQUE. Dos marcaciones identicas al mismo segundo se
--      guardan las dos. Si el boton se pulso dos veces, eso paso y se
--      ve; inventarse cual sobra es decidir por la trabajadora.
--    - ningun CHECK de secuencia. Dos entradas seguidas son validas.
--    - ningun ON DELETE CASCADE. Las marcaciones no se borran: son
--      evidencia de jornada laboral.
--
--  foto_ruta admite NULL POR DISENO, no por comodidad: si la foto
--  falla -viene rota, pesa demasiado, la camara devolvio basura- la
--  MARCACION SE GUARDA IGUAL y la incidencia queda anotada aparte. La
--  hora trabajada no depende de que la camara funcionara.
-- =====================================================================
CREATE TABLE marcacion (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trabajadora_id SMALLINT UNSIGNED NOT NULL,
  sede_id        TINYINT  UNSIGNED NOT NULL,
  tipo           ENUM('ENTRADA','SALIDA') NOT NULL,

  -- La hora que vale. Normalmente la pone el servidor.
  momento        DATETIME NOT NULL,

  -- Columna generada y ALMACENADA. Sin ella, filtrar por rango de dias
  -- obligaria a DATE(momento), que anula el indice; con ella la consulta
  -- del cronograma es un range scan.
  fecha          DATE GENERATED ALWAYS AS (DATE(momento)) STORED,

  -- Cola sin senal. En una marcacion normal fuente_hora es SERVIDOR y
  -- momento_recibido queda NULL. En una encolada, momento es lo que
  -- dijo el telefono y momento_recibido es cuando llego de verdad.
  -- El sistema nunca afirma una hora que no puede probar.
  fuente_hora      ENUM('SERVIDOR','DISPOSITIVO') NOT NULL DEFAULT 'SERVIDOR',
  momento_recibido DATETIME NULL,

  notas          VARCHAR(500) NULL,

  -- Clave del blob, no ruta de disco.
  foto_ruta      VARCHAR(200) NULL,
  foto_mini_ruta VARCHAR(200) NULL,
  -- Distingue "no tiene foto" de "la foto completa expiro a los 3 meses
  -- y solo queda la miniatura".
  foto_purgada   BOOLEAN NOT NULL DEFAULT FALSE,

  -- Correccion de olvidos: la administradora INSERTA una marcacion
  -- nueva, no edita la existente. El hecho sigue siendo inmutable y
  -- ademas queda escrito quien lo puso y por que.
  origen         ENUM('TRABAJADORA','ADMINISTRADORA') NOT NULL DEFAULT 'TRABAJADORA',
  corregida_por  SMALLINT UNSIGNED NULL,
  motivo         VARCHAR(200) NULL,

  creado_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_marc_trab  FOREIGN KEY (trabajadora_id) REFERENCES trabajadora(id),
  CONSTRAINT fk_marc_sede  FOREIGN KEY (sede_id)        REFERENCES sede(id),
  CONSTRAINT fk_marc_admin FOREIGN KEY (corregida_por)  REFERENCES administrador(id),

  -- Ordenado como la consulta que sirve: filtra por sede, recorta por
  -- rango de fechas, agrupa por trabajadora y ordena por hora. Es la
  -- consulta del dashboard y de las dos exportaciones.
  INDEX idx_cronograma  (sede_id, fecha, trabajadora_id, momento),
  -- El historial cruza todas las sedes, asi que necesita su propio indice.
  INDEX idx_trabajadora (trabajadora_id, fecha)
) ENGINE=InnoDB;

-- =====================================================================
--  5. jornada_diaria
-- =====================================================================
--  Tabla resumen: una fila por (trabajadora, sede, fecha). Es
--  exactamente UNA CELDA del Excel del cronograma, y sirve igual a la
--  exportacion diaria y a la semanal.
--
--  La clave primaria esta ordenada como la consulta del cronograma. En
--  InnoDB la tabla se almacena fisicamente en ese orden, asi que
--  exportar una semana de una sede es un recorrido contiguo.
-- =====================================================================
CREATE TABLE jornada_diaria (
  sede_id         TINYINT  UNSIGNED NOT NULL,
  fecha           DATE              NOT NULL,
  trabajadora_id  SMALLINT UNSIGNED NOT NULL,
  primera_entrada TIME NULL,
  ultima_salida   TIME NULL,
  -- Sin tope. Antes estaba documentado como "maximo 6"; ese tope cayo
  -- con ADR-012 junto con todos los rechazos.
  turnos          TINYINT  UNSIGNED NOT NULL DEFAULT 0,
  -- Suma turno a turno, NUNCA la resta de los extremos. Una jornada de
  -- 9-1 y 2-6 guarda 480 (las 8 horas reales), no 540. Cualquier
  -- informe de horas debe usar esta columna.
  minutos         SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  -- Literalmente el texto de la celda: un par por linea.
  --
  -- VARCHAR(400) y no 160: con el tope de 12 marcaciones no se podia
  -- pasar de ~120 caracteres, pero ese tope ya no existe. Si alguien
  -- marca veinte veces, la celda tiene que caber; si no cupiera, MySQL
  -- la cortaria en silencio y nadie se enteraria.
  etiqueta        VARCHAR(400) NOT NULL DEFAULT '',
  actualizada_en  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sede_id, fecha, trabajadora_id),
  CONSTRAINT fk_jor_sede FOREIGN KEY (sede_id)        REFERENCES sede(id),
  CONSTRAINT fk_jor_trab FOREIGN KEY (trabajadora_id) REFERENCES trabajadora(id)
) ENGINE=InnoDB;

-- =====================================================================
--  6. incidencia
-- =====================================================================
--  ANTES SE LLAMABA intento_rechazado, Y EL CAMBIO DE NOMBRE ES EL
--  CAMBIO DE FONDO: ya no queda nada que rechazar.
--
--  Esta tabla no guarda lo que el sistema impidio. Guarda lo que el
--  sistema ACEPTO Y NO ENTENDIO DEL TODO, para que una persona lo mire:
--  una foto que no se pudo guardar, una marcacion que llego con seis
--  horas de retraso, un envio duplicado al mismo segundo.
--
--  En casi todos los casos hay una marcacion guardada al lado, y
--  marcacion_id apunta a ella. Cuando no la hay -porque llego una
--  peticion que ni siquiera decia quien era- queda en NULL.
--
--  Es la contrapartida honesta de no rechazar nada: si el sistema deja
--  entrar todo, TIENE que ensenar bien lo que entro. Si no, acepta en
--  silencio y nadie se entera de nada.
-- =====================================================================
CREATE TABLE incidencia (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  momento        DATETIME NOT NULL,
  -- La marcacion que SI se guardo, cuando la hay.
  marcacion_id   BIGINT UNSIGNED NULL,
  -- VARBINARY(16) acepta IPv4 e IPv6 sin desperdiciar espacio.
  -- Se escribe con INET6_ATON() y se lee con INET6_NTOA().
  ip             VARBINARY(16) NULL,
  trabajadora_id SMALLINT UNSIGNED NULL,
  sede_id        TINYINT  UNSIGNED NULL,
  tipo           ENUM('FOTO_NO_GUARDADA',
                      'FOTO_DEMASIADO_PESADA',
                      'FOTO_SOSPECHOSA',
                      'HORA_DEL_TELEFONO',
                      'COLA_MUY_ANTIGUA',
                      'ENVIO_DUPLICADO') NOT NULL,
  detalle        VARCHAR(200) NULL,
  -- La administradora marca lo que ya reviso, para no volver a mirarlo.
  revisada       BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_inc_marc FOREIGN KEY (marcacion_id) REFERENCES marcacion(id),
  INDEX idx_incidencias (momento),
  INDEX idx_pendientes (revisada, momento)
  -- Sin clave foranea a trabajadora a proposito: aqui tambien se anota
  -- lo que llego sin un nombre que exista.
) ENGINE=InnoDB;
