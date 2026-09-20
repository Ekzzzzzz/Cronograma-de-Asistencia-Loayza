---
titulo: ADR-003 — Login de administrador y cronograma sin estados
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-06)
  - docs/Cronograma_Ejemplo.xlsx
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
actualizado: 2026-09-06
tags: [adr, decision, autenticacion, cronograma, patrones]
---

# ADR-003 — Login de administrador y cronograma sin estados

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

El usuario fijó dos cosas el 2026-09-06, después de [[adr-002-arquitectura-produccion]]:

1. **El cronograma solo escribe horas.** Todo lo demás que aparece en
   `docs/Cronograma_Ejemplo.xlsx` —`DESCANSO`, `NO TURNO`, `INASISTENCIA`, tardanzas— **se
   maneja de forma manual**, fuera del sistema. La celda es, textualmente, *"la hora de
   ingreso + ' - ' + la hora de salida"*.
2. **Hay login de administrador, sí o sí.**

La primera es una simplificación grande del alcance. La segunda cierra un hueco que llevaba
abierto desde el principio.

## Decisiones

### 1. La celda del cronograma es solo `entrada - salida`

El sistema genera únicamente celdas con horas. Si no hay marcaciones, la celda **queda
vacía** y la administradora escribe a mano lo que corresponda sobre el archivo exportado.

Consecuencias directas:

- **Se elimina la tabla `turno_planificado`** de [[modelo-datos]]. Existía solo para
  distinguir los tres estados de ausencia. Sin ese requisito, no tiene razón de ser — y con
  ella desaparece la pantalla de planificación semanal que habría que haber construido.
- **Se cierra la pregunta 13** de [[huecos-abiertos]] por eliminación del problema.
- El sistema deja de intentar interpretar por qué alguien no marcó. Solo registra lo que
  ocurrió.

**Turnos partidos: se colapsan.** Siguiendo la indicación literal, la celda es *primera
entrada – última salida*. Si alguien trabaja 9–1 y 3–8, la celda dice `9:00AM - 8:00PM`.
Eso declara 11 horas donde se trabajaron 9, así que **`jornada_diaria` guarda aparte los
minutos realmente trabajados y el número de turnos**: la celda simplifica, pero el dato
almacenado no miente y cualquier informe posterior puede usar la cifra correcta. Cierra la
pregunta 14 de [[huecos-abiertos]].

### 2. Autenticación real para la administradora

Cierra la pregunta 2 de [[huecos-abiertos]]. Spring Security con contraseñas en **BCrypt** y
una tabla `administrador` ([[modelo-datos]]).

El login protege el dashboard (RF-11), la exportación (RF-13) y el registro de trabajadoras
que introdujo [[adr-002-arquitectura-produccion]]. **La cara trabajadora sigue sin login**,
como manda [[requisitos]] RF-01: enlace público, cinco pasos, sin credenciales.

Esto **fortalece [[patron-proxy]]**: el *Protection Proxy* del dashboard dejaba de ser
convincente cuando no había nada que verificar. Ahora hay una sesión real que consultar, y el
patrón protege un recurso que de verdad lo necesita.

### 3. Se descarta el patrón State

**Este ADR revisa una decisión de [[adr-001-patrones-seleccionados]].**

State entró al núcleo con una justificación explícita: resolvía la entrada sin salida **y**
los tres marcadores del Excel, y [[patron-state]] argumentaba que *"contando todo son siete
estados, así que la advertencia de S12 sobre pocos estados no aplica"*.

La decisión 1 borra esa base. Quedan **dos situaciones**: turno abierto y turno cerrado. Y
[[s12-state-observer]] advierte que State *"puede ser excesivo si solo hay pocos estados"*.

Aplicando el criterio del proyecto —**¿el código queda más limpio con el patrón que sin
él?**— la respuesta ahora es no. Un `salida == null` dentro de `Turno` es más claro que dos
clases de estado con transiciones. Mantenerlo sería **Golden Hammer** ([[s14-antipatrones]]):
conservar un patrón por inercia cuando el problema que lo justificaba desapareció.

> **Este descarte es el mejor material del proyecto para el capítulo de antipatrones.** Los
> otros seis se descartaron en el análisis inicial; este se descarta **después de haberlo
> aceptado**, porque un cambio de requisitos le quitó el sustento. Muestra el criterio
> aplicándose en el tiempo, que es exactamente lo que pide la evaluación de antipatrones con
> evidencias de [[pc3-entregable]] (RC-06), y es una buena respuesta para la entrevista.

### 4. Observer sube de la periferia al núcleo

Descartar State deja al proyecto **sin ningún patrón de comportamiento**, y
[[pc3-entregable]] exige desarrollar las tres familias: creacionales, estructurales y de
interacción y responsabilidades. Es un hueco real frente a la rúbrica.

Observer lo cubre, y con mejor justificación de la que le quedaba a State:

- Al registrarse una marcación hay que **recalcular `jornada_diaria`** y **refrescar el
  dashboard**. Sin Observer, [[patron-facade]] tendría que conocer a ambos y la fachada
  empezaría a acumular responsabilidades — el *Big Ball of Mud* contra el que su propia
  página advierte.
- Con login y un dashboard real (decisión 2), el refresco en vivo es una funcionalidad
  concreta, no una excusa.
- Spring lo provee con `ApplicationEventPublisher` y `@EventListener`, así que el costo de
  añadirlo es bajo y el informe puede comparar la versión manual con la del framework, igual
  que se hace con Singleton y Proxy en [[adr-001-patrones-seleccionados]].

Queda pendiente su página: [[patron-observer]].

## Cuentas actualizadas

> **Corrección aritmética (2026-09-06).** Esta sección decía "seis patrones de núcleo"
> mientras la tabla de abajo listaba siete. **Son siete.** El núcleo eran 7 en
> [[adr-001-patrones-seleccionados]]; sale State y entra Observer, así que sigue en 7.

**Siete patrones de núcleo**, cubriendo las tres familias:

| Familia | Patrones |
|---|---|
| Creacional | [[patron-singleton]], [[patron-factory]] |
| Estructural | [[patron-decorator]], [[patron-composite]], [[patron-facade]], [[patron-proxy]] |
| Comportamiento | [[patron-observer]] |

**Descartes: siete** — Prototype, Abstract Factory, Memento, Bridge, Command, Builder y ahora
**State**. Adapter queda aparte, dependiendo del alcance. Las cuentas cierran: 7 + 7 + 1 = 15.

**De los diez patrones que nombra [[pc3-entregable]] implementamos siete**: Singleton, Factory,
Facade, Decorator, Composite, Proxy y Observer. Faltan tres —Prototype, Bridge y Command— y
cada uno tiene su descarte documentado con la cita del curso que lo respalda.

## Consecuencias

- Desaparece una tabla, una pantalla y un patrón. El alcance se reduce de forma medible.
- [[patron-state]] pasa a `obsoleto`. No se borra: su contenido es la evidencia del descarte.
- [[marcacion-multiple]] pierde la sección de los tres estados de ausencia y conserva la
  corrección del nivel `Turno`, que sigue siendo necesaria por los turnos múltiples.
- Aparece la tabla `administrador` y una dependencia nueva: Spring Security.
- **La lógica de emparejar entrada con salida queda en un solo sitio**: el modelo Java
  (`Jornada` → `Turno`). La base de datos guarda el resultado, no lo recalcula.

## Enlaces

[[adr-001-patrones-seleccionados]] · [[adr-002-arquitectura-produccion]] · [[modelo-datos]] ·
[[marcacion-multiple]] · [[patron-state]] · [[patron-observer]] · [[mapa-patron-requisito]]
