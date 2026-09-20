---
titulo: ADR-010 — Nunca rechazar una marcación, nombre libre y Oracle
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-20)
actualizado: 2026-09-20
tags: [adr, decision, marcacion, concurrencia, oracle, exportacion]
---

# ADR-010 — Nunca rechazar una marcación, nombre libre y Oracle

**Fecha:** 2026-09-20 · **Estado:** aceptada

## Contexto

Ocho cambios pedidos por el usuario de una vez. Seis son alcance nuevo; **dos chocan con
decisiones anteriores** y se registran como tensión antes de resolverlas.

## 1. El sistema no invalida nada

> *«El sistema te permita tomar la foto y enviar al sistema en caso sea un doble ingreso o
> doble salida, siempre se tiene que poder tomar, no debe invalidar nada.»*

**Se deroga toda validación de secuencia.** Doble entrada, doble salida, dos marcaciones
seguidas en la misma sede, marcaciones en sedes distintas: **todo se acepta y se guarda**.

Esto elimina la rama «Rechazar y avisar — ya hay un turno abierto aquí» que
[[adr-003-login-y-cronograma-simple]] había puesto en el flujo.

`Turno` **sigue infiriendo** si la marcación abre o cierra ([[adr-008-alcance-interfaz-y-cola-sin-senal]]),
pero esa inferencia pasa a ser **una sugerencia que se muestra, no una condición que bloquea**.

### Cómo se emparejan las marcaciones sueltas

Al recalcular `jornada_diaria` se emparejan **en orden cronológico**. Si queda una suelta —una
entrada sin su salida, o una salida sin su entrada— **queda visible como tal** y no se inventa
nada:

| Caso | Qué guarda la celda |
|---|---|
| Entrada sin salida | `10:12AM -` |
| Salida sin entrada previa | `- 8:46PM` |
| Doble entrada | La primera queda abierta, la segunda abre turno nuevo |

> **El sistema registra lo que ocurrió; interpretarlo es de la administradora.** Es la misma
> disciplina de `DESCANSO` e `INASISTENCIA` ([[adr-003-login-y-cronograma-simple]]): un
> registro de asistencia que corrige a la persona deja de ser un registro.

### Tensión con el tope de 12 — sin resolver del todo

[[adr-007-seguridad-endpoint-publico]] fijó un **tope duro de 12 marcaciones** por
(trabajadora, sede, día) y un **cooldown de 2 minutos**, a petición expresa del usuario:
*«es importante un límite»*, *«tomar medidas de seguridad»*.

«No debe invalidar nada» y «la número 13 se rechaza» no pueden ser las dos verdad. Las dos
frases son del usuario, con dos semanas de diferencia.

**Resolución propuesta, a confirmar:**

- **Las reglas de negocio ya no rechazan nunca.** Esa es la frase nueva y se aplica entera.
- **Los límites anti-abuso se conservan**, porque el usuario los pidió expresamente y protegen
  el endpoint público, pero **dejan de ser validación de la jornada**: son defensa contra
  ataque, y así hay que nombrarlos en el informe.
- **El cooldown baja de 2 minutos a 30 segundos.** Dos minutos sí estorbarían a alguien que
  corrige un doble ingreso en el momento; 30 segundos solo paran el doble toque accidental y el
  envío automatizado.

> **Hueco:** ¿el tope de 12 también cae? Si «no debe invalidar nada» aplica sin excepción, el
> endpoint público se queda **sin ningún límite superior**, y eso es exactamente lo que
> [[adr-007-seguridad-endpoint-publico]] vino a evitar. Hay que preguntarlo. Ver
> [[huecos-abiertos]] pregunta 27.

## 2. Aguantar a más de 80 personas a la vez

> *«No debe de colgarse el sistema porque muchas personas lo estén usando (+80 personas).»*

El volumen en sí es pequeño: 80 personas por 2–4 marcaciones son unas **320 al día**. El riesgo
no es el caudal sino **la ráfaga al cambio de turno** y, sobre todo, **la memoria**: decodificar
ochenta imágenes a la vez es lo que tumba un proceso.

| Medida | Por qué |
|---|---|
| **Decodificación de imágenes con semáforo de 4** | Una ráfaga **hace cola**, no explota la memoria. Es la medida que de verdad importa |
| **La respuesta vuelve al guardar la fila** | La miniatura se genera después. La trabajadora no espera por algo que ella no ve |
| **Pool de conexiones de 10, hilos de Tomcat en 50** | Dimensionado, no por defecto |
| **`sp_registrar_marcacion` sigue siendo una sola llamada** | Insertar y recalcular en una transacción, sin ida y vuelta extra |

**La decisión 6 ayuda más que todo lo anterior:** Oracle Always Free da **2 OCPU y 12 GB** frente
al 1 vCPU y 1 GB de la B1s de Azure ([[adr-004-despliegue-azure]]). La ráfaga de ochenta deja de
ser un problema de diseño y pasa a ser holgura.

## 3. Fuera el cargo de la plantilla

> *«Quitar cargo de la plantilla.»*

La columna **CARGO desaparece** del cronograma, de la exportación y de las vistas del dashboard.
Deroga la parte de [[adr-006-dashboard-y-turnos-multiples]] que la había metido.

**El campo `cargo` se conserva en `trabajadora` pero pasa a admitir nulo**, no por indecisión:
la decisión 5 permite que alguien se registre escribiendo su nombre, y de esa persona **no se
conoce el cargo**. Un `NOT NULL` obligaría a inventárselo.

`jornada` (completa o media) **se mantiene**: el usuario la pidió en
[[adr-007-seguridad-endpoint-publico]] y no la retiró.

## 4. Exportación diaria, además de la semanal

> *«Exportación diaria en Excel también.»*

Deroga «solo semanal» de [[adr-008-alcance-interfaz-y-cola-sin-senal]]. No hace falta
procedimiento nuevo: la hoja diaria se arma con `sp_dia_sede`, que ya existe para la vista.

## 5. La advertencia de hora nunca se oculta

> *«Las advertencias siempre deben de estar en caso no coincidan las horas entre la del teléfono
> y la hora recibida.»*

Refuerza [[adr-008-alcance-interfaz-y-cola-sin-senal]] §3 y añade dos cosas:

- La advertencia **no se puede descartar**. No es un aviso que se cierra: es una propiedad
  permanente de esa marcación, y aparece cada vez que se la mira.
- **También viaja al Excel exportado.** Como el sistema no escribe palabras en las celdas
  ([[adr-003-login-y-cronograma-simple]]), va como **asterisco junto a la hora** más una leyenda
  al pie de la hoja: `10:02AM* - 8:46PM`.

## 6. Escribir el nombre si no está en la lista

> *«Está bien que se pueda seleccionar y escoger quien eres luego de tomarse su foto, pero que
> también tengan la opción de escribir su nombre, en caso las trabajadoras no estén
> registradas.»*

La rejilla de caras **sigue siendo el camino principal**. Debajo aparece **«No estoy en la
lista»**, que abre un campo de texto.

### Tensión con la lista cerrada

[[adr-002-arquitectura-produccion]] cerró la lista precisamente para evitar que `MARY P.`,
`mary` y `Mari` acabaran siendo tres trabajadoras distintas y el agrupado del cronograma se
rompiera en silencio. Ese motivo **sigue siendo válido**.

**Resolución:** el texto libre **no crea una trabajadora del padrón**. Crea una **trabajadora
provisional**, marcada como tal:

- Su marcación **se guarda igual** y con su foto — nada se pierde, que es lo que pide la
  decisión 1.
- Aparece en el dashboard en una bandeja de **pendientes de identificar**.
- La administradora la **confirma como alta nueva** o la **fusiona** con quien ya existe.
- Hasta entonces **no entra en el cronograma exportado**, porque no se sabe si es una persona
  nueva o una que ya está escrita de otra forma.

> Así conviven las dos cosas: nadie se queda sin marcar, y el padrón no se ensucia solo.

Esto **añade la novena función del dashboard** y un campo `provisional` en `trabajadora`.

## 7. La plataforma es Oracle Always Free

> *«Vamos a trabajar con Oracle, la versión gratuita.»*

Decisión firme. Deroga el plan de [[adr-005-hosting-largo-plazo]] («Azure para el curso, Oracle
para la empresa») y saca de la lista de descartes de
[[adr-009-estructura-y-dos-patrones-que-vuelven]] la migración a Oracle, que vuelve al alcance.

Consecuencias inmediatas:

- **Probar ARM deja de ser «temprano» y pasa a ser lo primero.** Si `aarch64` da una sorpresa,
  hay que saberlo en la semana 1. Sube a lo más alto de [[huecos-abiertos]].
- **El riesgo de capacidad es ahora el riesgo principal del proyecto**: provisionar una instancia
  ARM gratuita falla a menudo. Si no hay capacidad, la salida sigue siendo Hetzner.
- **El crédito de Azure deja de importar** para la entrega. Las preguntas 19 y 20 de
  [[huecos-abiertos]] pierden urgencia.
- **`AlmacenFotosOci` es la única implementación que hace falta** para entregar.

> **Y esto le quita una pata a Bridge.** [[adr-009-estructura-y-dos-patrones-que-vuelven]] lo
> justificó sobre «tres nubes». Quedan dos implementaciones reales —Oracle Object Storage y
> disco local para pruebas— más los dos tipos de foto del lado de la abstracción. **Sigue siendo
> defendible, pero más delgado**, y es justo el tipo de reclamo que prometimos revisar al
> escribir la clase.

## Consecuencias

- **[[modelo-datos]]:** `cargo` pasa a `NULL`, aparece `provisional` en `trabajadora`,
  `sp_validar_marcacion` deja de validar secuencia, y aparecen `sp_cronograma_diario` y
  `sp_provisionales`.
- **[[requisitos]]:** RF-02 se matiza por tercera vez, se deroga la validación de secuencia,
  y aparecen RF-23 (nombre libre), RF-24 (exportación diaria) y RNF-09 (concurrencia).
- **[[marcacion-multiple]]:** la regla de emparejado deja de suponer pares completos.
- **El dashboard llega a nueve funciones.**
- **Dos preguntas nuevas y una que sube a lo más urgente** en [[huecos-abiertos]].
- **No cambia ningún patrón**, pero **Bridge queda más flojo** y Command sigue igual.

## Enlaces

[[adr-002-arquitectura-produccion]] · [[adr-007-seguridad-endpoint-publico]] ·
[[adr-008-alcance-interfaz-y-cola-sin-senal]] · [[adr-009-estructura-y-dos-patrones-que-vuelven]] ·
[[modelo-datos]] · [[marcacion-multiple]] · [[huecos-abiertos]]
