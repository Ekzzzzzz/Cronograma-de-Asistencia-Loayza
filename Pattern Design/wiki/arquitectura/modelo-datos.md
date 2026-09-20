---
titulo: Modelo de datos
tipo: arquitectura
estado: borrador
fuentes:
  - docs/Cronograma_Ejemplo.xlsx
  - decisiones del usuario (2026-09-06)
actualizado: 2026-09-06
tags: [arquitectura, base-de-datos, mysql, rendimiento]
---

# Modelo de datos

Esquema para **MySQL 8**, motor InnoDB, `utf8mb4`, zona horaria `America/Lima`. Las
decisiones que lo sostienen están en [[adr-002-arquitectura-produccion]] y
[[adr-003-login-y-cronograma-simple]]; el dimensionamiento que justifica cada índice, también.

Cinco tablas. Ninguna guarda imágenes: las fotos van al disco y aquí solo viaja la ruta.

## `sede`

Catálogo cerrado de 7 filas, sembrado en el script de datos iniciales. Es el mismo dato que
[[patron-singleton]] mantiene en memoria como `CatalogoSedes`; la tabla existe para que las
claves foráneas tengan a qué apuntar.

```sql
CREATE TABLE sede (
  id        TINYINT UNSIGNED PRIMARY KEY,
  nombre    VARCHAR(40)  NOT NULL UNIQUE,
  plus_code VARCHAR(16)  NOT NULL,
  latitud   DECIMAL(9,6) NOT NULL,
  longitud  DECIMAL(9,6) NOT NULL,
  activa    BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;
```

Coordenadas de [[sedes]]. `latitud`/`longitud` se guardan aunque hoy no se usen: son el dato
que necesitaría la validación por GPS de la pregunta 4 de [[huecos-abiertos]], y no cuestan
nada.

## `trabajadora`

~140 filas. Existe por la decisión 4 de [[adr-002-arquitectura-produccion]]: la trabajadora se
**elige de una lista**, no escribe su nombre.

```sql
CREATE TABLE trabajadora (
  id            SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(80) NOT NULL,
  cargo         ENUM('PODOLOGA','CAJERA') NULL,
  jornada       ENUM('FULL_TIME','PART_TIME') NULL,
  documento     VARCHAR(12) NULL UNIQUE,
  sede_habitual TINYINT UNSIGNED NULL,
  activa        BOOLEAN NOT NULL DEFAULT TRUE,
  provisional   BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_trab_sede FOREIGN KEY (sede_habitual) REFERENCES sede(id)
) ENGINE=InnoDB;
```

> **Corregido el 2026-09-20** por [[adr-010-sin-rechazos-oracle-y-nombre-libre]]: `cargo` y `jornada` **admiten nulo**, y
> aparece `provisional`. No es indecisión: una trabajadora que se registra **escribiendo su
> nombre** porque no está en la lista entra sin cargo ni jornada conocidos, y un `NOT NULL`
> obligaría a inventárselos. Mientras `provisional` sea verdadero, esa fila **no entra al
> cronograma exportado**: la administradora la confirma como alta nueva o la fusiona con quien
> ya existe.

`cargo` entra con [[adr-006-dashboard-y-turnos-multiples]]: no todas son podólogas, también hay
cajeras. Van **en la misma tabla del cronograma**, con el cargo como columna. Es `ENUM` y no
tabla de catálogo porque el usuario confirmó que **no hay más cargos que esos dos**.

`jornada` entra con [[adr-007-seguridad-endpoint-publico]] y es **independiente del cargo**: hay
podólogas y cajeras de jornada completa y de media. **Se muestra, no se valida** — el sistema no
implementa ninguna regla del tipo "media jornada no puede pasar de N horas", porque nadie la
pidió y rechazaría horas extra legítimas. Da contexto a los totales.

`sede_habitual` sirve para preseleccionar el desplegable y acortar el flujo (RNF-01), pero
**no restringe**: con [[marcacion-multiple]] cualquiera puede marcar en cualquier sede.

`activa` en vez de borrado físico: si una podóloga deja la empresa, sus marcaciones
históricas tienen que seguir existiendo para el cronograma de meses anteriores.

## `marcacion`

La tabla que crece: ~150 000 filas al año. Es el hecho inmutable del sistema — se inserta y
nunca se actualiza.

```sql
CREATE TABLE marcacion (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trabajadora_id SMALLINT UNSIGNED NOT NULL,
  sede_id        TINYINT  UNSIGNED NOT NULL,
  tipo           ENUM('ENTRADA','SALIDA') NOT NULL,
  momento        DATETIME NOT NULL,
  fecha          DATE GENERATED ALWAYS AS (DATE(momento)) STORED,
  fuente_hora    ENUM('SERVIDOR','DISPOSITIVO') NOT NULL DEFAULT 'SERVIDOR',
  momento_recibido DATETIME NULL,
  notas          VARCHAR(500) NULL,
  foto_ruta      VARCHAR(200) NULL,
  foto_mini_ruta VARCHAR(200) NULL,
  foto_purgada   BOOLEAN  NOT NULL DEFAULT FALSE,
  origen         ENUM('TRABAJADORA','ADMINISTRADORA') NOT NULL DEFAULT 'TRABAJADORA',
  corregida_por  SMALLINT UNSIGNED NULL,
  motivo         VARCHAR(200) NULL,
  creado_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_marc_trab FOREIGN KEY (trabajadora_id) REFERENCES trabajadora(id),
  CONSTRAINT fk_marc_sede FOREIGN KEY (sede_id)        REFERENCES sede(id),
  INDEX idx_cronograma  (sede_id, fecha, trabajadora_id, momento),
  INDEX idx_trabajadora (trabajadora_id, fecha)
) ENGINE=InnoDB;
```

Puntos que importan:

- **`fuente_hora` y `momento_recibido` existen por la cola sin señal**
  ([[adr-008-alcance-interfaz-y-cola-sin-senal]]). En una marcación normal `fuente_hora`
  es `SERVIDOR` y `momento_recibido` queda nulo. En una encolada, `momento` es **lo que
  dijo el teléfono**, `momento_recibido` es cuándo llegó de verdad, y la diferencia entre
  ambos es lo que ve la administradora. **El sistema nunca afirma una hora que no puede
  probar:** si la puso el dispositivo, lo dice.
- **Una marcación encolada que llega con más de 2 horas de retraso se rechaza** y va a
  `intento_rechazado` con motivo `COLA_VENCIDA`. Sin ese tope bastaría atrasar el reloj
  del teléfono para fabricar historia laboral.
- **El cooldown y el tope de 12 se evalúan contra `momento`, no contra la llegada.** Si no,
  una cola de tres marcaciones legítimas se rechazaría entera por llegar junta.

- **`momento` lo pone el servidor**, nunca el cliente. Es la garantía anti-fraude de
  [[adr-001-patrones-seleccionados]], la misma hora que [[patron-decorator]] imprime en la
  imagen.
- **`fecha` es una columna generada y almacenada.** Sin ella, filtrar por rango de días
  obligaría a `DATE(momento)`, que anula el índice. Con ella, la consulta del cronograma es un
  *range scan*. Ver la medida 4 de [[adr-002-arquitectura-produccion]].
- **`idx_cronograma` está ordenado como la consulta que sirve**: filtra por sede, recorta por
  rango de fechas y agrupa por trabajadora ordenando por hora. Es la consulta del dashboard y
  de la exportación, las dos más frecuentes del sistema.
- **`foto_purgada`** distingue "esta marcación no tiene foto" de "la foto completa expiró a
  los 3 meses y solo queda la miniatura" (decisión 5 del ADR-002).
- No hay `ON DELETE CASCADE` en ninguna parte: **las marcaciones no se borran**. Son evidencia
  de jornada laboral.
- **`origen`, `corregida_por` y `motivo` sostienen la corrección de olvidos**
  ([[adr-006-dashboard-y-turnos-multiples]]). Cuando alguien olvida marcar la salida, la
  administradora **inserta una marcación nueva**, no edita la existente: el hecho sigue siendo
  inmutable y además queda registrado quién lo puso y por qué.
- **`foto_ruta` admite nulo solo por eso.** Una marcación con `origen = TRABAJADORA` siempre
  trae foto; una puesta a mano no puede tenerla. En el dashboard y en la exportación las dos se
  distinguen a la vista.
- **`foto_ruta` y `foto_mini_ruta` guardan la clave del blob**, no una ruta de disco, desde
  [[adr-004-despliegue-azure]]. Es el mismo `VARCHAR`: el cambio de destino no toca el
  esquema.

## `administrador`

Introducida por [[adr-003-login-y-cronograma-simple]]: el dashboard lleva login obligatorio.
Pocas filas — una o dos personas.

```sql
CREATE TABLE administrador (
  id       SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  usuario  VARCHAR(40)  NOT NULL UNIQUE,
  clave    CHAR(60)     NOT NULL,
  nombre   VARCHAR(80)  NOT NULL,
  activo   BOOLEAN      NOT NULL DEFAULT TRUE,
  ultimo_acceso DATETIME NULL
) ENGINE=InnoDB;
```

`clave` es `CHAR(60)` porque un hash **BCrypt** ocupa exactamente 60 caracteres. Nunca se
guarda la contraseña en claro ni un hash sin sal.

El login protege el dashboard (RF-11), la exportación (RF-13) y el alta de trabajadoras.
**La cara trabajadora no pasa por aquí**: sigue siendo un enlace público sin credenciales,
como manda [[requisitos]] RF-01. Es la sesión que consulta el *Protection Proxy* de
[[patron-proxy]].

> **Nota:** no hay tabla de planificación de turnos. Existió en el borrador de este modelo
> para distinguir `DESCANSO`, `NO TURNO` e `INASISTENCIA`, pero
> [[adr-003-login-y-cronograma-simple]] fijó que **esos casos se manejan a mano fuera del
> sistema**. El sistema solo escribe horas.

## `intento_rechazado`

Introducida por [[adr-007-seguridad-endpoint-publico]]. El formulario de marcación es un
endpoint **público y sin autenticación**, así que las barreras contra abuso tienen que dejar
rastro: una defensa silenciosa protege, pero nadie se entera de que alguien lo intentó.

```sql
CREATE TABLE intento_rechazado (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  momento        DATETIME NOT NULL,
  ip             VARBINARY(16) NULL,
  trabajadora_id SMALLINT UNSIGNED NULL,
  sede_id        TINYINT  UNSIGNED NULL,
  motivo         ENUM('TOPE_DIARIO','COOLDOWN','ARCHIVO_INVALIDO',
                      'IMAGEN_ENORME','DEMASIADO_GRANDE','COLA_VENCIDA') NOT NULL,
  INDEX idx_intentos (momento)
) ENGINE=InnoDB;
```

`sp_intentos_rechazados(p_desde, p_hasta)` devuelve la lista para la octava función del
dashboard: sin ella la defensa actuaría en silencio y la administradora no se enteraría de nada
([[adr-009-estructura-y-dos-patrones-que-vuelven]]). Con eso son **diez procedimientos**.

- `trabajadora_id` admite nulo porque un archivo inválido se rechaza **antes** de saber quién
  dice ser quien lo envía.
- `ip` como `VARBINARY(16)` acepta IPv4 e IPv6 sin desperdiciar espacio.
- La administradora ve estos intentos en el dashboard. Si uno resultó legítimo —alguien que de
  verdad rotó siete veces— se arregla por la vía de la corrección manual de
  [[adr-006-dashboard-y-turnos-multiples]], que pasa por una persona y no por el endpoint
  abierto.

## `jornada_diaria`

Tabla resumen: una fila por (trabajadora, sede, fecha). **Es exactamente una celda del Excel**
de [[formato-cronograma-excel]].

```sql
CREATE TABLE jornada_diaria (
  sede_id         TINYINT  UNSIGNED NOT NULL,
  fecha           DATE NOT NULL,
  trabajadora_id  SMALLINT UNSIGNED NOT NULL,
  primera_entrada TIME NULL,
  ultima_salida   TIME NULL,
  turnos          TINYINT  UNSIGNED NOT NULL DEFAULT 0,
  minutos         SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  etiqueta        VARCHAR(160) NOT NULL,
  PRIMARY KEY (sede_id, fecha, trabajadora_id)
) ENGINE=InnoDB;
```

- **La clave primaria está ordenada como la consulta del cronograma.** En InnoDB la tabla se
  almacena físicamente en ese orden, así que exportar una semana de una sede es un recorrido
  contiguo de ~91 filas. Más rápido que esto no se puede.
- **`etiqueta` es literalmente el texto de la celda**: **un par por línea, hasta seis**
  ([[adr-006-dashboard-y-turnos-multiples]]). `VARCHAR(160)` porque seis pares con sus saltos
  de línea no caben en menos. `DESCANSO`, `NO TURNO` e `INASISTENCIA` **no los escribe el
  sistema**: los pone la administradora a mano sobre el archivo exportado.
- **No hay columna `estado`.** La tenía el borrador anterior, para persistir la máquina de
  [[patron-state]]; con State descartado y solo dos situaciones posibles, "turno abierto" se
  deriva de `ultima_salida IS NULL`. Una columna menos que mantener sincronizada.
- **`minutos` suma turno a turno, nunca resta los extremos.** Con una jornada de 9–1 y 3–8,
  `etiqueta` lleva las dos líneas, `turnos` guarda 2 y `minutos` guarda 540 —las 9 horas
  reales, no las 11 que darían los extremos—. Cualquier informe de horas debe usar `minutos`.
- **`turnos` tiene tope duro de 6**, es decir 12 marcaciones por (trabajadora, sede, día). La
  número 13 **se rechaza** y queda anotada en `intento_rechazado`. No es un límite de formato
  sino una **defensa contra abuso** ([[adr-007-seguridad-endpoint-publico]]): en la práctica no
  se ven más de 3 pares, así que seis dan el doble de holgura antes de estorbar a nadie.

  > **Matizado el 2026-09-20** por [[adr-010-sin-rechazos-oracle-y-nombre-libre]]: ese tope es **lo único** que
  > todavía rechaza, y solo como defensa del endpoint público. **Ninguna regla de negocio
  > invalida ya una marcación**: doble entrada, doble salida y varias marcaciones seguidas en la
  > misma sede **se aceptan todas**. Queda abierto si el tope también debe caer
  > ([[huecos-abiertos]] pregunta 27).

- **Los pares se arman en orden cronológico, y lo que sobra queda a la vista.** Una entrada sin
  salida escribe `10:12AM -`; una salida sin entrada previa escribe `- 8:46PM`. El sistema
  **no inventa la hora que falta ni descarta la marcación sobrante**.

**Se recalcula al insertar cada marcación**, dentro de la misma transacción, disparado por
[[patron-observer]]. A 400 marcaciones por día ese recálculo es irrelevante; a cambio, toda
lectura del cronograma es instantánea.

**Por qué existe esta tabla**, ahora que ya no persiste estados: emparejar N entradas con N
salidas en orden es lógica de dominio, y vive en `Jornada` → `Turno` ([[marcacion-multiple]]).
Si el cronograma se calculara consultando `marcacion` en crudo, ese emparejamiento habría que
escribirlo **una segunda vez en SQL** con funciones de ventana — la misma regla en dos
lenguajes, que es el *Shotgun Surgery* de [[s14-antipatrones]]. Con la tabla resumen, el
emparejamiento ocurre una sola vez, en Java, donde se puede probar.

## Stored procedures

RC-03 los exige ([[cuatro-capas]]) y aquí tienen trabajo real, no ceremonial:

| Procedimiento | Qué hace |
|---|---|
| `sp_registrar_marcacion` | Inserta la marcación y recalcula `jornada_diaria` en una sola transacción |
| `sp_cronograma_semanal` | Devuelve las ~91 celdas de una sede para una semana |
| `sp_marcaciones_del_dia` | Listado del dashboard, con rutas de miniatura |
| `sp_purgar_fotos` | Marca `foto_purgada` en lo que superó los 3 meses |
| `sp_autenticar_admin` | Devuelve el hash y el estado del administrador para que Spring Security lo verifique |
| `sp_dia_sede` | Vista diaria: cada trabajadora de una sede con todos sus turnos de ese día |
| `sp_historial_trabajadora` | Todas las marcaciones de una persona en un rango, **cruzando las 7 sedes** |
| `sp_totales_horas` | Suma de minutos por trabajadora y sede en un rango de fechas |
| `sp_validar_marcacion` | Cooldown y tope diario en **una sola consulta** antes de procesar la foto. Ya **no valida la secuencia**: ver [[adr-010-sin-rechazos-oracle-y-nombre-libre]] |
| `sp_cronograma_diario` | La hoja de un solo día de una sede, para la exportación diaria |
| `sp_provisionales` | Las trabajadoras registradas por nombre libre que esperan confirmación |

`sp_autenticar_admin` **devuelve el hash, no compara contraseñas**: la verificación BCrypt se
hace en Java. Un procedimiento que reciba la contraseña en claro la dejaría escrita en los
logs del motor.

`sp_registrar_marcacion` es el que justifica el patrón: insertar y recalcular tienen que ser
atómicos, y resolverlo en el motor evita un ida y vuelta extra desde Java.

## Lo que este modelo deja fuera a propósito

- **Sin tabla de `turno`** (el par entrada-salida). Se deriva de las marcaciones al recalcular
  `jornada_diaria`; materializarla añadiría una tercera copia del mismo hecho. El objeto
  `Turno` sí existe en el modelo Java — ver [[marcacion-multiple]].
- **Sin auditoría de cambios.** Las marcaciones no se editan, así que no hay qué auditar.
- **Sin particionado ni archivado.** A 150 000 filas al año, hablar de particionar es
  Overengineering ([[s14-antipatrones]]). Se revisa si alguna vez se pasa de ~5 millones.

## Enlaces

[[adr-002-arquitectura-produccion]] · [[marcacion-multiple]] · [[formato-cronograma-excel]] ·
[[cuatro-capas]] · [[patron-observer]] · [[patron-composite]]
