---
tipo: decision
titulo: ADR-004 — Llenado del cronograma sin programación previa
numero: 004
estado_adr: propuesta
tags: [adr, cronograma, dominio]
creado: 2026-09-05
actualizado: 2026-09-05
---

# ADR-004 — Llenado del cronograma sin programación previa

## Contexto

El usuario confirma que **no existe una programación formal de turnos**: se sabe de memoria
o se coordina por WhatsApp *(decisión del usuario, 2026-09-05)*.

Eso choca con el formato de salida. La rejilla de [[formato-cronograma-actual]] no contiene
sólo horas: contiene **siete estados** que explican por qué *no* hay horas. Y algunos de
esos estados **sólo se pueden escribir si se sabe qué turno le tocaba**.

En particular, la distinción entre `NO TURNO` («no le tocaba trabajar») e `INASISTENCIA`
(«le tocaba y no vino») es imposible de hacer sin la programación. Hoy la hace una persona
que sabe de memoria quién debía venir.

## Cuánto de la rejilla puede llenar el sistema

Medido sobre el archivo real de Los Olivos, 25 semanas *(cálculo del agente)*:

| Qué | Celdas | ¿Lo llena el sistema? |
|---|---:|---|
| **Horas reales** | 2 514 | **Sí** — es exactamente lo que producen las marcaciones |
| `DESCANSO` / `LIBRE` | 284 | **Sí**, si se registra el día de descanso semanal de cada trabajadora |
| `VACACIONES` | 70 | **Sí**, si se carga como rango de fechas (una vez, no celda por celda) |
| `PERMISO` | 45 | **Sí**, igual: un rango |
| `FERIADO` | 42 | **Sí**, automático con el calendario de feriados del Perú |
| `NO TURNO` | 589 | **No** — exige saber si le tocaba |
| `INASISTENCIA` | 72 | **No** — exige saber si le tocaba |

**El sistema resuelve solo 2 955 de 3 616 celdas: el 81,7 %.**

Y el residuo es más pequeño de lo que parece: 661 celdas repartidas en 25 semanas son
**unas 26 decisiones por semana y por sede**. Frente a transcribir a ojo más de 100 horarios
semanales desde fotos de WhatsApp, es un cambio de escala.

## Decisión

**El sistema llena todo lo que puede deducir y deja el resto en un estado explícito
`SIN MARCACIÓN`, que la administradora resuelve desde una cola en el panel.**

Tres piezas ligeras de datos, que no son una programación de turnos:

1. **Día de descanso semanal por trabajadora.** Dato estable, cambia poco. Resuelve 284
   celdas.
2. **Ausencias por rango**: vacaciones y permisos se cargan una vez con fecha de inicio y
   fin. Resuelven 115 celdas sin tocar la rejilla.
3. **Calendario de feriados del Perú.** Aplica a la columna entera de todas las sedes.

Lo que queda sin marcación y sin explicación aparece en la cola como una pregunta de dos
opciones: **¿no le tocaba, o faltó?** Un toque por celda.

**El sistema nunca elige por su cuenta entre `NO TURNO` e `INASISTENCIA`.** Escribir
`INASISTENCIA` en el registro de una persona sin saberlo es una acusación; escribir
`NO TURNO` cuando sí faltó, un encubrimiento. Es exactamente el caso del principio de
`CLAUDE.md` §8: **nunca inventar un dato**.

## Justificación

- El 81,7 % automático ya elimina el trabajo pesado y, sobre todo, elimina los errores que
  documenta [[formato-cronograma-actual]]: AM/PM invertido, `7:08PMPM`, conteos congelados
  durante 22 semanas, `INACISTENCIA` mal escrita 13 veces.
- Pedir que se cargue una programación completa antes de que el sistema sirva de algo sería
  imponer trabajo nuevo a cambio de una promesa. Así el sistema es útil **desde la primera
  semana**, con datos que ya existen.
- Las tres piezas ligeras son datos que la empresa ya maneja (quién descansa qué día, quién
  está de vacaciones), no una planificación que hoy nadie hace.

## Consecuencias

**A favor**
- El sistema entrega valor sin exigir un cambio de proceso previo.
- Los conteos `MAÑANA` y `TARDE` se recalculan siempre, resolviendo el fallo más grave del
  archivo actual.
- El vocabulario queda normalizado: un solo término por concepto, sin erratas.

**En contra**
- **No se pueden detectar tardanzas ni salidas antes de tiempo.** Sin turno previsto no hay
  con qué comparar. El sistema registra que entró 10:24; que eso sea tarde o no, no lo sabe.
- Quedan ~26 decisiones manuales por semana y sede. Poco, pero no cero.
- `DESCANSO` deducido del día semanal fallará cuando cambien descansos entre ellas — algo
  que el Excel actual muestra que ocurre («Katty cambia con Janet su descanso del jueves 5»).
  Esos casos caen en la cola, que es donde deben caer.

## Puerta abierta

En cuanto exista programación —aunque sea una pantalla simple donde la administradora marque
quién trabaja cada día— se desbloquean solas tres cosas: `NO TURNO` e `INASISTENCIA`
automáticos, detección de tardanzas, y la comparación entre lo previsto y lo real.

**No se construye ahora**, pero el modelo de datos debe dejarle sitio: una `Marcacion` que ya
guarda sede, trabajadora y hora admite comparar contra un turno el día que exista.

## Estado

**Propuesta.** Pendiente de que el usuario la acepte.

## Enlaces

- [[formato-cronograma-actual]] — el formato de salida y sus siete estados
- [[problema-cronogramas]] · [[antifraude]] · [[sintesis]] · [[decisiones-moc]]
