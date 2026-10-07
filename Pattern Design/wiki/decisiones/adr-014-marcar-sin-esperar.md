---
titulo: ADR-014 — Marcar sin esperar: el celular guarda primero, el servidor recibe después
tipo: decision
estado: estable
fuentes:
  - instrucciones del usuario (2026-10-06)
  - "[[adr-008-alcance-interfaz-y-cola-sin-senal]]"
  - "[[adr-012-sin-cargos-y-sin-rechazos]]"
  - "[[adr-013-la-transaccion-de-una-marcacion]]"
actualizado: 2026-10-06
tags: [decision, propuesta, offline, ux, rendimiento, pwa]
---

# ADR-014 — Marcar sin esperar

**Estado: ACEPTADA el 2026-10-06**, con las respuestas del usuario al final. La parte de datos
(esquema, procedimientos y capa `bd`) ya está construida y probada; la del celular (service
worker y cola) es de la capa `vista`.

## Contexto

> «Las trabajadoras llegan rápido y la marcación tiene que ser rápida: no puede quedarse
> cargando o no quedar registro de su marcación. Que no tengan ningún inconveniente con el
> internet o con que el sistema no cargue.» *(usuario, 2026-10-06)*

[[adr-008-alcance-interfaz-y-cola-sin-senal]] ya resolvió la mitad: el flujo no toca el
servidor entre pantalla y pantalla, se envía **una vez** al final, y si el envío falla queda en
cola. Lo que no resolvía:

| Problema | Qué ve la trabajadora hoy (diseño ADR-008) |
|---|---|
| La página no abre sin señal | Pantalla en blanco o «sin conexión» del navegador |
| El envío tarda con señal mala | Un círculo girando, justo cuando tiene prisa |
| **El servidor guardó pero la respuesta se perdió** | Error → reintenta → **la marcación entra dos veces** |
| La foto pesa 100 veces más que los datos | Con señal mala, la foto frena el envío entero |
| El teléfono tiene la hora mal puesta | Toda marcación encolada queda con hora falsa |

## La idea central

> **La marcación queda registrada EN EL CELULAR en el instante en que se toca el botón.
> Enviarla al servidor es trabajo de fondo, que nunca la hace esperar.**

Es lo mismo que hace WhatsApp con un mensaje: se escribe, aparece en la conversación al
instante con un visto gris, y se envía cuando se puede.

## La propuesta, en siete piezas

### 1. La página abre sin internet

Un *service worker* —un pequeño programa que el navegador guarda junto a la página— conserva
en el celular la página, sus estilos, su JavaScript, **la lista de sedes y el padrón** (unos
10 KB en total). Desde la segunda visita, **la página abre en menos de un segundo aunque el
celular esté en modo avión**. Cuando hay señal, se actualiza sola por detrás.

Por eso hacían falta `sp_sedes` y `sp_padron` sin filtro: el celular guarda la lista entera y
la pantalla «¿Quién eres?» funciona sin conexión.

### 2. Guardar primero en el celular, enviar después

Al tocar **ENVIAR**, en este orden y sin esperar a la red:

1. Se genera una **clave de envío** única para este toque (un UUID).
2. Se guardan en el almacén del navegador (IndexedDB) la persona, la sede, la clave, la foto
   ya comprimida y **el instante del toque**.
3. **Se le confirma a ella.** Ya está registrada: si el celular se queda sin batería un
   segundo después, la marcación sigue ahí.
4. Se intenta enviar, por detrás.

**Objetivo medible: del toque a la confirmación, menos de 300 ms, siempre**, haya señal o no.

### 3. Dos vistos, como WhatsApp

| Lo que ve | Qué significa |
|---|---|
| ✓ gris — *«Guardada en este celular»* | Está registrada y se enviará sola |
| ✓✓ verde — *«Recibida»* | Ya está en el sistema |

**Nunca un círculo girando que la retenga, y nunca la palabra «error».** Un envío que falla no
es un problema de ella: la marcación sigue con su ✓ y se reintenta sola. Las trabajadoras ya
conocen este código por WhatsApp; no hay nada que enseñar.

Si al abrir la página quedan marcaciones sin enviar, se ve arriba: *«1 marcación por enviar»*.
En un celular compartido lo ve también la siguiente persona, y **cualquiera que abra la página
con señal la envía**.

### 4. Los datos y la foto viajan por separado

Primero los **datos** (~300 bytes: entran hasta con una rayita de señal); el servidor los guarda
y responde ✓✓ en milisegundos. Después la **foto** (~100 KB), en un segundo envío que se
reintenta por su cuenta.

**Esto cierra el hueco 33** (la foto contra la inmutabilidad de la marcación): la foto pasa a
ser **un hecho aparte que llega después**, en una tabla propia `foto_marcacion` donde solo se
inserta. La marcación no se edita nunca, la regla 2 se cumple por construcción —la marcación
existe antes que la foto, siempre— y el dashboard puede decir honestamente *«foto pendiente»*.

> **Cambia una decisión de ADR-008**, que decía «se envía una sola vez». Pasan a ser dos envíos.
> El motivo: con un solo envío, la foto —que es corroboración, no el hecho— decide cuánto tarda
> en llegar la marcación, que es el hecho.

### 5. Una marcación nunca entra dos veces

El caso más traicionero con señal mala: **el servidor guarda la marcación, pero la respuesta
no llega al celular**. El celular cree que falló y reenvía. Sin protección, entra dos veces.

Con la **clave de envío** del punto 2, `sp_registrar_marcacion` —ya dentro del candado por
persona de [[adr-013-la-transaccion-de-una-marcacion]]— busca primero esa clave y, si ya está,
**devuelve la marcación existente en vez de insertar otra**.

> **Esto no contradice [[adr-012-sin-cargos-y-sin-rechazos]].** No se rechaza nada y no hace
> falta ningún `UNIQUE`: es una búsqueda bajo el candado que ya existe. Y distingue dos cosas
> que hoy se confunden: **dos toques** son dos claves y entran las dos, como manda ADR-012; **un
> toque enviado dos veces** es una clave y es una marcación.

### 6. La hora: la del servidor, aunque llegue tarde

ADR-008 resolvía la cola guardando la hora **que decía el teléfono**. Si el teléfono tiene la
hora mal puesta —pasa más de lo que parece—, todas sus marcaciones encoladas quedan mal.

**Propuesta: el celular no manda qué hora es, manda cuánto tiempo pasó desde el toque.** El
servidor calcula `momento = su propia hora − segundos transcurridos`. Así el teléfono puede
tener la hora mal puesta y no importa: solo cuenta la diferencia.

| Llegó... | `fuente_hora` | Incidencia |
|---|---|---|
| en menos de 2 minutos | `SERVIDOR` | ninguna |
| más tarde | `DISPOSITIVO` | `HORA_DEL_TELEFONO`, con el retraso |

Nada se rechaza, como manda ADR-012: lo raro se acepta y se enseña.

### 7. El servidor no hace esperar

El envío de datos solo hace el `INSERT` del circuito de ADR-013 (milisegundos). Decodificar la
foto y hacer la miniatura va **aparte**, con el límite de 4 a la vez de
[[adr-012-sin-cargos-y-sin-rechazos]]. Una ráfaga de 80 personas hace cola **en las fotos**,
nunca en las marcaciones.

## Lo que el celular NO puede garantizar

Hay que decirlo claro, porque ninguna página web puede:

- **Si el celular se rompe o se pierde antes de enviar**, esas marcaciones se pierden. Queda la
  corrección manual de la administradora, que ya existe.
- **iPhone no envía en segundo plano** (Safari no tiene *Background Sync*). Las pendientes se
  envían la próxima vez que alguien abra la página. Android sí puede enviarlas solo.
- **iPhone puede borrar los datos de una página web que no se usa en 7 días**, salvo que esté
  **instalada en la pantalla de inicio**. Conviene instalarla en cada celular el día que se
  explica el sistema.
- **Borrar los datos del navegador** borra las pendientes de ese celular.

## Lo que cambia, si se aprueba

| Dónde | Cambio |
|---|---|
| `SQL/01_esquema.sql` | `marcacion.clave_envio CHAR(36) NULL` con índice (no único); tabla nueva `foto_marcacion`; **salen** `foto_ruta`, `foto_mini_ruta` y `foto_purgada` de `marcacion` |
| `SQL/02_procedimientos.sql` | `sp_registrar_marcacion` recibe la clave y los segundos transcurridos; nuevo `sp_registrar_foto` |
| Capa `control` | Dos entradas: `POST /marcar` (datos) y `POST /marcar/foto` (foto) |
| Capa `vista` | Service worker y cola en IndexedDB: ~400 líneas de JavaScript propio, **sin framework**, en el mismo JAR. ADR-008 calculaba ~200 |

**No cambia ningún patrón**, y la fachada `RegistrarMarcacion` sigue siendo una sola puerta.

## Cómo se prueba

Con capturas, porque [[pc3-entregable]] las pide por caso de prueba:

1. **Modo avión:** abrir la página, marcar, ver ✓; quitar el modo avión, ver ✓✓.
2. **Red lenta simulada** (3G en las herramientas del navegador): la confirmación sigue tardando
   menos de 300 ms.
3. **Servidor apagado a mitad de envío:** al volver, la marcación entra **una sola vez**.
4. **Teléfono con la hora mal puesta:** la marcación entra con la hora correcta.

## Respuestas del usuario (2026-10-06)

| Pregunta | Respuesta |
|---|---|
| ¿Foto en su propia tabla? | **Sí.** Cierra el hueco 33 |
| ¿Pedir que instalen la página en la pantalla de inicio? | **Sí, se puede pedir** |
| ¿Dos minutos como frontera de la hora? | **Sí** |
| ¿Un celular o tableta fija por sede? | **No.** Son demasiadas podólogas para un solo aparato. El teléfono de la sede se puede usar, pero tiene otros usos y no puede ser el canal de todas |

**El usuario preguntó si instalar la página arregla el envío en segundo plano en iPhone. No lo
arregla, y quedó dicho así:** Safari no tiene *Background Sync*, tampoco instalado. Instalarla
sirve para otra cosa igual de importante —que iPhone no borre las pendientes a los 7 días— y
para que abra a pantalla completa. En iPhone, una pendiente se envía mientras la página siga
abierta (reintento cada pocos segundos) o la próxima vez que se abra; la hora sigue siendo la
correcta gracias a la pieza 6, y solo queda anotada si tardó más de 2 minutos. En Android se
envía sola.

**Consecuencia de la respuesta 4:** el canal es el celular de cada una; el teléfono de la sede y
el de una compañera quedan **para excepciones** (sin celular, sin batería). Como la cola es por
aparato, un teléfono compartido guarda y envía las de varias personas sin mezclarlas.

> **Hueco nuevo:** conviene que la empresa sepa **cuántas trabajadoras usan iPhone**. Son las
> únicas para las que una pendiente puede tardar en llegar.

## Lo que ya está construido (2026-10-06)

- `marcacion.clave_envio` con índice **no único**, y tabla `foto_marcacion` cuya clave primaria
  es la marcación.
- `sp_registrar_marcacion` reconoce el reenvío **bajo el candado de la persona** y devuelve la
  marcación original con `p_repetida = TRUE`. Nuevo `sp_registrar_foto`, que busca por clave.
- `sp_marcaciones_del_dia` y `sp_historial_trabajadora` devuelven **las mismas columnas de
  foto que antes**, ahora leídas de la tabla nueva: Java no tuvo que cambiar su mapper.
- Pruebas contra MySQL: la foto llega después y no se duplica al reenviarla; un reenvío con la
  hora recalculada sigue siendo la misma marcación; **8 reenvíos simultáneos de la misma clave
  dejan una sola marcación** (5 de 5 corridas); dos toques con claves distintas siguen siendo
  dos marcaciones.
- Pendiente para `control`: calcular `momento = hora del servidor − segundos transcurridos` y la
  frontera de 2 minutos. Pendiente para `vista`: el service worker, la cola y los dos vistos.

## Enlaces

[[adr-008-alcance-interfaz-y-cola-sin-senal]] · [[adr-012-sin-cargos-y-sin-rechazos]] ·
[[adr-013-la-transaccion-de-una-marcacion]] · [[huecos-abiertos]] · [[modelo-datos]] ·
[[requisitos]]
