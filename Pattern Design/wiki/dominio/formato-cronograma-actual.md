---
tipo: dominio
titulo: Formato del cronograma actual (Excel)
tags: [dominio, entregable, formato, calidad-de-datos]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
fuentes: ["docs/Cronograma_2026_Olivos.xlsx"]
---

# Formato del cronograma actual (Excel)

Análisis del archivo `docs/Cronograma_2026_Olivos.xlsx`, aportado por el usuario como
muestra de cómo se organizan hoy los cronogramas *(aporte del usuario, 2026-09-04)*.
Es la sede **Los Olivos**, de diciembre 2025 a julio 2026.

Esta página define el **formato de salida objetivo** del programa: no hay que inventarlo,
hay que reproducirlo. Ver [[problema-cronogramas]] y [[sintesis]].

## Estructura del archivo

- **2 hojas** (`Hoja1`, `Hoja2`), sin nombre significativo.
- **25 bloques semanales apilados verticalmente** dentro de las hojas. Cada bloque:

```
FEBRERO                                          <- etiqueta de mes (a veces "MARZO - ABRIL")
HORARIO PERSONAL LOS OLIVOS                      <- titulo con la sede
PODOLOGA | LUNES 2 | MARTES 3 | ... | DOMINGO 8  <- cabecera: nombre + 7 dias
DANIELA  | 10:20AM - 9:12PM | LIBRE | ...        <- una fila por trabajadora
...
MAÑANA   | 7 | 6 | 6 | 7 | 7 | 12 | 11           <- conteo de personal por turno
TARDE    | 12 | 11 | 10 | 12 | 12 | 17 | 13
```

- **Semana de lunes a domingo.** Los días llevan sólo el número, sin mes ni año: el año y el
  mes se infieren de la etiqueta del bloque.
- **Columna final libre**: observaciones en prosa, fuera de la rejilla.

**5.025 celdas con valor**, de las cuales **2.514 son rangos horarios** y **1.089 son
estados**.

## Contenido de una celda

Una celda de trabajadora × día contiene una de tres cosas:

### 1. Rango horario real (marcación)
`10:20AM - 9:12PM`, `9:44AM - 9:12PM`. **Es exactamente lo que el programa debe generar**
a partir de las fotos: hora de ingreso y hora de salida.

### 2. Turno planificado
`10AM-8PM` (turno completo), `3PM-8PM` (turno tarde), `10AM - 3PM` (medio turno mañana).

> [!important] Hallazgo
> Los turnos planificados aparecen **sólo en el primer bloque** (semana del 26 dic).
> Desde febrero, **todas** las celdas son marcaciones reales. Es decir: el archivo dejó de
> ser un cronograma *previsto* y se convirtió en un **registro de asistencia real**
> transcrito a mano desde las fotos de WhatsApp. Eso es precisamente lo que se automatiza.

En las notas se usan las abreviaturas `TC` (turno completo) y `MT` (medio turno).

### 3. Estado (sin horario)

| Estado | Veces | Significado aparente |
|---|---:|---|
| `NO TURNO` | 589 | No le tocaba trabajar ese día |
| `DESCANSO` | 192 | Día de descanso semanal |
| `LIBRE` | 92 | Lo mismo que DESCANSO — ver deriva abajo |
| `VACACIONES` | 70 | Periodo vacacional |
| `INASISTENCIA` | 59 (+13 mal escritas) | No vino sin aviso |
| `PERMISO` | 45 | Ausencia autorizada |
| `FERIADO` | 42 | Día feriado (aplica a la columna entera) |

`NO TURNO` y `DESCANSO`/`LIBRE` **no son lo mismo**: el primero es «no programada», el
segundo es «su día de descanso». La distinción importa para los conteos.

## Problemas del proceso manual (evidencia)

Todos verificados sobre el archivo. Son la justificación del proyecto y, a la vez, los
requisitos de calidad que el programa debe cumplir.

1. **Los conteos al pie están congelados.** La fila `TARDE` tiene el valor idéntico
   `11 9 11 9 11 18 17` en **22 de los 25 bloques**. Sólo los 4 primeros bloques tienen
   conteos propios. Se copió y pegó y nunca se volvió a calcular: el indicador de dotación
   por turno lleva meses siendo falso.

2. **Vocabulario que deriva en el tiempo.** `LIBRE` se usa hasta marzo, `DESCANSO` desde
   febrero en adelante, con un solapamiento donde conviven ambos. Dos palabras para el
   mismo concepto en la misma columna.

3. **Erratas en los estados.** `INACISTENCIA` (13 veces) junto a `INASISTENCIA` (59).

4. **AM/PM invertido.** Al menos 12 turnos de tarde escritos con `AM`:
   `2:45AM - 7:44PM`, `1:57AM - 8:55PM`, `12:41AM - 7:43PM`. Un turno que empieza a las
   2:45 de la madrugada y termina a las 7:44 de la tarde es imposible.

5. **Horas mal tecleadas.** `9:50AM - 7:08PMPM`, `2_43PM-8:18PM`, `9:37AM-8:022PM`,
   `2:53PM-9_12PM`.

6. **Datos faltantes.** `10AM - ??` (no se supo la hora de salida), celdas con sólo la hora
   de entrada (`10:19AM`), celdas vacías sin estado.

7. **Notas metidas dentro de la celda del horario**, con salto de línea:
   `10:24AM - 5:57PM \n PERMISO`, `9:59AM-3:27PM \n Tenia TC pero hizo MT`,
   `10:19AM \n YA ESTABA CAMBIADA EN LA FOTO SERIA 4:15 APPROX. SU SALIDA`.
   Esa última confirma que la transcripción se hace **leyendo la foto a ojo**.

8. **Filas de estructura corrompidas.** En un bloque, la fila de conteo `TARDE` contiene
   rangos horarios en vez de números: el bloque quedó desalineado.

9. **Anotaciones sueltas en columnas extra**: `TRAMPA`, `ya arreglo`, `POR REVISAR`,
   `CAMARAS`, `MOLINA PUERTA`. Sin estructura ni ubicación fija.

## Padrón de trabajadoras: no es estable

El bloque de diciembre lista **25 podólogas**; el de julio lista **21**, y no son las mismas.

- **Bajas**: LAURA ESCALANTE, MELISSA, LISSY, MARY P., LAURA BARRETO, JULY CABRERA,
  MARIANELA MONTALVO, MARINA.
- **Altas**: MISHELL, JESSICA CASAFRANCA, ALANA JAUDI, MARIA TARAZONA.

Implicación de diseño: el padrón necesita **vigencia** (fecha de alta y de baja), no una
lista fija. Y el reconocimiento facial necesita una foto de referencia por cada alta.

Los nombres se escriben de forma inconsistente: unas veces sólo el nombre de pila
(`DANIELA`, `NADIA`), otras con apellido (`KATTY DE LA CRUZ`, `JULY CABRERA`), y hay
abreviaturas (`MARY P.`). Hay dos LAURA distintas, desambiguadas por apellido. Esto es
crítico: **el nombre solo no es un identificador fiable**, lo que refuerza el valor del
reconocimiento facial como fuente de verdad.

## Sólo podólogas

La columna se titula `PODOLOGA` y no aparece ninguna **cajera** en todo el archivo. Pero el
enunciado dice que las cajeras también envían fotos ([[problema-cronogramas]]). Pregunta
abierta: ¿las cajeras tienen su propio archivo, o simplemente no se les lleva cronograma?

## Lo que esto fija para el diseño

1. **La salida objetivo es la rejilla semanal**, no un listado diario. El «cronograma
   diario» del enunciado se consolida en bloques de lunes a domingo por sede.
2. La celda es un **valor con variantes**: rango horario, estado o turno planificado. Un
   tipo de dato con varias formas — candidato natural para un patrón de comportamiento o
   creacional según cómo se resuelva *(propuesta del agente, sin decidir)*.
3. El programa debe emitir el **vocabulario de estados normalizado** (un único término por
   concepto) y los **conteos MAÑANA/TARDE siempre recalculados**.
4. Hay que conservar el canal de **observaciones en prosa**: las excepciones reales
   («cambio por el dom 8», «hizo adicional», «recupera el jueves») no caben en la rejilla y
   hoy se anotan a mano. El programa no puede inventarlas, pero sí debe dejarles sitio.
5. Debe existir un **estado explícito para «no se pudo determinar»**, que hoy se escribe
   como `??`. Es preferible una marca honesta a un dato inventado.

## Preguntas que abre

1. ¿Todas las sedes usan **este mismo formato**, o cada una tiene el suyo?
2. ¿Las **cajeras** tienen cronograma aparte?
3. `NO TURNO` vs `DESCANSO` vs `LIBRE`: ¿confirmas que LIBRE y DESCANSO son sinónimos?
4. Los conteos `MAÑANA` / `TARDE`, ¿cuentan personas presentes, o programadas?
   ¿Qué define «mañana» y «tarde» — la hora de entrada?
5. ¿Quién arma hoy este archivo y cuánto tiempo le toma por semana?
6. ¿De dónde salen los turnos **planificados**? Si el programa debe comparar lo real contra
   lo previsto (tardanzas, inasistencias), necesita esa programación como entrada.
7. ¿Qué significan las columnas `TRAMPA`, `CAMARAS` y `MOLINA PUERTA`?

## Enlaces

- [[problema-cronogramas]] — el enunciado del problema
- [[sintesis]] — estado del diseño
- [[dominio-moc]]
