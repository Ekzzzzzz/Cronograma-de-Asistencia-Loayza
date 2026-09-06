---
titulo: Mapa patrón → requisito
tipo: sintesis
estado: borrador
fuentes:
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
  - Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx
  - Archivos_de_clase/S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx
  - Archivos_de_clase/S08_s1-Patrones-Estructurales-AF.pptx
  - Archivos_de_clase/S09_s1-Patrones-Estructurales-DC.pptx
  - Archivos_de_clase/S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
  - Archivos_de_clase/S13_s1 - Patrones comportamiento_Command_Memento.pptx
  - Archivos_de_clase/S14_s1 - Antipatrones Concepto, Propósito.pptx
  - brief del usuario (2026-09-05)
actualizado: 2026-09-05
tags: [patrones, requisitos, mapa, decision]
---

# Mapa patrón → requisito

Evaluación de **los quince patrones que enseña el curso** contra el problema real. Escrita
después de ingerir S06–S14; sustituye a la versión preliminar que solo tenía PC-3.

## El criterio

**El objetivo del proyecto es código limpio aplicando los patrones del curso que encajen**
(decisión del usuario, 2026-09-06). No es maximizar la cantidad de patrones.

Eso ordena la evaluación con una sola pregunta por patrón: **¿el código queda más limpio con
él que sin él?** Si la respuesta es no, el patrón sobra — por muy justificable que sea en
abstracto.

Dos apoyos para sostenerlo ante el curso:

- [[s14-antipatrones]] nombra **Overengineering** ("complejidades innecesarias que no se
  requieren para cumplir con los requisitos actuales") y **Golden Hammer**, y
  [[pc3-entregable]] exige un capítulo que los evalúe **con evidencias**. Cada descarte
  razonado *es* esa evidencia.
- [[s07-factory-abstractfactory-builder]] enseña la misma disciplina: se empieza por lo
  simple y se avanza "solo cuando se descubre que hace falta más flexibilidad".

Escala de encaje: **fuerte** (el problema existe y el patrón es su solución natural) ·
**medio** (defendible, pero hay que comprobar si limpia o ensucia) · **débil** (habría que
inventar el problema).

## Núcleo — encaje fuerte

Estos siete se sostienen solos. Son los que conviene desarrollar a fondo en el informe.

| Patrón | Uso en el proyecto | Requisito | Capa |
|---|---|---|---|
| **Decorator** | Sellar la foto: `FotoBase` → `SelloFechaHora` → `SelloSede`, encadenables | RF-06 | *sin asignar* |
| **Composite** | Cronograma: marcación (hoja) → jornada → día → semana de sede | RF-12 | *sin asignar* |
| **Factory** | Crear la marcación según sea entrada o salida | RF-04 | Control |
| **Facade** | `RegistrarMarcacion`: valida, sella la foto, persiste y guarda notas tras una sola llamada | RF-05…RF-08 | Control |
| **Proxy** | *Protection* para el dashboard de administradora; *Virtual* para cargar las fotos solo al abrirlas | RF-10, RF-11 | Vista |
| **Singleton** | Catálogo fijo de las 7 sedes y conexión a base de datos | RF-03, RC-03 | Modelo |
| **State** | Estados de la jornada: `SIN_INICIAR → ABIERTA → CERRADA`, más `INCOMPLETA`, `DESCANSO`, `NO TURNO`, `INASISTENCIA` | RF-09, RF-12 | Modelo |

Por qué estos:

- **Decorator es el mejor encaje del proyecto entero.** RF-06 pide que la fecha y la hora
  *salgan en la imagen*: es literalmente envolver un objeto añadiéndole responsabilidades sin
  cambiar su interfaz. La cadena `Transport → Insurance → Tracking` de
  [[s09-decorator-composite]] se traduce uno a uno.
- **Composite sale del formato existente.** [[formato-cronograma-excel]] demuestra que la
  celda de jornada es una agregación, no un dato. El ejemplo del curso es de pacientes y
  tratamientos: dominio clínico, como la podología.
- **Factory replica el ejemplo del curso.** `NotificacionFactory` (correo/SMS) de
  [[s07-factory-abstractfactory-builder]] es estructuralmente idéntico a entrada/salida.
- **Proxy justifica sus dos tipos** —protección y carga diferida— con dos necesidades reales
  distintas, no con una excusa estirada.
- **State no lo pide PC-3 y aun así entra**, porque resuelve un hueco abierto: qué hacer con
  una entrada sin salida, y cómo representar `DESCANSO` / `NO TURNO` / `INASISTENCIA`. La
  desventaja que advierte [[s12-state-observer]] —"excesivo si hay pocos estados"— no aplica
  con seis estados.

## Periferia — evaluada bajo el criterio de código limpio

Los cinco son defendibles en abstracto. Pero la pregunta correcta no es "¿se puede
justificar?", sino **"¿deja el código más limpio que sin él?"**. Con ese filtro, solo dos
sobreviven.

| Patrón | Uso posible | ¿Limpia o ensucia? | Veredicto |
|---|---|---|---|
| **Observer** | Refrescar dashboard y recalcular la fila del cronograma al entrar una marcación | **Limpia.** Evita que la fachada de registro conozca al dashboard. Y Spring lo da casi gratis con `ApplicationEventPublisher` | **sí, si hay tiempo** |
| **Adapter** | Importar los Excel viejos, con sus formatos irregulares, al modelo nuevo | **Limpia**, pero solo si importar el histórico está en el alcance. Aísla el parseo sucio en una clase | **depende del alcance** |
| **Bridge** | Separar el reporte de su formato de salida | **Ensucia.** Con dos formatos añade una jerarquía doble sin la explosión de clases que el patrón viene a evitar | no |
| **Command** | `MarcarEntrada` / `MarcarSalida` como comandos | **Ensucia.** Se solapa con Factory y Facade. Solo se gana el undo, que nadie pidió | no |
| **Builder** | Construir el reporte con sus opciones | **Ensucia.** El propio S07 avisa que "puede ser excesivo para objetos simples". Un `record` de parámetros es más limpio | no |

**Sobre Adapter.** No hay sistema legado de software, pero sí un **formato legado**: el Excel
llenado a mano, con `10.02AM`, `9:57AM - 8PM` y espacios sobrantes
([[formato-cronograma-excel]]). Si se decide importar el histórico, Adapter es la forma limpia
de que esa suciedad no se filtre al modelo. Si no se importa, no hay nada que adaptar.

> **Hueco:** ¿hay que importar los cronogramas viejos, o el sistema arranca en blanco? De eso
> depende Adapter. Anotado en [[huecos-abiertos]].

## Descartes razonados

| Patrón | Por qué no encaja | ¿Lo exige PC-3? |
|---|---|---|
| **Prototype** | El curso lo justifica "cuando la creación directa es costosa o compleja". Una marcación es un objeto barato y pequeño: clonarla sería inventar el problema | **Sí** ⚠️ |
| **Abstract Factory** | Requiere **familias** de productos relacionados que varíen juntas. Aquí no hay dos familias: solo hay marcaciones | No |
| **Memento** | Sus casos son editores, juegos y control de versiones. La marcación es un registro pequeño e inmutable, sin estado interno rico que restaurar. Si se quiere deshacer, Command basta | No |

**Prototype se descarta, y punto.** [[pc3-entregable]] lo nombra entre los creacionales del
informe, y durante un tiempo se evaluó "rescatarlo" inventándole un uso —clonar plantillas de
cronograma— solo para no perder puntos. Eso es forzar un patrón: precisamente el
Overengineering que [[s14-antipatrones]] describe.

El objetivo del proyecto es **código limpio con los patrones que encajen**
([[adr-001-patrones-seleccionados]]). Un patrón metido a la fuerza ensucia el código, que es
justo lo contrario. Se descarta y se documenta el porqué en el capítulo de antipatrones, con
la cita de S06 que lo respalda: Prototype es para cuando "la creación directa de un objeto es
costosa o compleja", y una marcación no lo es.

## Cuentas

- **Se implementan: 7 patrones** (el núcleo) · **candidatos según alcance: 2** (Observer y
  Adapter) · **descartados: 6** (Prototype, Abstract Factory, Memento, Bridge, Command,
  Builder).
- **State entra sin ser obligatorio** para PC-3, porque resuelve un problema real que ningún
  patrón obligatorio cubre.
- Siete patrones bien aplicados, de tres familias —creacionales, estructurales y de
  comportamiento—, cubren lo que el curso pide demostrar sin inflar el diseño.

## Pendiente

- Ninguna guía de taller (S06_s2, S08_s2, S12, S13) está ingerida: pueden traer código Java
  reutilizable, sobre todo para Proxy y Bridge, que en S11 vienen sin ejemplos.
- Falta ubicar Decorator y Composite en una capa; ver la inferencia en [[cuatro-capas]].
- Falta el principio **ISP**, que PC-3 exige documentar (unidad I) y aún no se ingiere.
