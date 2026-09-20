---
titulo: Marcación múltiple
tipo: dominio
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [dominio, regla-de-negocio, nucleo]
---

# Marcación múltiple

La regla central del sistema y la que rompe cualquier diseño ingenuo de control de asistencia.
Enunciado en [[requisitos]] RF-09: **una trabajadora marca varias veces al mismo día, en sedes
distintas, y todas las marcaciones quedan registradas.**

Ningún cálculo del sistema puede asumir una entrada y una salida por día.

## Los tres ejes que se multiplican

Una podóloga en un día cualquiera puede producir:

1. **Varios turnos en la misma sede.** Entra a las 9, sale a la 1, vuelve a las 3 y sale a las
   8. Son dos turnos en una sola celda del cronograma.
2. **Turnos en sedes distintas.** Mañana en Los Olivos, tarde en Miraflores. Son dos celdas,
   en dos pestañas distintas del dashboard.
3. **Ambas cosas a la vez.**

## Cómo se refleja en el Excel actual

[[formato-cronograma-excel]] es **una tabla por sede**: `HORARIO PERSONAL LOS OLIVOS`, con las
podólogas en filas y los días en columnas. Eso resuelve el eje 2 sin ambigüedad: una
trabajadora que rota entre dos sedes aparece en **dos tablas**, cada una con su propia celda
para ese día. No hay conflicto, y confirma que la unidad natural del cronograma es
**(trabajadora, sede, fecha)** — que es justamente la clave de `jornada_diaria` en
[[modelo-datos]].

El eje 1 quedó resuelto el 2026-09-06 en [[adr-006-dashboard-y-turnos-multiples]]: la celda
lleva **un par por línea, hasta seis**.

```
9:00AM - 1:00PM
3:00PM - 8:00PM
```

> **Corrección de una decisión anterior.** [[adr-003-login-y-cronograma-simple]] había fijado
> lo contrario —colapsar a *primera entrada – última salida*— por una mala lectura de la
> indicación del usuario. Ese colapso declaraba 11 horas donde se trabajaron 9, algo que esta
> misma página ya había advertido. La corrección restituye lo que se recomendaba de entrada.

**Tope duro de 6 pares por día y sede**, es decir 12 marcaciones. **No es un límite de formato
sino una defensa** ([[adr-007-seguridad-endpoint-publico]]): el endpoint es público y sin
autenticación, así que sin tope alguien podría marcar cientos de veces para inutilizar el
cronograma. En la práctica **no se ven más de 3 pares**, así que seis dan el doble de holgura
antes de estorbar a nadie.

La marcación número 13 **se rechaza** y queda anotada en `intento_rechazado`, visible para la
administradora. Si el caso fuese legítimo, se arregla por la corrección manual de
[[adr-006-dashboard-y-turnos-multiples]] — que pasa por una persona, no por el endpoint
abierto.

`minutos` suma **turno a turno**, nunca resta los extremos, así que el total de horas es
correcto aunque la jornada esté partida. Ver [[modelo-datos]].

## Lo que ve la trabajadora: pares, no el tope

La pantalla de confirmación muestra **«Marcación 1 de 2 de hoy»**; al marcar la salida pasa a
**«2 de 2»**; si más tarde abre otro turno, **«3 de 4»**. El denominador es siempre
`redondear_a_par(n)` — el par en curso — **nunca el máximo de 12**.

Dos motivos, y el segundo no es de interfaz:

1. **Es lo que ella entiende.** Una jornada son pares de entrada y salida, no una cuota que se
   gasta. «1 de 12» sugiere un presupuesto diario que no existe.
2. **El formulario público no debe anunciar su propio tope.** Mostrar el 12 en un endpoint sin
   autenticación es decirle a cualquiera cuánto aguanta el sistema antes de rechazar
   ([[adr-007-seguridad-endpoint-publico]]). El 12 aparece **únicamente en el mensaje de
   rechazo**, cuando ya se alcanzó y ocultarlo no serviría de nada.

> **El tope duro no cambia:** siguen siendo 12 marcaciones por (trabajadora, sede, día) y la
> número 13 se rechaza. Lo que cambia es que el endpoint deja de publicarlo.

## Corrección al diseño de los patrones

[[patron-composite]] se escribió antes de modelar esta regla y su jerarquía **no puede
representar una jornada partida**: `Jornada` agrega marcaciones directamente, así que dos
pares entrada-salida en el mismo día y sede se mezclan sin frontera entre ellos.

**La jerarquía necesita un nivel más:**

```
SemanaSede            compuesto — una pestaña del dashboard, una tabla de Excel
└── DiaSede           compuesto — una columna: LUNES 25
    └── Jornada       compuesto — una celda: (trabajadora, sede, fecha)
        └── Turno     compuesto — un par entrada-salida
            └── Marcacion   hoja — una entrada o una salida, con su foto
```

Reparto de responsabilidades, que además elimina una duplicación:

- **`Turno` es el par entrada-salida** y el dueño de la regla. Está abierto mientras
  `salida == null`; al llegar la salida se cierra y ya no admite más marcaciones. Una entrada
  posterior abre un turno **nuevo** en lugar de reabrir el anterior.
- **`Jornada` solo agrega turnos** y produce la etiqueta de la celda: **una línea por turno**,
  hasta seis ([[adr-006-dashboard-y-turnos-multiples]]).
- **La validación vive en un solo sitio: `Turno`.** Eso cierra la tensión que había entre
  `Marcacion.validar()` en [[patron-factory]] y la máquina de estados que se proponía en
  [[patron-state]]. Duplicarla era *Shotgun Surgery* ([[s14-antipatrones]]); ahora
  `Marcacion` vuelve a ser lo que debe ser — un hecho inmutable, sin comportamiento de
  validación.

> **Por qué esto no es [[patron-state]].** El borrador anterior modelaba el turno como una
> máquina de estados. Con [[adr-003-login-y-cronograma-simple]] quedan solo dos situaciones
> —abierto y cerrado— y [[s12-state-observer]] advierte que State "puede ser excesivo si solo
> hay pocos estados". Un `salida == null` es más limpio que dos clases de estado. El patrón
> se descartó; el razonamiento completo está en el ADR-003.

## Nada se rechaza por su secuencia

**Cambio del 2026-09-20** ([[adr-010-sin-rechazos-oracle-y-nombre-libre]]). El sistema **acepta cualquier
marcación**: doble entrada, doble salida, dos seguidas en la misma sede. `Turno` sigue
infiriendo si abre o cierra, pero esa inferencia **sugiere, no bloquea**.

Los pares se arman **en orden cronológico** y lo que sobra **queda a la vista**:

| Lo que llegó | Lo que muestra la celda |
|---|---|
| Entrada sin salida | `10:12AM -` |
| Salida sin entrada previa | `- 8:46PM` |
| Dos entradas seguidas | La primera queda abierta; la segunda abre turno nuevo |

> **El sistema registra lo que ocurrió; interpretarlo es de la administradora.** Es la misma
> disciplina que ya rige para `DESCANSO` e `INASISTENCIA`
> ([[adr-003-login-y-cronograma-simple]]): un registro de asistencia que corrige a la persona
> deja de ser un registro.

## Las reglas, enunciadas

Sobre una `Jornada` identificada por (trabajadora, sede, fecha):

1. Una **ENTRADA** es válida si no hay ningún turno abierto en esa jornada. Abre un turno.
2. Una **SALIDA** es válida si hay exactamente un turno abierto. Lo cierra.
3. Una ENTRADA con un turno ya abierto **en la misma sede** se rechaza: es un doble marcado.
4. Una ENTRADA en una **sede distinta** teniendo un turno abierto en otra es válida — son dos
   jornadas independientes — pero deja el turno anterior sin cerrar.
5. Un turno que llega al cierre del día sin salida se queda abierto. No hay estado especial:
   `ultima_salida IS NULL` lo dice todo.

La regla 4 es la consecuencia incómoda de la marcación múltiple: **el sistema no puede
distinguir** entre una podóloga que olvidó marcar la salida y una que se fue corriendo a otra
sede. Ambas producen lo mismo, y ninguna es una falta laboral que el sistema deba declarar —
por [[adr-003-login-y-cronograma-simple]] eso es criterio de la administradora, a mano.

> **Inferencia:** con la celda reducida a horas, una jornada sin salida debería mostrar
> `10:12AM -`, con el guion y el hueco a la vista. Dejarla vacía la haría indistinguible de
> un día sin marcaciones, y la administradora perdería la señal de que falta un dato.
> **Falta confirmarlo**; es un cambio de una línea en el formateador si se prefiere otra cosa.

> **Resuelto el 2026-09-06** por [[adr-006-dashboard-y-turnos-multiples]]: la administradora
> **sí** puede añadir la salida olvidada, y lo hace **insertando una marcación nueva** con
> `origen = ADMINISTRADORA`, sin foto y con motivo obligatorio. No edita ni borra nada, así que
> la inmutabilidad se conserva y además se gana trazabilidad: consta quién la puso y por qué.

## Por qué esto complica el emparejamiento

El cálculo de horas no es `última salida − primera entrada`. Con turnos partidos hay que sumar
turno a turno, que es exactamente lo que `horasTrabajadas()` recorre hacia arriba en
[[patron-composite]] — y ahora el recorrido tiene sentido, porque la duración nace en `Turno`
y no en una hoja que devolvía `Duration.ZERO`.

Ese `Duration.ZERO` de `Marcacion` era además una violación de **ISP**: la interfaz obligaba a
la hoja a implementar una operación que no le correspondía. Con el nivel `Turno` la
responsabilidad queda donde debe, y **el antes/después es evidencia directa** para el capítulo
de principios ISP que exige [[pc3-entregable]] (RC-05).

## Enlaces

[[requisitos]] · [[modelo-datos]] · [[formato-cronograma-excel]] · [[patron-composite]] ·
[[patron-state]] · [[patron-factory]] · [[adr-002-arquitectura-produccion]]
