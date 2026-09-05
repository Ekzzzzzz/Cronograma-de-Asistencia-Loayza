---
tipo: dominio
titulo: Jornadas múltiples — varias marcaciones al día y rotación entre sedes
tags: [dominio, marcacion, jornada, reglas]
creado: 2026-09-05
actualizado: 2026-09-05
estado: activo
---

# Jornadas múltiples — varias marcaciones al día y rotación entre sedes

## La regla nueva

Una trabajadora puede **entrar y salir de una sede y luego entrar y salir de otra el mismo
día** *(aporte del usuario, 2026-09-05)*. El supuesto de «dos marcaciones diarias» que
sostenía [[problema-cronogramas]] queda descartado.

**Límite: 6 marcaciones por trabajadora y día**, fijado por el usuario para evitar que
alguien intente saturar la aplicación. Son hasta tres jornadas en un día.

## Qué rompe

### 1. La inferencia de entrada o salida

[[adr-005-interfaz-de-marcacion]] infería el tipo por el orden: la primera del día es
entrada, la segunda salida. Con seis marcaciones posibles y cambio de sede, eso ya no basta
— y falla justo en el caso más probable: **que olvide marcar una salida**.

Regla que lo sustituye *(propuesta del agente)*:

> Si la trabajadora tiene una **jornada abierta** (una entrada sin su salida), la siguiente
> marcación es **la salida de esa jornada**, en esa sede. Si no tiene ninguna abierta, es
> una **entrada**.

Y en todos los casos, la pantalla de confirmación **dice cuál es** antes de enviar, con la
sede a la que corresponde, para que ella pueda corregirlo en el acto.

Caso especial: marca entrada en Miraflores, olvida la salida, y luego marca en Lince. El
sistema propondría «salida de Miraflores», que es falso. Ella lo ve escrito y lo corrige a
«entrada en Lince»; la jornada de Miraflores queda **sin cerrar** y va a la cola de revisión
para que la administradora le ponga hora de salida. Ninguna de las dos cosas se inventa.

### 2. El formato del cronograma

Es la consecuencia más incómoda *(propuesta del agente)*.

La rejilla de [[formato-cronograma-actual]] tiene **una celda por trabajadora y día**, con
un solo rango horario. No puede expresar que Daniela trabajó de 10:00 a 14:00 en Miraflores
y de 15:00 a 20:00 en Lince: son dos sedes distintas, y los cronogramas son **por sede**.

Opciones, sin decidir:

| Opción | Cómo se ve | Coste |
|---|---|---|
| **Aparece en ambas rejillas** | En Miraflores `10:00 - 14:00`, en Lince `15:00 - 20:00` | Los conteos MAÑANA/TARDE la cuentan dos veces si no se ajustan |
| **Dos rangos en una celda** | `10:00-14:00 / 15:00-20:00` | Rompe el formato actual y no dice en qué sede fue cada uno |
| **Rejilla + anexo de rotaciones** | La celda muestra la jornada de esa sede y una hoja aparte lista los movimientos | Más fiel, pero es un formato nuevo |

La primera es la que menos altera lo que ya conocen, y es la que se recomienda — con la
salvedad de recalcular los conteos por **presencia efectiva en esa sede y turno**, no por
número de filas.

### 3. Los conteos MAÑANA y TARDE

Si una persona está media jornada en cada sede, contarla entera en las dos infla la
dotación. Con los conteos recalculados por el sistema
([[adr-004-llenado-del-cronograma]]) esto se resuelve, pero hay que decidir la regla:
¿cuenta en el turno donde estuvo, o a prorrata? **Pregunta abierta.**

## El límite de 6 como defensa

Fijar un tope no es sólo prudencia técnica: es una **verificación antifraude** más para la
cadena de [[antifraude]].

- Un tope de 6 por trabajadora y día corta el intento de saturar la aplicación enviando
  marcaciones en cadena.
- Conviene acompañarlo de un **intervalo mínimo entre marcaciones** — un par de minutos —
  que además evita el doble envío accidental por tocar dos veces el botón, que con estas
  usuarias es más probable que un ataque.
- La marcación número 7 **no se descarta en silencio**: se rechaza con un mensaje claro y
  queda registrada como intento, para que la administradora lo vea si se repite.

## Preguntas abiertas

1. ¿Con qué frecuencia ocurre realmente la rotación entre sedes en un mismo día? Si es
   excepcional, conviene tratarla como excepción visible; si es habitual, el formato del
   cronograma tiene que cambiar de verdad.
2. ¿Los conteos MAÑANA/TARDE cuentan a la persona entera o a prorrata?
3. ¿Hay una duración mínima de jornada por debajo de la cual algo está mal? Sirve para
   detectar la entrada y salida seguidas ([[antifraude]], vector 7).

## Enlaces

- [[adr-006-acceso-sin-sesion]] · [[adr-005-interfaz-de-marcacion]]
- [[formato-cronograma-actual]] · [[problema-cronogramas]] · [[dominio-moc]]
