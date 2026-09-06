---
titulo: Formato del cronograma en Excel
tipo: dominio
estado: estable
fuentes:
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-05
tags: [dominio, excel, exportacion]
---

# Formato del cronograma en Excel

Este es el formato que la empresa ya usa y que la exportación de [[requisitos]] RF-13 debe
reproducir. Estructura verificada descomprimiendo `docs/Cronograma_Ejemplo.xlsx`
(una sola hoja de datos, `Hoja1`, rango `B2:I…`).

## Estructura

| Fila | Contenido |
|---|---|
| 2 | Mes, celdas `B2:I2` combinadas. En el ejemplo: `MAYO` |
| 3 | Título, celdas `B3:I3` combinadas: `HORARIO PERSONAL LOS OLIVOS` |
| 4 | Encabezados: `B4 = PODOLOGA`, y `C4:I4` los siete días |
| 5+ | Una fila por podóloga: `B` el nombre, `C:I` la jornada de cada día |

Los encabezados de día llevan día de semana **y número**: `LUNES 25`, `MARTES 26`,
`MIERCOLES 27`, `JUEVES 28`, `VIERNES 29`, `SABADO 30`, `DOMINGO 31`. Es decir, **la tabla
es semanal** (una semana de lunes a domingo), con el mes como encabezado superior — no es
una tabla mensual completa.

## Contenido de las celdas de jornada

Cada celda es **texto**, no hora. Dos formas posibles:

1. **Jornada trabajada**: `entrada - salida`, por ejemplo `10:12AM - 8:46PM`.
2. **Marcador de estado**, uno de: `DESCANSO`, `NO TURNO`, `INASISTENCIA`.

Podólogas presentes en el ejemplo: DANIELA, LEYDI, NADIA, NERY, CARMEN, DORA, ROXANA,
JAZMIN, EVELYN, JESSICA CASAFRANCA, ALANA JAUDI, MARIA TARAZONA, JANET.

## Irregularidades del archivo real

Importan porque el exportador debe **producir** este formato y quizá **leer** archivos
viejos como estos:

- Horas sin minutos: `9:57AM - 8PM`, `3PM - 8:24PM`.
- Separador equivocado: `10.02AM - 3:55PM` (punto en vez de dos puntos).
- Espacio final sobrante: `2:52PM - 8:37PM `.
- Nombres inconsistentes: unas con apellido (`JESSICA CASAFRANCA`) y otras sin él
  (`DANIELA`, `JANET`).

> **Inferencia:** este archivo se llena a mano. Es exactamente el trabajo manual que el
> sistema viene a eliminar, y la razón por la que el nombre de la trabajadora no debería
> ser texto libre (ver el hueco correspondiente en [[requisitos]]).

## Implicancias para el diseño

- La celda de jornada es una **derivación**, no un dato: hay que emparejar la marcación de
  entrada con la de salida de esa trabajadora, ese día y esa sede. Con
  [[marcacion-multiple]] el emparejamiento no es trivial.
- Un `DESCANSO` o `NO TURNO` es "ausencia de marcaciones", pero el sistema no puede
  distinguir por sí solo entre descanso programado, no turno e inasistencia: son tres
  estados distintos en el Excel y ninguno se deduce de los datos de marcación.

  > **Hueco:** ¿cómo se determina cuál de los tres corresponde? Requiere un cronograma
  > planificado, que hoy no existe en los requisitos. Anotado en [[huecos-abiertos]].
- La exportación es **por sede y por semana**, no por mes.
