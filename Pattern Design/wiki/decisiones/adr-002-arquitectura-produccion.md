---
titulo: ADR-002 — Arquitectura de producción y estrategia de rendimiento
tipo: decision
estado: estable
fuentes:
  - brief del usuario (2026-09-05)
  - decisiones del usuario (2026-09-06)
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [adr, decision, produccion, rendimiento, infraestructura]
---

# ADR-002 — Arquitectura de producción y estrategia de rendimiento

**Fecha:** 2026-09-06 · **Estado:** aceptada, **parcialmente superada por
[[adr-003-login-y-cronograma-simple]]**

> **Qué cambió el mismo día.** El dimensionamiento, las cinco decisiones del usuario y las
> seis medidas de rendimiento **siguen vigentes**. Lo que quedó revertido es la tabla
> `turno_planificado` y la justificación de la medida 3, porque el usuario fijó que los
> estados de ausencia se manejan a mano. Se marca en su sitio y no se reescribe la página.

## Contexto

El usuario fijó el 2026-09-06 una restricción que [[adr-001-patrones-seleccionados]] no
contemplaba: **esto no es solo un entregable de curso, tiene que quedar listo para
producción** en una empresa real con 7 sedes ([[sedes]]) y personal numeroso. El requisito
explícito es que **el sistema sea rápido en cuanto a los datos**, con fotos y generación de
tablas de cronograma de por medio.

Eso obliga a dimensionar antes de diseñar, porque la intuición sobre dónde está el cuello de
botella es equivocada.

## La escala real

Estimación a partir de datos verificados: `docs/Cronograma_Ejemplo.xlsx` lista **13
podólogas** en la sede Los Olivos ([[formato-cronograma-excel]]); con 7 sedes eso son del
orden de 100–140 trabajadoras. Con [[marcacion-multiple]] (RF-09), entre 2 y 4 marcaciones
diarias por persona:

| Magnitud | Valor estimado |
|---|---|
| Marcaciones por día | ~400 |
| Filas de `marcacion` por año | ~150 000 |
| Fotos por año | ~150 000 |
| Peso de las fotos sin comprimir (4 MB de cámara) | ~600 GB/año |
| Peso comprimidas a 1280 px, calidad 80 | **~30 GB/año** |

> **Inferencia:** las cifras de trabajadoras y de marcaciones diarias son una extrapolación
> del único Excel disponible, no un dato del cliente. El orden de magnitud es el que importa
> y no cambia aunque la cifra real doble o se reduzca a la mitad.

**La conclusión que ordena todo lo demás:** 150 000 filas al año no son nada. Cualquier
consulta sobre esa tabla se resuelve en milisegundos con el índice correcto, aunque la
empresa crezca a 30 sedes. **El cuello de botella son las imágenes**, por peso en disco y
sobre todo por tiempo de subida en red móvil. Y en segundo lugar la generación del
cronograma, que no es una consulta sino una agregación.

Añadir caché distribuida, particionado o un motor NoSQL a la tabla de marcaciones sería el
**Overengineering** que [[s14-antipatrones]] describe y que este proyecto se comprometió a
evitar.

## Decisiones

### 1. Motor de base de datos: MySQL 8

A este volumen **MySQL y PostgreSQL rinden igual**: la diferencia no se notará nunca aquí. Lo
que decide el rendimiento son los índices, el pool de conexiones y no meter fotos en la base.

Se elige MySQL 8 por criterios ajenos al rendimiento: su sintaxis de `CREATE PROCEDURE` es la
más cercana a la que enseña el curso —relevante para RC-03 y para la entrevista de
[[pc3-entregable]]—, está disponible en cualquier hosting económico y su respaldo es trivial.

Configuración obligatoria: InnoDB, `utf8mb4`, y zona horaria **`America/Lima`** fijada en el
motor, en la JVM y en la cadena de conexión.

### 2. Las fotos van al disco del servidor, con la ruta en la base de datos

**Decisión del usuario (2026-09-06).** Nunca como `BLOB`: 200 KB × 150 000 filas convertirían
una base de 30 MB en una de 30 GB, llenando el *buffer pool* de píxeles en vez de índices,
alargando cada respaldo a horas y haciendo que un `SELECT *` descuidado arrastre megabytes.

Se accede siempre a través de una interfaz `AlmacenFotos` con una única implementación
(`AlmacenFotosDisco`). Eso permite migrar a object storage —Cloudflare R2 o S3— cambiando una
clase, si el crecimiento lo pide.

### 3. Despliegue en VPS en la nube

**Decisión del usuario (2026-09-06).** Un servidor accesible desde las 7 sedes, con dominio y
**HTTPS obligatorio**: sin certificado, el navegador móvil **no concede acceso a la cámara** y
RF-05 deja de funcionar. No es un detalle de infraestructura, es un requisito funcional
encubierto.

### 4. Identidad de la trabajadora: lista desplegable cerrada

**Decisión del usuario (2026-09-06).** Cierra la pregunta 1 de [[huecos-abiertos]]. La
administradora registra a las podólogas; la trabajadora se elige de la lista de su sede.

Matiza [[requisitos]] RF-02, que decía "escribir el nombre". El motivo está documentado en
[[formato-cronograma-excel]]: el Excel actual mezcla `DANIELA` con `JESSICA CASAFRANCA`, y
texto libre produciría tres personas distintas donde hay una. La foto sigue siendo la
evidencia de quién marcó realmente.

### 5. Retención: 3 meses en tamaño completo, miniatura para siempre

**Decisión del usuario (2026-09-06).** Acota el disco a ~8 GB permanentes en vez de crecer
sin límite, y conserva constancia visual de toda marcación histórica.

Implica una tarea programada de purga y una columna `foto_purgada` en `marcacion`, para que
la interfaz sepa distinguir "no hay foto" de "la foto completa expiró".

## Estrategia de rendimiento

Seis medidas, ordenadas por impacto real medido contra la escala de arriba.

### 1. Comprimir la foto en el navegador antes de subirla

**La medida de mayor impacto de todas.** Una foto de 4 MB por red móvil son 15–30 segundos de
espera; redimensionada con `canvas` a 1280 px queda en ~200 KB y sube en 1–2 segundos. Con
[[requisitos]] RNF-01 —usuarias con poca familiaridad tecnológica— esa espera es la diferencia
entre que marquen y que abandonen.

**No compromete la garantía anti-fraude** de [[adr-001-patrones-seleccionados]]: el sello de
fecha y hora lo sigue poniendo el servidor con `LocalDateTime.now()`. El cliente solo entrega
píxeles más livianos, nunca la hora.

### 2. Dos tamaños en la misma pasada del Decorator

El sellado de [[patron-decorator]] ya decodifica y recodifica la imagen; emitir además un
thumbnail de ~200 px y ~10 KB en ese mismo momento no cuesta una segunda decodificación.

Esto le da al **Virtual Proxy** de [[patron-proxy]] una justificación de producción y no de
aula: el dashboard lista miniaturas y trae la foto completa solo cuando alguien la abre.

### 3. Tabla resumen `jornada_diaria`

Una fila por (trabajadora, sede, fecha). Ver [[modelo-datos]].

~~**No entra como optimización, entra por corrección.** `DESCANSO`, `NO TURNO` e
`INASISTENCIA` son ausencia de marcaciones con tres significados distintos y **no se pueden
derivar de los datos**. Esa tabla es donde persiste el resultado de [[patron-state]].~~

> **Justificación corregida por [[adr-003-login-y-cronograma-simple]].** Esos tres estados
> salieron del alcance y State fue descartado, así que la tabla ya no persiste ninguna máquina
> de estados. **Sigue existiendo por otra razón:** emparejar N entradas con N salidas es
> lógica de dominio y vive en `Jornada` → `Turno`. Sin la tabla resumen habría que reescribir
> ese emparejamiento en SQL con funciones de ventana — la misma regla en dos lenguajes. Ver
> [[modelo-datos]].

Que además convierta el cronograma semanal en un `SELECT` plano de ~91 filas, en vez de una
agregación sobre miles de marcaciones, es beneficio gratis.

### 4. Columna `fecha` generada e indexada

Filtrar con `DATE(momento) BETWEEN ...` descarta el índice y fuerza un escaneo completo. Con
una columna `DATE` generada y almacenada, más el índice `(sede_id, fecha, trabajadora_id)`,
la misma consulta es un *range scan*. Decide si la exportación tarda 3 ms o 300 ms cuando haya
tres años de historia.

### 5. Zona horaria `America/Lima` explícita en las tres capas

Perú es UTC-5 sin horario de verano. Sin fijarla, el sello sale con la hora del servidor y las
jornadas nocturnas se parten en el día equivocado. Es el bug que no se ve hasta que el sistema
ya acumuló evidencia legal errónea.

### 6. Acotar el procesamiento de imágenes

Las marcaciones llegan en picos —apertura y cierre de las 7 sedes—, no de forma sostenida. El
sellado es trabajo de CPU: conviene un pool de hilos acotado para el procesamiento, de modo
que un pico de 30 fotos simultáneas encole en vez de saturar el servidor.

## Consecuencias

- **[[requisitos]] RF-02 queda matizado**: el nombre se elige, no se escribe. Hay que añadir
  al alcance el registro de trabajadoras por parte de la administradora, que el brief no
  mencionaba.
- **Aparece un requisito no funcional nuevo**: HTTPS, por la cámara.
- ~~**Aparece una entidad nueva en el dominio**: `turno_planificado`, sin la cual el
  cronograma no puede distinguir los tres estados de ausencia.~~ **Revertido el mismo día por
  [[adr-003-login-y-cronograma-simple]]**: esos casos se manejan a mano, así que la tabla no
  llegó a existir.
- **Aparece una tarea programada**: la purga de fotos a los 3 meses.
- El diseño de [[patron-composite]] cambia: se introduce el nivel `Turno`. Ver
  [[marcacion-multiple]]. ([[patron-state]] fue descartado poco después.)
- Se cierran las preguntas 1, 2 (parcialmente) y 9 de [[huecos-abiertos]].

## Enlaces

[[adr-001-patrones-seleccionados]] · [[modelo-datos]] · [[marcacion-multiple]] ·
[[cuatro-capas]] · [[huecos-abiertos]]
