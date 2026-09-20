---
titulo: ADR-012 — Sin cargos, sin jornadas y sin un solo rechazo
tipo: decision
estado: estable
fuentes:
  - instrucciones del usuario (2026-09-20)
actualizado: 2026-09-20
tags: [decision, dominio, seguridad, rendimiento, exportacion]
---

# ADR-012 — Sin cargos, sin jornadas y sin un solo rechazo

Tres instrucciones del usuario el 2026-09-20, después de ver el esquema cargado. La tercera
**revierte decisiones de tres ADR anteriores**, así que conviene dejar escrito qué se quita y
por qué, para que nadie lo reponga dentro de un mes creyendo que fue un descuido.

## 1. Fuera `cargo` y `tipo de jornada`

> «El software no debería distinguir cargos (podólogas, cajera) ni si es full time o part time.
> Las trabajadoras no deben tener cargo ni tipo de jornada.»

**Eliminados del esquema, del modelo Java y de todas las consultas.**

### Por qué es una mejora y no una pérdida

[[modelo-datos]] ya decía que `jornada` **se muestra, no se valida**, y que `cargo` solo daba
contexto. Es decir: ya estaban reconocidos como datos que **no cambian ni una sola de las horas
que el sistema registra**. Un dato así tiene tres costos y ningún beneficio:

1. **Hay que mantenerlo al día para siempre.** Alguien pasa de cajera a podóloga y nadie avisa;
   a los seis meses el padrón miente y nadie lo sabe.
2. **Se queda obsoleto en silencio.** No hay ninguna pantalla que fallara si estuviera mal, así
   que nada lo delata.
3. **Tienta a inventar reglas.** Este es el grande. En cuanto existe una columna «media
   jornada», alguien acaba proponiendo avisar cuando pase de cuatro horas — y eso **rechazaría
   horas extra legítimas**, que es exactamente lo que la decisión 3 de este mismo ADR prohíbe.

> **Cambio registrado:** contradice el párrafo de `cargo` en [[modelo-datos]], que a su vez
> venía de [[adr-006-dashboard-y-turnos-multiples]] y [[adr-007-seguridad-endpoint-publico]].
> Esas páginas quedan matizadas, no borradas: la necesidad que describían —«no todas son
> podólogas»— era real, pero se resolvía con un dato que el sistema no usa.

## 2. Exportar a Excel, diaria **y** semanal, por sede

> «El software debe poder exportar tanto diariamente como semanalmente en Excel por sede,
> verificar eso.»

**Verificado. El lado de los datos está completo; el generador del archivo no existe todavía.**

| Pieza | Estado |
|---|---|
| `sp_cronograma_semanal(sede, lunes)` — 7 días, rejilla completa | ✅ escrito y probado, **4 ms** |
| `sp_cronograma_diario(sede, fecha)` — un día, **mismas columnas** | ✅ escrito y probado, **2 ms** |
| Las 7 sedes devuelven su rejilla | ✅ 14 personas × 7 días = 98 filas cada una |
| **El archivo `.xlsx` en sí (Apache POI)** | ❌ **no existe**. Va en la capa `control` |

**Los dos procedimientos devuelven exactamente las mismas columnas a propósito.** Así el
generador de Excel es **un solo código** y lo único que cambia es cuántas columnas de fecha
pinta. Dos exportaciones, un generador que mantener.

La semanal devuelve la **rejilla completa**, no solo los días con marcaciones: cada trabajadora
por cada uno de los 7 días, con la celda vacía donde no marcó. Es lo que hace que el Excel
salga cuadrado sin que Java tenga que rellenar huecos.

> **Hueco abierto:** falta escribir el generador `.xlsx` y añadir la dependencia de Apache POI.
> Es trabajo de la capa `control` y está en el plan de la semana 2.

## 3. El sistema no rechaza absolutamente nada

> «Es una empresa con 7 sedes y aprox 10-20 trabajadoras por sede y posiblemente habrán más
> sedes… las trabajadoras marcan apuradas, que se le invalide su marcación sería lo peor.»

**Esta es la decisión de fondo, y cierra el hueco 27**, que llevaba abierto desde
[[adr-007-seguridad-endpoint-publico]].

### Lo que se quita

| Barrera | De dónde venía | Qué hace ahora |
|---|---|---|
| **Tope de 12 marcaciones** | ADR-007 | **Eliminado.** No hay número máximo |
| **Cooldown de 30 s** | ADR-010 | **Eliminado.** Dos envíos seguidos entran los dos |
| **Cola vencida a las 2 h** | ADR-008 | **Eliminado.** Entra y se marca el desfase |
| **Foto inválida / enorme / pesada** | ADR-007 | Se descarta **la foto**, nunca la marcación |

**No se subieron los límites: se quitaron.** Es deliberado. Un límite «muy alto» es un límite
que alguien puede volver a bajar pensando que solo hay que afinarlo; uno que no existe obliga a
justificar por qué se añade.

### Por qué el usuario tiene razón, y no es solo una preferencia

El argumento es de asimetría de daños, y aguanta:

- **Si el sistema rechaza una marcación legítima**, le borra a una persona una hora de trabajo
  real. Ella no se entera en el momento —está con prisa, entre paciente y paciente— y al final
  de la semana tiene que perseguir a la administradora para que se lo arreglen a mano. El daño
  es a la persona que sí trabajó.
- **Si el sistema acepta una marcación rara**, aparece una línea extra en una celda del
  cronograma. La administradora la ve y la resuelve en diez segundos.

Rechazar protege contra un fraude que **nadie ha reportado**; aceptar protege contra un fallo
que pasa todas las semanas.

### La contrapartida, que es la parte seria

**Un sistema que lo acepta todo y no dice nada es peor que uno estricto**, porque acepta en
silencio. Así que la decisión viene con su precio pagado:

- La tabla `intento_rechazado` **se renombra a `incidencia`**, y el cambio de nombre es el
  cambio de fondo: ya no guarda lo que el sistema impidió, sino **lo que aceptó y no entendió
  del todo**.
- Cada incidencia **apunta a la marcación que sí se guardó** (`marcacion_id`), para que el
  dashboard la enseñe junto a la hora y no en una lista aparte que nadie abre.
- Tiene columna `revisada`, para que la lista no crezca sin fin y se convierta en un sitio al
  que nadie entra.
- `sp_dia_sede` devuelve `incidencias_pendientes` por persona y día.

**El sistema pasa de decidir a informar.** Quien decide es una persona, que es quien puede
llamar a la sede y preguntar qué pasó.

### La única barrera que sobrevive, y no protege contra la trabajadora

`FOTO_SOSPECHOSA`: una imagen que declara más de 5000×5000 px se descarta **sin decodificar**.
No es una regla de negocio — es la bomba de descompresión: un archivo pequeño que al
decodificarse ocupa gigabytes y **deja sin servicio a las otras 98 personas**. La marcación se
guarda igual, sin foto.

Dicho de otro modo: la única cosa que este sistema sigue rechazando es **un archivo**, nunca
una hora trabajada.

## Lo que esto cambia en el esquema

- **`etiqueta` pasa de `VARCHAR(160)` a `VARCHAR(400)`.** Con el tope de 12 la celda no podía
  pasar de ~120 caracteres. Sin tope, puede. El síntoma habría sido el peor posible: **MySQL
  cortándola en silencio**. Hay prueba (`laEtiquetaNoDesbordaLaColumna`) y Java corta por línea
  entera antes de llegar ahí.
- **`sp_validar_marcacion` → `sp_contexto_marcacion`.** Ya no valida nada, así que tampoco se
  llama así. Devuelve la cuenta del día —para el contador— y el último tipo —para
  preseleccionar el botón—. **Sugiere, no juzga.**
- **`sede.id` pasa a autoincremental.** El usuario dice que «posiblemente habrán más sedes»:
  dar de alta la octava tiene que ser un `INSERT`, no una migración.
- **Ni un `CHECK`, ni un `UNIQUE`, ni un `SIGNAL`** en toda la ruta de una marcación. Se puede
  comprobar leyendo `SQL/01_esquema.sql`.

## ¿Y no se satura?

El usuario lo pregunta expresamente. **Medido, no estimado**, sobre el juego de datos nuevo
—99 trabajadoras, 3 943 marcaciones—:

| Consulta | Tiempo |
|---|---|
| `sp_contexto_marcacion` — corre en **cada** marcación | **0,7 ms** |
| `sp_cronograma_diario` — Excel diario de una sede | 2,1 ms |
| `sp_dia_sede` — pestaña del dashboard | 3,0 ms |
| `sp_cronograma_semanal` — Excel semanal de una sede | 4,2 ms |

**El cuello de botella de este sistema no es la base de datos.** Una sola conexión haría más de
mil marcaciones por segundo; el pico real son ~100 personas en unos minutos. Con 10 conexiones
en el pool sobra de largo, y quitar el cooldown no mueve la aguja.

**Lo que sí puede tumbarlo es decodificar fotos**, porque cada imagen ocupa memoria de verdad
mientras se procesa. Esa es la medida que importa y sigue en pie:
`foto.decodificaciones-simultaneas: 4`. Una ráfaga **hace cola en vez de reventar la memoria** —
y hacer cola no es rechazar: la marcación ya está guardada antes de que la foto se procese.

> **Consecuencia de diseño que esto obliga:** la marcación tiene que guardarse **antes** de
> procesar la foto, no después. Si se procesara primero, una cola de fotos sería una cola de
> personas esperando, y eso es justo lo que el usuario no quiere. Va a la capa `control`.

## Huecos que este ADR cierra y abre

- **Cierra el hueco 27** (¿cae también el tope de 12?): sí, cae.
- **Cierra el hueco 28** (¿se usa `cargo` en algún sitio?): no, y por eso desaparece.
- **Abre:** ¿cada cuánto revisa la administradora las incidencias pendientes? Si nadie las mira,
  toda esta decisión se queda a medias.
- **Abre:** falta el generador `.xlsx`.

## Enlaces

[[modelo-datos]] · [[adr-007-seguridad-endpoint-publico]] · [[adr-008-alcance-interfaz-y-cola-sin-senal]] ·
[[adr-010-sin-rechazos-oracle-y-nombre-libre]] · [[adr-011-jpa-o-sql-a-mano]] ·
[[marcacion-multiple]] · [[formato-cronograma-excel]] · [[huecos-abiertos]]
