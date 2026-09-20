---
titulo: ADR-006 — Dashboard, turnos múltiples en la celda y cargos
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-06)
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [adr, decision, dashboard, cronograma, dominio]
---

# ADR-006 — Dashboard, turnos múltiples en la celda y cargos

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

El usuario detectó tres huecos al revisar la presentación de avance:

1. **El dashboard nunca se especificó.** Todo el wiki decía "una pestaña por sede" y nada más,
   pese a ser la mitad del sistema y la cara que usa quien paga.
2. **La celda del cronograma no puede colapsar los turnos.** Una trabajadora que rota entre
   sedes genera varios pares entrada-salida en el mismo día, y **todos tienen que verse**.
3. **No todas las trabajadoras son podólogas.** También hay **cajeras**, un dato que no
   aparecía en ninguna fuente previa.

## Decisiones

### 1. La celda del cronograma lleva hasta seis pares, uno por línea

**Esto revierte la decisión 1 de [[adr-003-login-y-cronograma-simple]]**, que colapsaba los
turnos partidos a *primera entrada – última salida*.

> **Fue una mala lectura mía, y estaba advertida.** Cuando el usuario dijo que la celda lleva
> "netamente la hora de ingreso más la hora de salida", lo interpreté como un solo rango. El
> propio ADR-003 dejó anotado que eso "declara 11 horas donde se trabajaron 9", y
> [[marcacion-multiple]] lo había señalado antes como el riesgo del colapso. La corrección
> restituye lo que ambas páginas ya recomendaban.

Formato nuevo de la celda: **un par por línea**, con salto de línea dentro de la celda y ajuste
de texto activado.

```
9:00AM - 1:00PM
3:00PM - 8:00PM
```

- **Límite: 6 pares por día y sede**, es decir hasta 12 marcaciones. Es el caso extremo de
  rotación que el usuario fija como tope.
- Un séptimo par **no se rechaza ni se pierde**: se registra igual y la jornada queda marcada
  para que la administradora la revise. Perder una marcación real es peor que romper el
  formato.
- Se mantiene lo que ya estaba bien: `minutos` **suma turno a turno**, así que el total de
  horas nunca depende de restar los extremos de la celda.

**Consecuencia en [[modelo-datos]]:** `jornada_diaria.etiqueta` pasa de `VARCHAR(40)` a
`VARCHAR(160)` — seis pares con sus separadores no caben en cuarenta caracteres.

### 2. Las trabajadoras tienen cargo

Aparece `trabajadora.cargo`, con valores **`PODOLOGA`** y **`CAJERA`**. En el cronograma
**van todas en la misma tabla, con una columna de cargo**; no se parten en hojas separadas.

Eso matiza [[formato-cronograma-excel]], cuyo encabezado de columna dice `PODOLOGA`: pasa a ser
`TRABAJADORA`, y se añade la columna `CARGO`.

> **Se modela como `ENUM` y no como tabla de catálogo.** Con dos valores, una tabla aparte con
> su clave foránea sería la ceremonia que este proyecto se comprometió a evitar. Añadir un
> tercer cargo es un `ALTER TABLE` de una línea; si algún día hay cinco o más, entonces sí
> conviene la tabla. Anotado como escape, no como deuda.

### 3. El dashboard tiene seis funciones, no una

Hasta ahora solo existía "ver una pestaña por sede". Queda especificado así:

| Función | Qué hace |
|---|---|
| **Vista diaria** | Una sede y un día: cada trabajadora en una fila, con **todos sus turnos** y sus horas sumadas |
| **Vista semanal** | La tabla del cronograma, de lunes a domingo, con el formato de [[formato-cronograma-excel]] |
| **Exportar a Excel** | La vista semanal, en el formato que la empresa ya usa |
| **Corregir una marcación** | Añadir a mano una salida olvidada. Ver la decisión 4 |
| **Historial de una trabajadora** | Sus marcaciones en un rango de fechas, **cruzando las 7 sedes** |
| **Totales de horas** | Suma por trabajadora y por sede en un rango. El dato que el Excel a mano nunca dio |

**La vista diaria es una sede a la vez**, no las siete en paralelo: con siete columnas la
pantalla queda ilegible, y la unidad natural del negocio es (sede, día) — la misma que la clave
de `jornada_diaria`.

**El historial es la única vista que cruza sedes.** Todas las demás filtran por una. Eso tiene
sentido: el cronograma es por sede, pero un reclamo por horas es por persona.

### 4. Las correcciones son marcaciones nuevas, no ediciones

Cierra la pregunta 15 de [[huecos-abiertos]]. Si una trabajadora olvidó marcar la salida, la
administradora la añade — pero **no se edita ni se borra nada**:

- Se **inserta una marcación nueva** con `origen = ADMINISTRADORA`, sin foto, con la hora que
  indique la administradora y un motivo obligatorio.
- `marcacion.foto_ruta` pasa a **admitir nulo**, únicamente para este caso.
- Queda registrado **quién la puso** (`corregida_por`) y **por qué** (`motivo`).
- En el dashboard y en la exportación, una marcación puesta a mano **se distingue
  visualmente** de una con foto.

Así se conserva la regla de [[modelo-datos]] —las marcaciones son hechos inmutables que se
insertan y nunca se actualizan— sin renunciar a arreglar un olvido. Y la trazabilidad se gana
en vez de perderse: se ve que hubo corrección, quién la hizo y con qué justificación.

## Consecuencias

- **[[requisitos]] crece**: RF-16 (vista diaria), RF-17 (corrección con trazabilidad), RF-18
  (historial por trabajadora), RF-19 (totales de horas), RF-20 (cargo de la trabajadora).
- **[[modelo-datos]] cambia** en tres tablas: `cargo` en `trabajadora`; `origen`,
  `corregida_por`, `motivo` y `foto_ruta` nulable en `marcacion`; `etiqueta` a `VARCHAR(160)`
  en `jornada_diaria`.
- **Aparecen tres procedimientos** nuevos: `sp_dia_sede`, `sp_historial_trabajadora` y
  `sp_totales_horas`.
- **El diagrama de casos de uso queda incompleto** con solo cuatro casos de administradora:
  hay que rehacerlo con los seis.
- **[[formato-cronograma-excel]] cambia de encabezado** y gana la columna de cargo.
- **Sube el peso del tramo 2** del plan: el dashboard pasa de una pantalla a seis funciones.
  Sigue cabiendo, pero deja de ser el tramo ligero.

## Lo que no cambia

- El sistema sigue **sin escribir** `DESCANSO`, `NO TURNO` ni `INASISTENCIA`
  ([[adr-003-login-y-cronograma-simple]] decisión 1 sigue vigente en esa parte).
- Sigue **sin haber patrón State**: seis pares por día no son seis estados, son seis
  instancias de `Turno`. El descarte se sostiene.
- La jerarquía `Jornada → Turno → Marcacion` de [[marcacion-multiple]] **ya soportaba esto**.
  Lo que estaba mal era solo cómo se renderizaba la celda, no el modelo.

## Enlaces

[[adr-003-login-y-cronograma-simple]] · [[marcacion-multiple]] · [[modelo-datos]] ·
[[formato-cronograma-excel]] · [[requisitos]] · [[patron-composite]]
