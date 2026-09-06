---
titulo: ADR-001 — Patrones seleccionados y stack
tipo: decision
estado: estable
fuentes:
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
  - Archivos_de_clase/S14_s1 - Antipatrones Concepto, Propósito.pptx
  - brief del usuario (2026-09-05)
  - confirmación del usuario (2026-09-06)
actualizado: 2026-09-06
tags: [adr, decision, patrones, spring]
---

# ADR-001 — Patrones seleccionados y stack

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

**El objetivo del proyecto, fijado por el usuario el 2026-09-06:** producir **código limpio
aplicando algunos de los patrones trabajados en clase, los que encajen con el proyecto**. No
es maximizar la cantidad de patrones ni la nota de un criterio.

Eso resuelve la tensión con [[pc3-entregable]], que exige diez patrones y da 6 puntos por su
uso. [[s14-antipatrones]] nombra **Overengineering** y **Golden Hammer** como antipatrones, y
PC-3 exige un capítulo que los evalúe *con evidencias*: un patrón forzado es evidencia de
antipatrón, y un descarte razonado también es evidencia.

El criterio de selección es una sola pregunta por patrón: **¿el código queda más limpio con
él que sin él?**

## Decisión

### 1. Stack: Spring Boot

Confirmado por el usuario el 2026-09-06, en línea con [[requisitos]] RNF-03. Se mantiene la
restricción RC-03 de [[cuatro-capas]]: base de datos con **scripts y stored procedures**, no
un diseño solo-ORM.

### 2. Núcleo de siete patrones

Se implementan a fondo y se documentan en el informe:

| Patrón | Capa | Requisito |
|---|---|---|
| [[patron-decorator]] | Vista *(propuesta)* | RF-06 |
| [[patron-composite]] | Modelo *(propuesta)* | RF-12 |
| [[patron-factory]] | Control | RF-04 |
| [[patron-facade]] | Control | RF-05…RF-08 |
| [[patron-proxy]] | Vista | RF-10, RF-11 |
| [[patron-singleton]] | Modelo | RF-03, RC-03 |
| [[patron-state]] | Modelo | RF-09, RF-12 |

**State no es obligatorio para PC-3** y entra igual: resuelve la entrada sin salida y los
estados `DESCANSO` / `NO TURNO` / `INASISTENCIA` del Excel, que ningún patrón obligatorio
cubre.

### 3. Dos candidatos que dependen del alcance

- **Observer** — desacopla el registro de marcación del refresco del dashboard, y Spring lo
  da casi gratis con `ApplicationEventPublisher`. Entra si el tiempo alcanza.
- **Adapter** — solo tiene sentido **si hay que importar los cronogramas viejos**. Si el
  sistema arranca en blanco, no hay nada que adaptar.

### 4. Descartes

Seis patrones quedan fuera. Los tres primeros no tienen problema que resolver; los tres
últimos lo tienen, pero **el patrón ensucia más de lo que limpia**:

| Patrón | Motivo |
|---|---|
| **Prototype** | Una marcación es un objeto barato. S06 lo justifica solo si la creación es "costosa o compleja" |
| **Abstract Factory** | No hay familias de productos que varíen juntas: solo hay marcaciones |
| **Memento** | La marcación es un registro pequeño e inmutable, sin estado rico que restaurar |
| **Bridge** | Con dos formatos de salida añade una jerarquía doble sin la explosión de clases que viene a evitar |
| **Command** | Se solapa con Factory y Facade. Solo aportaría el *undo*, que nadie pidió |
| **Builder** | El propio S07 avisa que es excesivo para objetos simples. Un `record` de parámetros es más limpio |

Los seis se documentan en el capítulo de antipatrones como Overengineering evitado.

## Sobre Prototype, que fue el caso difícil

PC-3 lo nombra explícitamente entre los creacionales del informe. Durante un tiempo se evaluó
"rescatarlo" inventándole un uso —clonar plantillas de cronograma semanal— para no arriesgar
puntos del criterio que vale 6.

**Se descartó esa salida.** Inventarle un problema a un patrón para justificar su presencia es
exactamente el Overengineering que el curso enseña a evitar, y contradice el objetivo de este
proyecto. Prototype queda fuera, y su ausencia se explica en el informe con la cita de S06 que
la respalda.

## Cómo se apoya cada patrón en Spring Boot

Importante para el informe: **Spring aplica varios de estos patrones por debajo**. Escribir
la versión manual y comparar es material directo para la entrevista de PC-3.

| Patrón | Qué hace Spring | Qué escribimos a mano y por qué |
|---|---|---|
| Singleton | Los beans son singleton por defecto | `CatalogoSedes` clásico, autocontenido. Duplicarlo en todos lados sería *Singletonitis* |
| Proxy | `@Transactional`, `@Cacheable` y Spring Security crean proxies dinámicos | Proxies explícitos: el informe pide capturas de código, y una anotación no muestra el patrón |
| Observer | `ApplicationEventPublisher` y `@EventListener` | A decidir si entra la periferia |
| Facade | Corresponde a un `@Service` | La fachada explícita, que coordina sin implementar |
| Factory, Decorator, Composite, State | Sin equivalente en el framework | Java puro |

## Consecuencias

- **Siete patrones** de las tres familias —creacional, estructural y de comportamiento—
  cubren lo que el curso pide demostrar, sin inflar el diseño.
- Cada descarte alimenta el capítulo de antipatrones, así que **no se pierde contenido**: se
  convierte en material de otro criterio del informe.
- **Se asume conscientemente el riesgo** de que el docente espere ver los diez patrones que
  PC-3 nombra. Se mitiga documentando cada descarte con la cita del curso que lo respalda —
  S06 para Prototype, S07 para Builder, S14 para el criterio general.
- **Decorator y Composite siguen sin capa asignada** por PC-3; las propuestas de la tabla hay
  que sostenerlas en el informe.
- El foco pasa de "cuántos patrones" a **cómo queda el código**: nombres, responsabilidades
  únicas, capas separadas y ausencia de duplicación pesan tanto como los patrones.

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[pc3-entregable]] · [[huecos-abiertos]]
