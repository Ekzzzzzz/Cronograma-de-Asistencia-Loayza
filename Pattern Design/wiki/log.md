---
titulo: Bitácora
tipo: sintesis
estado: estable
actualizado: 2026-09-05
tags: [log]
---

# Bitácora

Registro **append-only** de todo lo que se hace en el wiki. Nunca se reescriben entradas
pasadas; los errores se corrigen con una entrada nueva que lo diga.

Formato obligatorio de cada entrada:

```
## [AAAA-MM-DD] <operacion> | <titulo>
```

Operaciones: `scaffold` · `ingesta` · `consulta` · `lint` · `decision`.

Últimas entradas: `grep "^## \[" wiki/log.md | tail -5`

---

## [2026-09-05] scaffold | Inicialización del wiki

Creado el esqueleto completo del wiki según el patrón LLM Wiki.

**Creado:**

- `CLAUDE.md` — esquema con las tres capas, convenciones de página, las operaciones
  (ingesta / consulta / lint), el estado del entorno y las reglas duras.
- `wiki/` con seis carpetas: `fuentes/`, `patrones/`, `dominio/`, `arquitectura/`,
  `decisiones/`, `sintesis/`.
- [[index]] — catálogo, incluyendo el inventario completo de las 42 fuentes crudas (41 del curso + 1 del cliente) con su
  estado de ingesta.
- [[log]] — esta bitácora.
- [[overview]] — el proyecto en una plana.
- [[requisitos]] — RF-01…RF-13 y RNF-01…RNF-04 extraídos del brief del usuario.
- [[sedes]] — las 7 sedes, con los Plus Codes tomados de `tools/pluscode.py`.
- [[formato-cronograma-excel]] — estructura del Excel objetivo.
- [[huecos-abiertos]] — bloqueos, preguntas y decisiones pendientes.

**Verificado en el entorno (no asumido):**

- `docs/Cronograma_Ejemplo.xlsx` descomprimido y decodificado a mano: es una tabla
  **semanal** (lunes 25 a domingo 31 de mayo) de la sede Los Olivos, con 13 podólogas y
  celdas de texto tipo `10:12AM - 8:46PM`, `DESCANSO`, `NO TURNO`, `INASISTENCIA`.
- No hay Python, Java, Maven ni Node instalados. Los tres scripts de `tools/` no se pueden
  ejecutar.
- No hay `poppler-utils`: los 8 PDF de `Archivos_de_clase/` son ilegibles por ahora.
- `unzip` sí está disponible, así que `.docx`, `.pptx` y `.xlsx` se pueden extraer sin
  dependencias. Comandos documentados en `CLAUDE.md` §7.
- Duplicados exactos confirmados por MD5: los tres `S10_s1s2 - PC-2-DPA*.pdf` son el mismo
  archivo, y `Diseño-Poster.pdf` es copia de `Cartel-Indicaciones.pdf`.

**No hecho:** ninguna fuente del curso fue ingerida. `wiki/fuentes/` y `wiki/patrones/`
están vacíos a propósito, esperando que el usuario dirija el orden de ingesta.

## [2026-09-05] decision | Se parte de cero pese a existir un wiki anterior

Al terminar el scaffold se detectó que el commit `6f3105c` ("Add Pattern Design wiki, assets
and tools") contenía una versión anterior y mucho más avanzada de este mismo wiki, borrada
del working tree antes de esta sesión: ~200 KB con 15 páginas de patrones, las fuentes
S06–S14 ya ingeridas, PC-2 y PC-3 leídas, 6 ADRs y un `log.md` de 21 KB con 12 entradas.

**Decisión del usuario:** quedarse con el scaffold nuevo y empezar de cero. El wiki anterior
no se restaura.

Sigue siendo recuperable en cualquier momento:

```bash
git show 6f3105c --stat              # ver qué contenía
git checkout 6f3105c -- "Pattern Design/wiki"   # restaurarlo si se cambia de opinión
```

Dos datos de ese wiki que conviene tener presentes, porque siguen siendo ciertos:

- Ya había llegado a conclusiones alineadas con el brief actual (enlace global sin sesión,
  jornadas múltiples por sede), así que si esas decisiones se vuelven a tomar aquí,
  probablemente coincidan.
- Su análisis del Excel se basaba en `docs/Cronograma_2026_Olivos.xlsx`, un archivo
  **distinto** (otro MD5) al `docs/Cronograma_Ejemplo.xlsx` actual. El análisis vigente es
  el de [[formato-cronograma-excel]], hecho sobre el archivo nuevo.

## [2026-09-05] scaffold | Toolchain instalado — se desbloquean los PDF y las sedes

El usuario instaló Python, Java y Node. Verificado por ejecución, no por suposición:
Python 3.14.7, Java/javac 26.0.2.1, Node 24.20.0 con npm 11.19.0. **Maven sigue faltando** y
`JAVA_HOME` no está definido.

**Desbloqueado:**

- Los tres scripts de `tools/` ya corren.
- `extraer_pdf.py` lee los PDF del curso: ~104 000 caracteres de PC-2 y ~107 000 de PC-3.
  Solo `CASO DE EJEMPLO 2.pdf` resiste (510 caracteres, PDF de imágenes, requeriría OCR).
- `pluscode.py` entregó las coordenadas de las 7 sedes con precisión de ~14 m.

**Hallazgos:**

- Las dos sedes más cercanas están a 3 333 m (San Borja–Miraflores), muy por encima de los
  ~14 m de precisión: identificar la sede por GPS sería inequívoco si alguna vez se decide
  hacerlo.
- `Poster-Indicaciones.pdf` no es una indicación del curso, sino un artículo académico sobre
  cómo elaborar un póster científico (Díaz V., Pediátrica de Panamá, 2016).
- `extraer_pdf.py` pierde tildes y ligaturas en algunos PDF. No citar literalmente sin
  corregir.
- El PATH del shell de esta sesión quedó desactualizado; hay que exportarlo a mano en cada
  comando (receta en `CLAUDE.md` §7).
- El PATH de usuario apunta a `C:\Program Files\Java\jdk-26\bin`, que no existe: el
  directorio real es `jdk-26.0.2.1`. Funciona igual gracias a `javapath`.

**Actualizado:** `CLAUDE.md` §7, [[huecos-abiertos]], [[sedes]] (coordenadas y separación
entre sedes) e [[index]] (los PDF pasan de `bloqueado` a `pendiente`).

## [2026-09-05] scaffold | Maven instalado y JAVA_HOME configurado — toolchain completo

A pedido del usuario se instaló lo que faltaba. El entorno ya no bloquea nada.

**Instalado:**

- **Maven 3.9.16**, a nivel de usuario en
  `%LOCALAPPDATA%\Programs\apache-maven-3.9.16`. No estaba en el índice de winget, así que
  se descargó el zip oficial de `dlcdn.apache.org` y se **verificó su SHA-512** contra el
  checksum publicado por Apache antes de extraerlo. Verificado después con `mvn -v`: usa
  Java 26.0.2.1 y encoding UTF-8.
- **`JAVA_HOME`** = `C:\Program Files\Java\jdk-26.0.2.1` (variable de usuario).
- Añadidos al PATH de usuario: el `bin` de Maven y el `bin` real del JDK.

**Descartado un riesgo:** se consultó `start.spring.io/metadata/client`. Spring Boot 4.1.1
(por defecto) ofrece Java **26**, 25, 21 y 17. El JDK 26 instalado está soportado, así que la
advertencia de la entrada anterior sobre necesitar un JDK 21 LTS **queda sin efecto**.

**Notas de la operación:**

- El PATH se modificó de forma **puramente aditiva**: no se quitó nada. Queda una entrada
  muerta previa, `C:\Program Files\Java\jdk-26\bin`, que apunta a un directorio inexistente;
  es inofensiva. Respaldo del PATH anterior en el scratchpad de la sesión.
- Los cambios de PATH y `JAVA_HOME` **no llegan a esta sesión**: su entorno se capturó al
  inicio. Hasta reiniciar la terminal hay que exportarlos a mano (receta en `CLAUDE.md` §7).

**Estado del entorno:** Python 3.14.7 · Java/javac 26.0.2.1 · Node 24.20.0 · npm 11.19.0 ·
Maven 3.9.16 · `unzip`. Nada pendiente de instalar.

## [2026-09-05] lint | Reverificación del entorno — Maven se había caído del PATH

El usuario pidió volver a comprobar Java y Maven. Bien pedido: **el entorno había cambiado**
desde la entrada anterior.

**Lo que se encontró:**

- **Java: correcto.** `java` y `javac` 26.0.2.1 resuelven por `javapath` (PATH de máquina) y
  además por `C:\Program Files\Java\jdk-26.0.2.1\bin` (PATH de usuario).
- **Maven: instalado pero roto.** Los binarios seguían intactos en
  `%LOCALAPPDATA%\Programs\apache-maven-3.9.16` y `mvn -v` funcionaba dándole la ruta, pero
  **su entrada había desaparecido del PATH de usuario**: en una terminal nueva, `mvn` no se
  habría encontrado.
- **Causa:** entremedio se instaló **Chocolatey**, y con él Python 3.14. Esa instalación
  reescribió el PATH de usuario. En el mismo cambio desapareció la entrada muerta
  `C:\Program Files\Java\jdk-26\bin` y `JAVA_HOME` se movió de ámbito usuario a ámbito
  máquina (con el valor correcto).
- No hay un segundo Maven: Chocolatey no instaló ninguno, el único es el de
  `%LOCALAPPDATA%`. `M2_HOME` y `MAVEN_HOME` no están definidos y no hacen falta.

**Corregido:** se reañadió el `bin` de Maven al PATH de usuario. Verificado después
simulando una terminal nueva (PATH leído solo del registro, sin añadidos manuales): `java`,
`javac`, `mvn`, `python`, `node` y `npm` resuelven los seis.

**Aprendizaje anotado en `CLAUDE.md` §7:** en esta máquina el PATH de usuario es frágil ante
instaladores. No basta con que algo esté instalado — hay que comprobar que **resuelva**.
Quedan documentados el comando de diagnóstico y el de reparación.

## [2026-09-05] ingesta | PC-3 — el entregable del curso

Primera fuente ingerida del wiki, y la más determinante hasta ahora: no enseña patrones,
**dice qué hay que entregar**. Extraída con `extraer_pdf.py`; el texto útil son las primeras
39 líneas del volcado, el resto es basura binaria de fuentes incrustadas. Acentos intactos.

**Creado:** [[pc3-entregable]], [[cuatro-capas]], [[mapa-patron-requisito]].

**Propagado a:** [[requisitos]] (nueva sección de requisitos del curso RC-01…RC-08 y una de
tensiones), [[huecos-abiertos]] (cinco preguntas nuevas, 7 a 11) e [[index]].

**Lo que la fuente cierra:**

- La arquitectura **no se elige**: son cuatro capas (Vista, Control, Modelo, BD) y el
  informe se evalúa sobre eso.
- **Diez patrones son obligatorios** y PC-3 los reparte por capa: Vista lleva Proxy, Bridge,
  Observer y Command; Control lleva Facade y Factory; Modelo lleva Singleton y Prototype. A
  eso se suman Decorator y Composite, exigidos por familia pero **sin capa asignada**.
- Hace falta **base de datos con scripts y stored procedures**: queda descartado un diseño
  solo-ORM.
- El repo va en **GitHub con carpetas `MVC` y `SQL`**.
- Hay que **evaluar antipatrones con evidencias** y documentar **principios ISP**.
- El proyecto debe declarar a qué **ODS** responde. El brief no lo mencionaba.

**Rúbrica (20 puntos):** informe APA 7 (2) · arquitectura de 4 capas (2) · **uso de patrones
(6)** · demo en video (5) · entrevista presencial (5). Dos lecturas para priorizar: los
patrones son el criterio más pesado, y **10 de 20 puntos son de exposición**, no de código —
el software tiene que ser demostrable con datos de prueba.

**Tensión registrada:** el usuario pidió Spring Boot y el curso pide `MVC` + `SQL` con
stored procedures. Compatibles, pero obligan a escribir SQL a mano.

**No hecho:** no se ingirió PC-2, que serviría para confirmar si PC-3 es la entrega
acumulativa final. Ninguna fuente de patrones (S06–S13) está ingerida, así que
[[mapa-patron-requisito]] es una propuesta inicial, no una decisión.

## [2026-09-05] ingesta | S06–S14 en lote — los quince patrones del curso

Ingesta en lote a pedido del usuario, que además fijó el criterio: **no hay que usar todos
los patrones, sino los que encajen con el proyecto**. Coincide con la regla 5 de
`CLAUDE.md`, así que se adopta sin fricción.

Extraídas ocho presentaciones (~105 000 caracteres) con `tools/extraer.py`.

**Creado:** [[s06-singleton-prototype]], [[s07-factory-abstractfactory-builder]],
[[s08-adapter-facade]], [[s09-decorator-composite]], [[s11-proxy-bridge]],
[[s12-state-observer]], [[s13-command-memento]], [[s14-antipatrones]].

**Reescrito:** [[mapa-patron-requisito]], que pasa de propuesta preliminar a evaluación de
encaje de los quince patrones, con veredicto por patrón.

**El hallazgo que reconcilia la tensión con PC-3:** [[s14-antipatrones]] nombra
**Overengineering** —"complejidades innecesarias que no se requieren para cumplir con los
requisitos actuales"— y **Golden Hammer** como antipatrones. Y [[pc3-entregable]] exige un
capítulo que los evalúe *con evidencias*. Por lo tanto **descartar un patrón que no encaja
no incumple el curso: es aplicar la unidad 4**, y cada descarte razonado es evidencia para
ese capítulo. El propio S07 refuerza el criterio al decir que se empieza por Factory y se
avanza "solo cuando se necesita más flexibilidad".

**Veredicto:** 7 de núcleo (Decorator, Composite, Factory, Facade, Proxy, Singleton, State)
· 5 de periferia (Observer, Bridge, Command, Adapter, Builder) · 3 descartes (Prototype,
Abstract Factory, Memento).

**Dos entradas por la puerta de atrás.** Ni **State** ni **Adapter** los exige PC-3, y los
dos encajan mejor que varios obligatorios: State modela los estados de la jornada y resuelve
el hueco de "entrada sin salida" más los `DESCANSO` / `NO TURNO` / `INASISTENCIA` del Excel;
Adapter importa los Excel viejos con sus formatos irregulares, que es el mismo problema del
`OldInventorySystem` del curso.

**Un conflicto abierto: Prototype.** PC-3 lo nombra explícitamente, pero el propio S06 lo
justifica solo "cuando la creación directa es costosa o compleja", y una marcación no lo es.
Se proponen tres salidas en [[mapa-patron-requisito]]; la recomendada es darle un uso
legítimo clonando plantillas de cronograma semanal.

**Otras tensiones registradas:** S06 enseña Singleton mientras S14 llama "Singletonitis" a
su abuso — material directo para el capítulo de antipatrones.

**Huecos detectados:** S11, S12 y S13 **no traen código Java**, a diferencia de S06–S09. El
código de Proxy, Bridge, State, Observer, Command y Memento habrá que sacarlo de las guías
de taller, aún sin ingerir. También se detectó un error en el material de origen: la
diapositiva 10 de S13, titulada "Patrón Command", describe en realidad el Memento.

**No hecho:** no se escribieron páginas individuales de patrón. Hacerlo para los quince
antes de que el usuario confirme el recorte sería el Overengineering que este mismo lote
identificó.

## [2026-09-06] decision | ADR-001 — lista corta confirmada y stack Spring Boot

El usuario confirmó la lista corta y, en el mismo tramo, fijó el stack: **Spring Boot**.
Ambas cosas quedan registradas en [[adr-001-patrones-seleccionados]].

**Creado:** las siete páginas del núcleo — [[patron-decorator]], [[patron-composite]],
[[patron-factory]], [[patron-facade]], [[patron-proxy]], [[patron-singleton]] y
[[patron-state]] — más [[adr-001-patrones-seleccionados]] y [[decisiones]] como índice de
ADRs.

Cada página de patrón lleva definición citada de su sesión, la justificación contra un
requisito concreto, un diseño en Java **adaptado al dominio** (no el ejemplo del curso
copiado) y una sección de cuidados.

**Lo que aportó confirmar Spring Boot.** Obligó a resolver una ambigüedad que no se había
visto: **Spring ya gestiona los beans como singletons**, así que escribir el Singleton
clásico encima de Spring para las mismas clases sería la Singletonitis que advierte
[[s14-antipatrones]]. La salida adoptada es `CatalogoSedes` con Singleton clásico —
autocontenido, sin inyección— y dejar la conexión a base de datos a Spring, documentando en
el informe que el framework aplica el mismo patrón. Lo mismo con Proxy: `@Transactional`,
`@Cacheable` y Spring Security son proxies dinámicos, pero se escriben a mano porque PC-3
pide capturas de código y una anotación no muestra el patrón. La tabla de correspondencias
está en el ADR.

**Decisiones de diseño que quedaron fijadas de paso:**

- El sello de fecha y hora se aplica **en el servidor**, con `LocalDateTime.now()` del
  backend, para que no se pueda falsificar desde el teléfono.
- `CatalogoSedes` usa el *holder idiom*, que resuelve la desventaja multihilo que reconoce
  [[s06-singleton-prototype]], y devuelve una lista inmutable.
- [[patron-state]] cierra el hueco que [[patron-composite]] dejaba abierto: de dónde sale la
  etiqueta de la celda cuando no hay marcaciones.

**Tensión detectada entre páginas nuevas:** la validación de entrada/salida aparece tanto en
`Marcacion.validar()` ([[patron-factory]]) como en los estados ([[patron-state]]).
Duplicarla sería *Shotgun Surgery*. Queda anotada en ambas páginas; hay que elegir un solo
sitio al implementar.

**Pendiente:** confirmar el rescate de Prototype vía plantillas de cronograma semanal antes
de escribir su página. La periferia (Observer, Bridge, Command, Adapter, Builder) sigue sin
páginas, a la espera de si se implementa.

## [2026-09-06] decision | Se reenfoca el proyecto: código limpio, no cuenta de patrones

El usuario corrigió el rumbo: **el objetivo es código limpio aplicando algunos patrones del
curso, los que encajen**. No maximizar la cantidad ni la nota de un criterio.

La corrección era necesaria. Las entradas anteriores de esta bitácora venían optimizando
para la rúbrica de [[pc3-entregable]] —el criterio de patrones vale 6 puntos— y eso llevó a
proponer el **rescate de Prototype**, inventándole un uso (clonar plantillas de cronograma)
para no perder puntos. Eso es Overengineering: exactamente lo que el curso enseña a evitar y
lo contrario del objetivo del proyecto.

**Cambios aplicados:**

- **Prototype: descartado, sin rescate.** La propuesta de rescate se elimina de
  [[mapa-patron-requisito]] y de [[adr-001-patrones-seleccionados]].
- **La periferia se filtra con una pregunta nueva:** ya no es "¿se puede justificar?", sino
  **"¿el código queda más limpio con el patrón que sin él?"**. Con ese filtro, de los cinco
  de periferia sobreviven dos:
  - **Observer** — sí, desacopla el registro del dashboard, y Spring lo da casi gratis.
  - **Adapter** — solo si hay que importar los Excel históricos. Hueco nuevo, pregunta 12.
  - **Bridge, Command y Builder** — descartados: con dos formatos de salida, sin requisito
    de *undo* y con `record` disponible, los tres añaden ceremonia sin ganancia.
- **Cuentas finales:** 7 se implementan · 2 dependen del alcance · **6 descartados**
  (Prototype, Abstract Factory, Memento, Bridge, Command, Builder).
- [[overview]] pasa a declarar el objetivo del proyecto en su propia sección, en vez de
  presentar el curso como la restricción principal.

**Riesgo asumido explícitamente:** el docente podría esperar los diez patrones que PC-3
nombra. Se mitiga documentando cada descarte con la cita del curso que lo respalda — S06
para Prototype, S07 para Builder, S14 para el criterio general — y llevando esos descartes al
capítulo de antipatrones, donde son contenido válido en vez de omisiones.

**Consecuencia para el trabajo que viene:** el foco pasa de "cuántos patrones" a **cómo queda
el código**. Nombres, responsabilidades únicas, separación de capas y ausencia de duplicación
pesan tanto como los patrones. Eso vuelve relevante el material de principios de diseño
(S01–S04 y el ISP que pide PC-3), aún sin ingerir.
