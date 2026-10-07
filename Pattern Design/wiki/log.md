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

## [2026-09-06] decision | ADR-002 — el proyecto pasa a ser producción real

El usuario fijó una restricción que cambia el encuadre: **el sistema no es solo un entregable
de curso, tiene que quedar listo para producción** en una empresa real con 7 sedes y personal
numeroso, y el requisito explícito es que **sea rápido con los datos**.

**Creado:** [[adr-002-arquitectura-produccion]], [[modelo-datos]] y [[marcacion-multiple]]
— esta última llevaba tres páginas enlazándola sin existir.

**Lo primero fue dimensionar, y el resultado reordena las prioridades.** Extrapolando de las
13 podólogas que lista `docs/Cronograma_Ejemplo.xlsx`: ~140 trabajadoras, ~400 marcaciones
diarias, **~150 000 filas al año**. Eso es poco: cualquier consulta sobre esa tabla se
resuelve en milisegundos con el índice correcto. **El cuello de botella son las imágenes**
—~600 GB/año sin comprimir, ~30 GB comprimidas— y sobre todo el tiempo de subida en red
móvil. Optimizar la tabla de marcaciones habría sido el Overengineering de
[[s14-antipatrones]]; el trabajo real estaba en otro lado.

**Decisiones del usuario (cinco):** MySQL 8 como motor · fotos en disco con la ruta en base,
nunca como BLOB · despliegue en VPS en la nube · identidad por **lista desplegable cerrada** ·
retención de 3 meses en tamaño completo con miniatura permanente.

**Hallazgos que no estaban en el wiki:**

- **HTTPS es un requisito funcional encubierto**, no de infraestructura: sin certificado el
  navegador móvil no concede acceso a la cámara y RF-05 deja de funcionar.
- **Comprimir la foto en el navegador antes de subirla** es la medida de mayor impacto de
  todas: 15–30 segundos de espera en 4G bajan a 1–2. Y no rompe la garantía anti-fraude,
  porque el sello de hora lo sigue poniendo el servidor.
- **La tabla `jornada_diaria` entra por corrección, no por velocidad.** `DESCANSO`,
  `NO TURNO` e `INASISTENCIA` no se derivan de las marcaciones; esa tabla es donde persiste
  el resultado de [[patron-state]]. Que además vuelva el cronograma un SELECT de ~91 filas es
  beneficio gratis.
- **Aparece `turno_planificado`**, la entidad que faltaba para distinguir esos tres estados.
  La pregunta 3 de [[huecos-abiertos]] baja de "no sabemos cómo se modela" a "falta la
  pantalla que lo captura".
- **La columna `fecha` generada e indexada** decide si la exportación tarda 3 ms o 300 ms con
  tres años de historia.

**Corrección a dos páginas de patrón.** [[patron-composite]] y [[patron-state]] usaban
`Jornada` para dos cosas incompatibles, y con turnos partidos el diseño se rompe: al cerrar el
primer turno el estado pasa a `CERRADA`, y la segunda entrada lo reabre **sobre el mismo
objeto**, perdiendo el primer par. Se introduce el nivel **`Turno`** entre `Jornada` y
`Marcacion`. Efectos: el contexto de State pasa a ser `Turno`, la validación queda en **un
solo sitio** —cerrando la tensión de *Shotgun Surgery* anotada entre [[patron-factory]] y
[[patron-state]]— y `Marcacion` vuelve a ser un hecho inmutable.

**Un hallazgo aprovechable para el informe:** el `Duration.ZERO` que `Marcacion` devolvía en
[[patron-composite]] era una violación de **ISP** — la interfaz obligaba a la hoja a
implementar lo que no le corresponde. Con el nivel `Turno` se corrige, y el antes/después es
evidencia directa para el capítulo de principios ISP que exige [[pc3-entregable]] (RC-05),
que hasta ahora no tenía material propio.

**Cerrado en [[huecos-abiertos]]:** preguntas 1 (identidad), 9 (motor de base de datos) y
parcialmente 2 (acceso de la administradora) y 3 (los tres estados de ausencia).

**No hecho:** no se escribió código. El proyecto Maven, el esquema SQL y los stored procedures
siguen sin existir. Tampoco se ingirieron S01–S04 ni las guías de taller, que son el material
de principios de diseño e ISP.

## [2026-09-06] decision | ADR-003 — login de administrador y un patrón que se cae

El usuario fijó dos cosas: **el cronograma solo escribe horas** —`DESCANSO`, `NO TURNO`,
`INASISTENCIA` y las tardanzas se manejan a mano fuera del sistema— y **hay login de
administrador sí o sí**.

**Creado:** [[adr-003-login-y-cronograma-simple]] y [[patron-observer]].

**Lo que la primera decisión elimina.** Desaparecen la tabla `turno_planificado`, la pantalla
de planificación semanal que habría que construir, y la columna `estado` de `jornada_diaria`.
El hueco de los tres estados de ausencia, abierto desde la primera lectura del Excel, **no se
cerró con más diseño sino con menos alcance**.

**Y arrastra un patrón.** [[patron-state]] entró al núcleo argumentando que "contando todo son
siete estados, así que la advertencia de S12 sobre pocos estados no aplica". Al irse cinco de
esos siete quedan dos —turno abierto y cerrado— y [[s12-state-observer]] dice literalmente
que State "puede ser excesivo si solo hay pocos estados". Por el criterio del proyecto
—¿el código queda más limpio con él?— la respuesta pasó a ser no: un `salida == null` gana a
dos clases de estado. **State se descarta** y su página pasa a `obsoleto`, conservada como
evidencia.

**Observer sube de la periferia al núcleo** para tapar el hueco que eso abría: sin State el
proyecto se quedaba **sin ningún patrón de comportamiento**, y [[pc3-entregable]] exige
desarrollar las tres familias. Su justificación es además más sólida que la que le quedaba a
State: sin él, [[patron-facade]] tendría que conocer al recalculador de jornadas y al
dashboard, y crecería con cada efecto nuevo.

**Cuentas nuevas:** 6 patrones de núcleo —Singleton y Factory (creacionales), Decorator,
Composite, Facade y Proxy (estructurales), Observer (comportamiento)—, **7 descartados** y
Adapter según alcance.

**El login refuerza [[patron-proxy]].** Mientras no había autenticación, `SesionAdministradora`
era un objeto ficticio y el *Protection Proxy* protegía un recurso que en realidad estaba
abierto. Con Spring Security y BCrypt sobre la tabla `administrador` nueva, el proxy consulta
una sesión real.

**Sobre los turnos partidos:** se colapsan, siguiendo la indicación literal del usuario —hora
de ingreso, guión, hora de salida—. Como un día de 9–1 y 3–8 declararía 11 horas donde se
trabajaron 9, `jornada_diaria` conserva aparte `minutos` y `turnos` reales: **la celda
simplifica, el dato no miente.**

**Requisitos nuevos:** RF-14 (login), RF-15 (la celda solo lleva horas), RNF-05 (HTTPS) y
RNF-06 (compresión en el navegador). RF-02 y RF-10 quedan matizados.

**Cerrado en [[huecos-abiertos]]:** preguntas 2, 3, 13 y 14. La 5 baja a parcial.

**Sigue pendiente y ahora es lo más urgente:** el **ODS** (RC-07), único hueco que bloquea el
informe. Y **el ISP no tiene página** pese a ser criterio de la rúbrica, aunque el caso ya
está identificado —el `Duration.ZERO` de `Marcacion`— y S01–S04 siguen sin ingerir.

**No hecho:** cero código. No existe el proyecto Maven, ni el esquema SQL, ni los stored
procedures que [[modelo-datos]] define.

## [2026-09-06] decision | ADR-004 — Azure con 100 USD de crédito de estudiante

El usuario dispone de **100 USD en Azure** por su cuenta de estudiante.
[[adr-002-arquitectura-produccion]] había dicho "VPS en la nube" en abstracto; con proveedor y
presupuesto concretos, dos decisiones se afinan y una se revierte.

**Creado:** [[adr-004-despliegue-azure]].

**Lo primero fue hacer la cuenta, y sale corta.** Con precios de lista, una VM B1s más disco,
IP pública estática y Blob suman **~13–14 USD al mes**: el crédito rinde unos **7 meses**, no
doce. Con la VM reservada a un año baja a ~10 USD y llega a diez meses. Solo supera el año si
el nivel gratuito cubre las 750 horas de B1s, **y eso no está verificado** — pregunta 19 de
[[huecos-abiertos]].

**Se descarta separar la base de datos.** Azure Database for MySQL, en su SKU más barato,
cuesta 12–14 USD al mes él solo: se comería el crédito entero sin haber pagado todavía el
servidor de aplicación. A 400 marcaciones diarias no hay razón técnica para separarlas. Todo
va en una VM B1s: Nginx, Spring Boot y MySQL.

**Se revierte la decisión 2 de ADR-002: las fotos van a Blob Storage, no al disco.** En Azure
el almacenamiento de objetos es **más barato que el disco** —~0,22 USD al mes contra ~4,80 de
un disco de 64 GB— y además sobrevive a la VM y elimina el fallo por disco lleno. Lo esencial
de ADR-002 se mantiene: **nunca `BLOB` dentro de MySQL**. Y el cambio **no toca ningún
llamador**, porque ADR-002 ya había impuesto la interfaz `AlmacenFotos` justo para esto.

**La retención de 3 meses resultó ser lo que vuelve viable el presupuesto**: en régimen
estable son ~12 GB, no los ~30 GB anuales que crecerían sin purga.

**HTTPS sale gratis.** RNF-05 se resuelve con la etiqueta DNS de Azure más Let's Encrypt, sin
comprar dominio. El respaldo también: `mysqldump` nocturno a Blob en nivel frío, porque Azure
Backup cuesta ~5 USD al mes, la mitad del presupuesto mensual.

**Y aparece un uso de [[patron-adapter]] que no depende del alcance.** Adapter llevaba desde
[[adr-001-patrones-seleccionados]] en suspenso, condicionado a importar los Excel históricos.
El SDK de Azure Blob expone una interfaz ajena que hay que traducir a la del dominio, que es
lo que [[s08-adapter-facade]] define. **Queda anotado con su reserva:** si al escribir la clase
resulta ser una delegación de tres líneas sin traducción real, no es Adapter y no debe
llamarse así — sería Golden Hammer. Se decide escribiéndolo. De eso depende que el proyecto
tenga 6 o 7 patrones.

**Dos consecuencias que no son técnicas:** hay que poner **alertas de presupuesto** al 25, 50 y
75 % del crédito, y hay que **planificar el traspaso de facturación a la empresa** antes de que
venza. La cuenta de estudiante no lleva tarjeta: al agotarse el crédito los recursos se
detienen. Un control de asistencia que se apaga sin aviso es peor que no tenerlo.

**Riesgo asumido:** 1 GB de RAM para Ubuntu, MySQL, la JVM y Nginx es ajustado. Queda
documentado el afínado (`-Xmx384m`, buffer pool de 192 MB, 2 GB de swap) y la salida si falla:
B1ms, que duplica el costo de la VM.

**No hecho:** sigue sin escribirse código. Tampoco se ha creado nada en Azure todavía.

## [2026-09-06] lint | Corrección de cuentas — el núcleo son siete patrones, no seis

Al preparar la presentación de avance salió un error propio: [[adr-003-login-y-cronograma-simple]]
escribía "seis patrones de núcleo" mientras **su propia tabla listaba siete**, y la cifra
equivocada se había propagado a [[index]], [[overview]], [[mapa-patron-requisito]] y
[[adr-004-despliegue-azure]].

**Son siete.** El núcleo eran 7 en [[adr-001-patrones-seleccionados]]; sale State y entra
Observer, así que sigue en 7: Singleton, Factory, Decorator, Composite, Facade, Proxy y
Observer. Las cuentas cierran **7 implementados + 7 descartados + 1 según alcance = 15**.

Importa porque es la cifra que va al informe: **de los diez patrones que nombra
[[pc3-entregable]] implementamos siete** —faltan Prototype, Bridge y Command, los tres con su
descarte documentado— y con Adapter serían ocho.

**Corregido en:** ADR-003 (con nota de la corrección), ADR-004, [[mapa-patron-requisito]],
[[index]] y [[overview]].

## [2026-09-06] decision | ADR-005 — hosting a largo plazo, ODS 8 y equipo de dos

El usuario pidió investigar si los 100 USD de Azure se renuevan con el correo de la UTP y si
hay alternativas más baratas. Además confirmó ODS, equipo y forma de trabajo.

**Creado:** [[adr-005-hosting-largo-plazo]].

**El crédito sí se renueva, y aun así no sirve.** Azure for Students se renueva cada 12 meses
sin límite de veces mientras se siga siendo estudiante verificado, con 100 USD nuevos cada vez
(no es automático: avisan ~30 días antes y hay que reverificar). **Pero el crédito se agota por
lo que ocurra primero**: a ~13 USD al mes los 100 dólares se acaban en el mes 7,7 y la
renovación no llega hasta el 12. **Son más de cuatro meses de sistema apagado.** Para que
coincidan, el gasto tendría que bajar de 8,33 USD/mes, y en Azure no se llega sin recortar algo
necesario.

**La alternativa que resuelve el problema de raíz: Oracle Cloud Always Free** — 2 OCPU ARM y
**12 GB de RAM**, 200 GB de bloque, 20 GB de objetos y 10 TB de salida mensual, **gratis y sin
vencimiento**. Doce veces la memoria de la B1s de Azure, y sus 20 GB de objetos cubren justo
los ~12 GB de fotos en régimen estable. Con 12 GB de RAM desaparece además todo el afinado
incómodo que documentaba ADR-004.

**Riesgos anotados, no ocultados:** Oracle **redujo el nivel gratuito a la mitad el 15 de junio
de 2026** —de 4 OCPU y 24 GB a 2 y 12— **sin anuncio público**, solo actualizando la
documentación; provisionar ARM falla a menudo por *out of capacity*; y es aarch64. Si no da
capacidad, la salida es **Hetzner** a ~5,50 USD/mes.

**Decisión: Azure para el curso, Oracle para producción.** Cambiar de proveedor a mitad del
trabajo académico no compra nada, y 100 USD cubren de sobra hasta la entrega.

**Las dos cuentas de estudiante no se encadenan.** La compañera también tiene crédito, pero
depender de una suscripción que controla otra persona para las marcaciones reales de una
empresa es frágil. Se destina a un **entorno de pruebas separado**, que además produce los
datos de prueba que exige RC-08 para el video.

**ODS 8 confirmado** — Trabajo decente y crecimiento económico, meta 8.8. Cierra RC-07 y la
pregunta 7 de [[huecos-abiertos]]: **era el último hueco que bloqueaba el informe.**

**Equipo de dos, programando a la par.** Más lento que repartirse capas, pero es lo que premia
la entrevista individual de PC-3, donde cada estudiante responde por todo el sistema. **Queda
un riesgo abierto:** PC-3 nombra los archivos `Apellidos1-2-3` y pide lecciones "por cada
estudiante" — hay que **confirmar con el docente** que un grupo de dos está autorizado.

**También:** el [GitHub Student Developer Pack](https://education.github.com/pack) con el
correo UTP incluye 200 USD de DigitalOcean por un año y un **dominio gratis**, que resuelve sin
costo lo único que ADR-004 dejaba como mejora opcional.

**No hecho:** sigue sin escribirse código, y no se ha creado nada en Azure ni en Oracle.

## [2026-09-06] decision | ADR-006 — el dashboard, y una decisión mía que estaba mal

El usuario revisó la presentación de avance y detectó tres huecos. Dos son omisiones; uno es un
error de interpretación mío.

**Creado:** [[adr-006-dashboard-y-turnos-multiples]].

**El error: la celda del cronograma no podía colapsar los turnos.**
[[adr-003-login-y-cronograma-simple]] fijó que la celda fuera *primera entrada – última
salida*, porque leí "netamente la hora de ingreso más la hora de salida" como un solo rango.
**Era una mala lectura**, y estaba advertida por escrito: el propio ADR-003 anotaba que eso
"declara 11 horas donde se trabajaron 9", y [[marcacion-multiple]] lo había señalado antes como
el riesgo del colapso. Ahora la celda lleva **un par por línea, hasta seis** —hasta 12
marcaciones al día— que es el caso extremo de rotación entre sedes.

Un séptimo par **no se rechaza**: se registra y marca la jornada para revisión. Perder una
marcación real es peor que romper el formato.

**Lo que sí estaba bien:** el modelo de dominio nunca colapsó nada. `Jornada → Turno →
Marcacion` de [[marcacion-multiple]] ya soportaba seis pares, y `minutos` siempre sumó turno a
turno. El fallo estaba solo en cómo se renderizaba la celda.

**Primera omisión: el dashboard nunca se especificó.** Todo el wiki decía "una pestaña por
sede" y nada más, siendo la mitad del sistema. Queda con **seis funciones**: vista diaria
(una sede y un día, cada trabajadora con todos sus turnos y horas sumadas), vista semanal
(la tabla del cronograma), exportar a Excel, **corregir una marcación olvidada**, **historial
por trabajadora** cruzando las 7 sedes, y **totales de horas**.

La vista diaria es **una sede a la vez**, no las siete en paralelo: con siete columnas la
pantalla queda ilegible, y la unidad natural del negocio es (sede, día), la misma clave de
`jornada_diaria`. El historial es **la única vista que cruza sedes**, porque un reclamo por
horas es por persona, no por local.

**Segunda omisión: existen cajeras.** Ninguna fuente previa lo mencionaba — todo el wiki
hablaba solo de podólogas, y el Excel de ejemplo tiene el encabezado `PODOLOGA`. Aparece
`trabajadora.cargo` con valores `PODOLOGA` y `CAJERA`, **todas en la misma tabla del
cronograma** con el cargo como columna. Se modela como `ENUM` y no como tabla de catálogo:
con dos valores, una tabla aparte sería la ceremonia que este proyecto evita.

**Las correcciones son inserciones, no ediciones.** Cierra la pregunta 15 de
[[huecos-abiertos]]. Cuando alguien olvida marcar la salida, la administradora **inserta una
marcación nueva** con `origen = ADMINISTRADORA`, sin foto y con motivo obligatorio. Así la
regla de [[modelo-datos]] —las marcaciones son hechos inmutables— se conserva, y la
trazabilidad se **gana** en vez de perderse: consta quién la puso y por qué. `foto_ruta` pasa
a admitir nulo solo para este caso.

**Cambios en [[modelo-datos]]:** `cargo` en `trabajadora`; `origen`, `corregida_por`, `motivo`
y `foto_ruta` nulable en `marcacion`; `etiqueta` de `VARCHAR(40)` a `VARCHAR(160)` —seis pares
no caben en cuarenta caracteres—. Y tres procedimientos nuevos: `sp_dia_sede`,
`sp_historial_trabajadora` y `sp_totales_horas`.

**Requisitos nuevos:** RF-16 a RF-20. RF-15 queda corregido.

**Lo que no cambia:** el sistema sigue sin escribir `DESCANSO`, `NO TURNO` ni `INASISTENCIA`;
y **sigue sin haber State** — seis pares por día no son seis estados, son seis instancias de
`Turno`, así que el descarte se sostiene.

**Pendiente:** el diagrama de casos de uso queda incompleto con cuatro casos de
administradora; hay que rehacerlo con los seis. Y el tramo 2 del plan sube de peso: el
dashboard pasa de una pantalla a seis funciones.

## [2026-09-06] decision | ADR-007 — el endpoint público no tenía ni una defensa escrita

El usuario reencuadró el tope de 6 pares: **no es un límite de formato, es una medida de
seguridad** por si alguien intenta marcar muchas veces para tumbar el sistema. En la práctica
no se ven más de **2 rotaciones, o sea 3 pares**. Además confirmó que **no hay más cargos** que
podóloga y cajera, y añadió la distinción **jornada completa / media jornada**.

**Creado:** [[adr-007-seguridad-endpoint-publico]].

**El reencuadre abrió un frente que el wiki no había tocado en siete decisiones.** El
formulario de marcación es un **endpoint público, sin autenticación, que acepta subida de
archivos** (RF-01): la superficie de ataque más expuesta del sistema, y no tenía una sola
línea escrita sobre ella.

**El ataque más peligroso no es el que el usuario mencionó.** Marcar cientos de veces ensucia
el cronograma, sí. Pero una **imagen bomba** —100 KB comprimidos que declaran 30 000 × 30 000
píxeles— **mata la JVM al instante** con el heap de 384 MB de la VM B1s. No hace falta enviar
un archivo grande. Es el ataque que un sistema con subida de fotos y poca RAM tiene que
contemplar sí o sí, y la defensa es leer las dimensiones de la cabecera **antes** de
decodificar.

**Cuatro barreras, ordenadas de la más barata a la más cara de evaluar:** Nginx (tamaño ≤ 2 MB
y tasa por IP, cuesta cero porque no llega a Java) → validación de imagen real (bytes mágicos y
tope de 5000 × 5000 px, solo se lee la cabecera) → reglas de negocio (cooldown de 2 minutos y
tope diario, **una sola consulta indexada** para ambas) → procesar (~300 ms de CPU). El
principio es **rechazar lo antes posible**: descartar tras decodificar una imagen cuesta
cientos de milisegundos que en 1 vCPU son un lujo.

**Se corrige [[adr-006-dashboard-y-turnos-multiples]]:** ahí decía que un séptimo par "se
registra igual y marca la jornada para revisión". Como límite de formato tenía sentido; **como
defensa no sirve de nada**, porque aceptar por encima del tope es justo lo que el atacante
quiere. Ahora la marcación 13 **se rechaza** — pero **el intento se registra** en una tabla
nueva, `intento_rechazado`, y si el caso fuese legítimo se arregla por la corrección manual,
que pasa por una persona y no por el endpoint abierto. Así el límite es duro y ningún dato real
se pierde.

**Sin CAPTCHA, y se documenta por qué.** Sería la defensa obvia contra automatización, pero
[[requisitos]] RNF-01 dice que las usuarias tienen poca familiaridad tecnológica y que
cualquier fricción se traduce en marcaciones no registradas. Un CAPTCHA **rompería el requisito
principal del cliente** para defender algo que las cuatro barreras ya cubren. Es una decisión
de compromiso consciente, y buen material para la entrevista.

**El enlace lleva un segmento aleatorio** (`/m/7f3a9c2e4b81`), y se dice claramente que **no es
seguridad**: filtra el ruido automatizado, no sustituye a ninguna barrera.

**Jornada completa o media: se muestra, no se valida.** No se implementa ninguna regla del tipo
"media jornada no puede pasar de N horas" — nadie la pidió y rechazaría horas extra
legítimas. El sistema registra lo que ocurrió; interpretarlo es criterio de la administradora,
la misma disciplina que con `DESCANSO` e `INASISTENCIA`.

**No añade ningún patrón, y eso fue una tentación real.** Un filtro de limitación de tasa se
parece a Decorator, a Chain of Responsibility y a Proxy a la vez. Pero Chain of Responsibility
ni siquiera está entre los quince del curso, y bautizar como patrón nuevo lo que es un
**segundo uso de [[patron-proxy]]** sería inflar la cuenta. Se documenta como refuerzo de
Proxy: que el mismo patrón sirva al dashboard y al lado público fortalece su justificación en
vez de estirarla.

**Cambios en [[modelo-datos]]:** `jornada` en `trabajadora`, tabla nueva `intento_rechazado`, y
`sp_validar_marcacion` que resuelve cooldown y tope en una consulta. **Requisitos nuevos:**
RF-21 y RNF-07.

**Aparece un entregable que no estaba:** la configuración de Nginx deja de ser un detalle de
despliegue — los límites viven ahí.

**No hecho:** sigue sin escribirse código.

## [2026-09-07] decision | ADR-008 — el prototipo contra el wiki, y doce huecos cerrados

El usuario compartió un **prototipo navegable de la app de marcación** construido por otra vía
y pidió revisarlo antes de seguir. Contradecía al wiki en cinco puntos, así que la mitad del
trabajo fue **decidir cuál gana en cada uno y por qué**, no añadir cosas.

**Gana el wiki en tres.** Nada de **reconocimiento facial**: una foto procesada para
identificar es **dato biométrico**, o sea dato sensible bajo la Ley 29733, con consentimiento
expreso y por escrito — y no cabe en el nivel gratuito de Oracle. Nada de **GPS**: el usuario
lo dejó fuera del alcance el mismo día. Y el **tope diario sigue siendo 12 marcaciones**, no
las 6 del prototipo: con los números del propio usuario, 6 bloquearía a una trabajadora
legítima en el caso extremo que dice soportar.

**Gana el prototipo en dos.** No preguntar entrada o salida sino **inferirlo** del turno
abierto — `Turno` ya lo sabía hacer, así que quita un paso sin tocar el modelo. Y **marcar sin
señal**, que el wiki no contemplaba.

**La cola sin señal se acepta con condiciones**, porque tal cual rompía la regla que sostiene
el proyecto: el servidor es el dueño de la hora. Se guardan **dos momentos** —el del
dispositivo y el de llegada—, se marca `fuente_hora = DISPOSITIVO`, y se rechaza lo que llegue
con más de **2 horas** de retraso. El usuario eligió 2 sobre 12 a propósito. Así el sistema
**nunca afirma una hora que no puede probar**, que era lo único innegociable.

**Interfaz: Thymeleaf con HTMX**, un JAR y un servidor. La pregunta del usuario llevaba dentro
una confusión que conviene dejar escrita: el prototipo que admira **no usa ningún framework**.
Lo moderno viene de la tipografía, los radios y el espaciado, no de React.

**Diez decisiones de alcance más**, todas del usuario: 100–150 trabajadoras, una sola
administradora, arranque en blanco, exportación solo semanal, retención de 3 meses, sin turnos
que crucen medianoche, dominio propio, aviso de privacidad en papel a cargo de la empresa, y
`10:12AM -` para la jornada sin salida.

**Cambios en [[modelo-datos]]:** `fuente_hora` y `momento_recibido` en `marcacion`, y
`COLA_VENCIDA` en `intento_rechazado`. **En [[requisitos]]:** RF-04 derogado, RF-02 pasa a
rejilla de caras por sede, y se corrigió la nota de RF-10 que aún decía «las fotos van al disco»
cuando [[adr-004-despliegue-azure]] las había movido al almacén de objetos.

**[[huecos-abiertos]] cierra siete preguntas** (4, 5, 6, 10, 12, 16, 17), deja la 20 parcial y
**abre tres nuevas**: si existen fotos de referencia, si se prueba con una podóloga real, y con
qué correo se crea la cuenta de la nube — porque desplegar en una cuenta personal de estudiante
deja los datos de la empresa a nombre del alumno.

**No cambia ningún patrón.** Siguen siendo siete: ni el descarte del reconocimiento facial, ni
HTMX, ni la cola añaden o quitan nada.

**No hecho:** sigue sin escribirse código.

## [2026-09-07] decision | El contador de la pantalla no debe anunciar el tope

Corrección del usuario sobre la maqueta de confirmación: no «Marcación 1 de 12 de hoy» sino
**«1 de 2»**, luego «2 de 2», luego «3 de 4». El denominador es el **par en curso**.

Lo pidió como claridad para la trabajadora —una jornada son pares, no una cuota— y de paso
**cierra un agujero que yo no había visto**: mostrar el 12 en el formulario público es anunciar
el tope de la propia defensa, o sea decirle a cualquiera con el enlace cuánto aguanta el
sistema antes de rechazar. Con el contador por pares, el 12 aparece **solo en el mensaje de
rechazo**, cuando ya se alcanzó y ocultarlo no serviría de nada.

El tope duro de [[adr-007-seguridad-endpoint-publico]] no cambia: 12 marcaciones por
(trabajadora, sede, día), y la número 13 se rechaza. Lo que cambia es que el endpoint deja de
publicarlo. Escrito en [[marcacion-multiple]] y como decisión 6 de
[[adr-008-alcance-interfaz-y-cola-sin-senal]].

## [2026-09-07] decision | ADR-009 — la recapitulación encontró 6 puntos sobre la mesa

Recapitulación del proyecto entero contra la rúbrica, a petición del usuario. Salieron tres
cosas que el wiki no había mirado de frente.

**La primera vale 6 puntos.** De los diez patrones que PC-3 exige teníamos **siete**, y ese es
el criterio que más puntúa. Revisando los descartes de [[adr-001-patrones-seleccionados]]
aparece que **dos de los tres que faltaban ya tienen casa honesta**, y no por cambiar de
opinión sino porque **cambió el problema**:

- **Bridge** se descartó cuando eran «dos formatos de salida». Hoy las fotos viven en tres
  sitios ([[adr-005-hosting-largo-plazo]]) y hay dos tipos de foto —evidencia que se purga y
  referencia permanente ([[adr-008-alcance-interfaz-y-cola-sin-senal]])—. Dos jerarquías que
  varían por su cuenta.
- **Command** se descartó porque «solo aportaría el *undo*». Hoy existe la **cola sin señal**:
  capturar una petición como objeto, guardarla y ejecutarla más tarde es literalmente el
  patrón.

Los dos entran **por confirmar**, como Adapter: se deciden escribiendo la clase. Nueve patrones
bien defendidos valen más que diez que no se sostienen. **Prototype se queda fuera** y se
entrega igual, como sección «evaluación y descarte» del informe — un hueco en el índice es peor
que un descarte razonado.

**La segunda es el calendario.** La entrega es en **menos de un mes** y el plan estaba dibujado
a ocho semanas. Comprimido a cuatro, y **escrito qué se cae**: la migración a Oracle, las fotos
de referencia y la presentación a la dueña. Lo que no se cae, aunque tiente: las pruebas de
integración por capa y la evaluación de antipatrones, las dos obligatorias en la estructura del
informe. Para la segunda **la evidencia ya existe** —el ISP corregido con `Turno`, el Shotgun
Surgery de la validación duplicada, los cinco descartes— y solo falta redactarla.

**La tercera es que no existe ni una carpeta del entregable.** Escrita
[[estructura-repositorio]]: `MVC` y `SQL` por el anexo, y los paquetes de Java llamados
`vista`, `control`, `modelo` y `bd` en vez de la convención de Spring, porque quien abre el
repositorio tiene que **ver** la arquitectura, no traducirla. Un solo módulo Maven: cuatro
`pom.xml` son cuatro sitios donde romperse la semana de la entrega.

**Y un aviso que salió de rebote:** el repositorio **ya es público** y este wiki lleva **nombres
reales de trabajadoras** tomados del Excel de la empresa. En un repositorio público el historial
no se borra fácil.

**Cambios en [[modelo-datos]]:** `sp_intentos_rechazados`, el décimo procedimiento — sin él la
octava función del dashboard no tenía de dónde leer.

**No hecho:** sigue sin escribirse código, ahora por decisión explícita del usuario: primero la
propuesta completa en el enlace, para presentarla bien.

## [2026-09-08] consulta | Los patrones explicados sin jerga, para quien recién empieza

El usuario pidió que la presentación de avance explicara **cómo se aplican los patrones**, con
palabras entendibles para alguien que recién entra al curso. Archivado como
[[patrones-en-cristiano]] siguiendo §5 del contrato: la respuesta tenía valor duradero y no
debía morir en el chat.

**La forma que mejor funciona no es la lista, es el viaje.** Doce pasos desde que la trabajadora
toca ENVIAR hasta que el dashboard se refresca, con el patrón que actúa en cada uno. Deja ver lo
que una lista esconde: que el Facade llama al Factory, que el Factory produce el objeto que el
Decorator envuelve, y que el Observer desencadena al Composite sin conocerlo.

Para cada patrón, tres columnas: **qué hace en cristiano, dónde entra aquí, y qué pasaría sin
él.** Esa tercera columna es la importante — es la que convierte una definición memorizada en un
argumento, y es el orden en que hay que responder en la entrevista individual: **problema
primero, patrón después, consecuencia al final**.

**Sin inventar nada nuevo:** la página destila [[mapa-patron-requisito]], [[cuatro-capas]] y los
nueve ADR existentes. Prototype y State se explican también, porque un descarte razonado es
evidencia para el capítulo de antipatrones de [[pc3-entregable]].

**Corregidos tres datos desactualizados del artefacto** que aparecieron al releerlo entero: el
resumen del cuadro de patrones aún decía «7 se implementan · 1 depende · 7 descartados» cuando
[[adr-009-estructura-y-dos-patrones-que-vuelven]] lo dejó en 7 · 3 por confirmar · 5 fuera; el
árbol del repositorio decía 8 decisiones en vez de 9; y el pie citaba hasta ADR-008.

**No hecho:** sigue sin escribirse código.

## [2026-09-20] decision | ADR-010 — el sistema deja de decir que no

Ocho cambios del usuario de una sola vez. Seis son alcance; **dos chocan de frente con
decisiones anteriores**, y por contrato quedan registrados como tensión antes de resolverlos.

**El cambio de fondo:** *«siempre se tiene que poder tomar, no debe invalidar nada»*. Se deroga
toda validación de secuencia — doble entrada, doble salida, dos marcaciones seguidas en la misma
sede: **todo se acepta**. Desaparece la rama «Rechazar y avisar» que venía de
[[adr-003-login-y-cronograma-simple]]. `Turno` sigue infiriendo si abre o cierra, pero ahora
**sugiere en vez de bloquear**, y lo que queda suelto se muestra suelto: `10:12AM -` para una
entrada sin salida, `- 8:46PM` para una salida sin entrada.

**Primera tensión, sin resolver del todo.** El 2026-09-06 el usuario pidió expresamente un tope
—*«es importante un límite»*— y hoy dice que no se invalide nada. Las dos frases son suyas. La
propuesta es conservar el tope de 12 **solo como defensa del endpoint público**, nunca como
validación de la jornada, y bajar el cooldown de 2 minutos a 30 segundos, que es lo que
estorbaría a alguien corrigiendo un doble ingreso en el momento. **Queda como pregunta 27**: si
«no invalidar nada» aplica sin excepción, el formulario público se queda sin ningún límite.

**Segunda tensión.** Poder **escribir el nombre** cuando la trabajadora no está registrada choca
con la lista cerrada de [[adr-002-arquitectura-produccion]], que existe para que `MARY P.`,
`mary` y `Mari` no acaben siendo tres personas. Resolución: el texto libre **no crea una
trabajadora del padrón**, crea una **provisional** que la administradora confirma o fusiona. Su
marcación se guarda igual —nadie se queda sin marcar— pero no entra al cronograma exportado
hasta que alguien decida quién es. Eso obliga a que `cargo` y `jornada` admitan nulo, y añade la
novena función del dashboard.

**Lo demás:** el cargo sale de la plantilla y de las vistas; la exportación gana **hoja diaria**;
la advertencia de hora del dispositivo pasa a ser **permanente y viaja al Excel** como asterisco
con leyenda; y hay que aguantar **ráfagas de más de 80 personas** — donde la medida que de verdad
importa no es el caudal sino **acotar cuántas imágenes se decodifican a la vez**, porque eso es
lo que tumba un proceso.

**Y la plataforma pasa a ser Oracle Always Free**, sin pasar por Azure. Eso vuelve del revés dos
decisiones: la migración deja de estar fuera del alcance
([[adr-009-estructura-y-dos-patrones-que-vuelven]]) y **probar `aarch64` sube a ser el riesgo
número uno**, bloqueando la semana 1. De paso ayuda con la ráfaga: 2 OCPU y 12 GB contra el
1 vCPU y 1 GB de la B1s.

**Lo que hay que vigilar:** comprometerse con una sola nube **le quita una pata a Bridge**.
Quedan dos implementaciones reales y los dos tipos de foto. Sigue siendo defendible, pero más
delgado — y es exactamente el reclamo que prometimos revisar al escribir la clase.

**Cambios en [[modelo-datos]]:** `cargo` y `jornada` a nulo, `provisional` en `trabajadora`,
`sp_cronograma_diario` y `sp_provisionales`. **En [[requisitos]]:** RF-02 matizado por tercera
vez, RF-20 recortado, y nuevos RF-22 a RF-25 y RNF-09.

**No hecho:** sigue sin escribirse código.

## [2026-09-20] scaffold | MVC/ existe y compila

Primer paso del backend. **El contrato §7 se salvó de un fallo**: obliga a comprobar que las
herramientas *se resuelvan*, no que estén instaladas — y **Maven había desaparecido del disco
entero**, no solo del PATH. También estaba desfasado el resto de la tabla: Java pasó de
`jdk-26.0.2.1` a `jdk-26`, y Python de 3.14 a 3.13.

**No se reinstaló Maven.** En su lugar el proyecto usa el **Maven Wrapper**, que viaja dentro
de `MVC/` y descarga su propio Maven. Compila igual en las dos máquinas del equipo y en el
servidor ARM de Oracle sin instalar nada — que era justo el problema recurrente de esta
máquina.

**El esqueleto se generó con Spring Initializr** (Boot 4.1.1, Java 25, Maven, con web,
thymeleaf, jdbc, mysql, validation y devtools) y se adaptó:

- **`pom.xml` limpio**, con los bloques vacíos de Initializr fuera y **comentado el porqué de
  JDBC y no JPA**: PC-3 exige procedimientos almacenados como evidencia.
- **Cuatro paquetes con `package-info.java`** —`vista`, `control`, `modelo`, `bd`— cada uno
  documentando su responsabilidad, los patrones que aloja y **la regla que no se puede
  romper** en esa capa. Es lo primero que lee quien abre el repositorio.
- **`application.yml`** en vez de `.properties`, con las decisiones del wiki **como
  configuración citando su ADR**: cooldown de 30 s, tope de 12, cola de 2 h, 5000×5000 px,
  4 decodificaciones simultáneas, 90 días de retención, pool de 10 y 50 hilos.

**Dos sorpresas de Boot 4** que conviene tener escritas: el starter web ahora se llama
`spring-boot-starter-webmvc`, y los starters de test van **partidos por función** —ya no
existe un único `spring-boot-starter-test`.

**Y una trampa de Initializr que costó un build:** genera `<version>4.1.1.RELEASE</version>`
y **ese artefacto no existe en Central**. La versión real es `4.1.1`, sin sufijo. Corregido y
anotado en `CLAUDE.md` §7 para que no vuelva a pasar.

**Estado:** `.\mvnw.cmd -B clean compile` → **BUILD SUCCESS**, `javac ... release 25`.

**Hallazgo que desbloquea el siguiente paso:** MySQL 8.0 ya está instalado y el servicio
`MySQL80` está corriendo. Falta acordar las credenciales para crear el esquema.

**No hecho:** ni una tabla, ni una clase de dominio. `SQL/` todavía no existe.

## [2026-09-20] scaffold | SQL/ escrito, cargado y probado

Segundo paso del backend. `SQL/` existe con sus cuatro archivos y esta cargado en MySQL
8.0.42.

**El usuario propuso usar `@Entity` y `ddl-auto` en vez de escribir el SQL a mano.** Es una
propuesta razonable y se contesto por escrito en [[adr-011-jpa-o-sql-a-mano]] en vez de
despacharla: la razon que decide no es de gusto, es que `SQL/` es una **carpeta calificada por
el anexo**, asi que los scripts hay que entregarlos de todos modos y la pregunta real era
«SQL, o SQL *mas* JPA» — dos definiciones del mismo esquema que se separan. Se recoge lo que
la propuesta tenia de razon: `SimpleJdbcCall` y `RowMapper` evitan escribir la traduccion
fila-objeto a mano.

**Lo escrito:** 6 tablas, **16 procedimientos**, el evento nocturno de purga y un semillero de
457 marcaciones sobre 13 personas y dos semanas.

**Tres cosas que el wiki decia mal y el SQL destapo:**

- [[modelo-datos]] abria con «cinco tablas» mientras describia **seis**.
- El numero de procedimientos no cuadraba: la tabla listaba 11 y el cuerpo decia «diez».
- **Cuatro procedimientos faltaban de verdad, no por alcance sino por agujero:**
  `intento_rechazado` se leia pero nada la escribia, las provisionales igual, y
  `administrador.ultimo_acceso` era una columna muerta.

**Una contradiccion de diseno, registrada y resuelta partiendo el paso en dos.** La pagina
prometia que `sp_registrar_marcacion` recalcula `jornada_diaria` en el motor, y mas abajo
prometia que emparejar entradas con salidas vive **solo** en Java. Las dos no caben. Ahora el
procedimiento inserta y **devuelve el dia entero ordenado** en la misma llamada; Java arma los
turnos y llama a `sp_actualizar_jornada` con la celda resuelta. Las dos llamadas van en la
misma transaccion de Spring. **El costo, dicho sin adornos:** «atomico dentro del motor» deja
de ser el argumento que justifica el procedimiento; el que queda en pie es «un viaje a la base
en vez de dos». Detalle en `## Tensiones` de [[modelo-datos]].

**Decision que se mantuvo a proposito:** el cooldown de 30 s y el tope de 12 **no estan
escritos en el SQL**. `sp_validar_marcacion` devuelve la cuenta y los segundos medidos, y Java
los compara contra `application.yml`. Los procedimientos cuentan y guardan; quien decide es el
modelo.

**Estado comprobado:** 17 pruebas de humo, todas pasan, entrando como el usuario `asistencia`
y no como root — asi se verifico de paso que los permisos alcanzan. El contador que ve la
trabajadora sale **«Marcacion 1 de 2», «2 de 2», «3 de 4»**, nunca «de 12». Las tildes y la
enie sobreviven de punta a punta. El hash BCrypt de la administradora es real y se verifico
que casa con su clave.

**Higiene cumplida:** los 13 nombres de `03_datos_prueba.sql` son **inventados**. El
repositorio es publico y el video se sube al aula virtual. Las coordenadas de las 7 sedes si
son reales, porque son locales comerciales y no datos de personas.

**Pendiente de la misma tarea:** el wiki sigue teniendo **nombres reales de trabajadoras**
tomados de [[formato-cronograma-excel]]. El SQL ya esta limpio; el wiki no.

**No hecho:** ni una clase Java de dominio, ni un `RowMapper`, ni una pantalla. `MVC/` sigue
teniendo solo el esqueleto.

## [2026-09-20] scaffold | La capa modelo, con el Composite y el Singleton

Tercer paso. `MVC/` ya tiene codigo: **dieciseis clases en `modelo/`**, Java puro, sin Spring y sin
base de datos.

**El Composite completo, no solo nombrado.** Una interfaz `PeriodoTrabajado` con `minutos()`,
`titulo()` y `partes()`, e implementada en los cuatro niveles que el dominio ya describia:
`SemanaSede` -> `DiaSede` -> `Jornada` -> `Turno`. Preguntar las horas es la misma llamada en
cualquier nivel.

> **Un limite que se puso a proposito:** `Marcacion` **no** implementa la interfaz, aunque
> seria mas simetrico. Una entrada a las 9:00 no es tiempo trabajado, es un instante; el
> tiempo aparece cuando hay un par. Estirar el patron un nivel mas solo por simetria seria el
> *Golden Hammer* de [[s14-antipatrones]].

**El emparejamiento vive en `Jornada.de(...)` y solo ahi**, que es lo que
[[adr-011-jpa-o-sql-a-mano]] prometio. Los cuatro casos de [[adr-010-sin-rechazos-oracle-y-nombre-libre]]
estan implementados sin que ninguno rechace ni descarte nada: doble entrada cierra la anterior
coja, doble salida deja la segunda huerfana, y las dos se ven.

**`ContadorDeHoy` es una clase entera para una division**, y con motivo: es la correccion
explicita del usuario. «Marcacion 1 de 2», «2 de 2», «3 de 4» — nunca «de 12».

**19 pruebas, todas pasan, en milisegundos.** Ninguna levanta Spring ni toca MySQL.

**Y una de ellas encontro un error de verdad, ya corregido en los dos sitios.** `sp_dia_sede`
derivaba «turno abierto» de `ultima_salida IS NULL`, copiado de [[modelo-datos]]. **Esta mal en
cuanto hay mas de un turno**: quien hace 9-1, 2-6 y vuelve a entrar a las 7 tiene ultima salida
y sigue dentro. La regla buena es «la ultima marcacion del dia fue una entrada». El error venia
de la frase «solo dos situaciones posibles», escrita pensando en la jornada de un turno cuando
[[marcacion-multiple]] dice desde el primer dia que eso no se puede asumir.

**Una prueba mia tambien estaba mal y se reescribio.** Afirmaba que el contador «nunca dice
12», y con 11 marcaciones dice «11 de 12» por pura aritmetica del redondeo al par. La propiedad
que de verdad protege el endpoint publico es otra y es mas fuerte: **el total mostrado nunca
supera lo hecho mas uno**, asi que el mensaje jamas revela cuanto margen queda.

**Decision registrada en `CatalogoSedes`:** es un Singleton clasico y no un `@Bean` de Spring,
que seria lo idiomatico. El motivo no es preferencia: PC-3 pide el patron **en la capa
modelo**, y esa capa no puede importar el framework. El costo -estado global, pruebas
acopladas- se paga con un `limpiar()` documentado como «solo para pruebas».

**No hecho:** la capa `bd` sigue vacia. Ninguna de estas clases ha visto todavia una fila de
MySQL: los `RowMapper` y las llamadas a los 16 procedimientos son el paso siguiente.

**Observacion sin relacion:** aparecio `MVC/.github/modernize/java-upgrade/` en el repositorio.
No lo creo este trabajo; parece de una extension del editor. Se deja como esta, pero conviene
decidir si entra al repositorio publico o al `.gitignore`.

## [2026-09-20] decision | ADR-012: fuera cargos, fuera rechazos

Tres instrucciones del usuario despues de ver el esquema cargado. La tercera **revierte
decisiones de tres ADR anteriores**, asi que quedan escritas en
[[adr-012-sin-cargos-y-sin-rechazos]].

**1. Fuera `cargo` y `tipo de jornada`.** Eliminados del esquema, del modelo Java y de todas
las consultas. [[modelo-datos]] ya decia que los dos **se muestran, no se validan**: eran datos
que no cambiaban ni una hora registrada. El argumento que decide no es el ahorro sino el
tercero: **una columna «media jornada» tienta a inventar una regla que avise al pasar de cuatro
horas**, y eso rechazaria horas extra legitimas.

**2. Exportacion diaria y semanal por sede: verificado a medias, y se dice cual mitad.** Los
dos procedimientos existen, devuelven **las mismas columnas a proposito** —un solo generador
de Excel que mantener— y la semanal trae la rejilla completa con los dias vacios incluidos.
**El archivo `.xlsx` en si NO existe todavia**: falta Apache POI y va en la capa `control`.

**3. El sistema no rechaza absolutamente nada. Cierra el hueco 27.** Caen el tope de 12, el
cooldown de 30 s y la cola vencida de 2 h. **No se subieron: se quitaron**, para que nadie los
reponga creyendo que solo habia que afinarlos.

> El argumento es de asimetria de danos y aguanta: rechazar una marcacion legitima le borra a
> una persona una hora de trabajo real y la obliga a perseguir a la administradora; aceptar una
> rara anade una linea que se resuelve en diez segundos. Rechazar protege contra un fraude que
> **nadie ha reportado**; aceptar protege contra un fallo que pasa todas las semanas.

**La contrapartida, pagada y no prometida:** `intento_rechazado` **se renombra a `incidencia`**
y cambia de sentido —guarda lo que se **acepto** y no se entendio—, cada fila **apunta a la
marcacion que si se guardo**, y lleva `revisada` para que la lista no se vuelva un sitio al que
nadie entra. Un sistema que lo acepta todo y no dice nada es peor que uno estricto.

**La unica barrera que sobrevive no protege contra la trabajadora:** una imagen de mas de
5000x5000 px se descarta sin decodificar, porque es la bomba de descompresion y dejaria sin
servicio a las otras 98. **La marcacion se guarda igual, sin foto.** Lo unico que este sistema
sigue rechazando es un archivo, nunca una hora trabajada.

**Un efecto de segundo orden que se escapaba:** sin tope de marcaciones, `etiqueta` podia
crecer mas alla del `VARCHAR(160)` y **MySQL la cortaria en silencio**. Ampliada a 400, con
corte por linea entera en Java y una prueba que lo fija.

**`sp_validar_marcacion` pasa a llamarse `sp_contexto_marcacion`.** Ya no valida; mide. Un
nombre que miente sobre lo que hace una funcion es la peor clase de documentacion.

**`sede.id` pasa a autoincremental**: el usuario dice que habra mas sedes, y dar de alta la
octava tiene que ser un INSERT y no una migracion.

**Juego de datos rehecho a escala real: 99 trabajadoras y 3 943 marcaciones** (antes 13 y 457).
Con doce personas ninguna consulta parece lenta y no se prueba nada sobre el cambio de turno.

**Saturacion: medido, no estimado.** `sp_contexto_marcacion`, que corre en **cada** marcacion,
tarda **0,7 ms**; el cronograma semanal de una sede, 4,2 ms; el diario, 2,1 ms. **El cuello de
botella no es la base de datos** —una conexion sola haria mas de mil marcaciones por segundo—
sino decodificar fotos. Esa medida sigue en pie: 4 decodificaciones simultaneas, y la
**marcacion se guarda ANTES de procesar la foto**, para que una cola de fotos no sea una cola
de personas esperando.

**Estado:** los cuatro scripts recargados de cero, **22 pruebas Java, todas pasan**. Se
eliminaron `Cargo.java`, `TipoJornada.java` y `MotivoRechazo.java`; entro `TipoIncidencia.java`.

**No hecho:** el generador de Excel, la capa `bd` y la capa `control` siguen vacias.

## [2026-10-06] scaffold | Se retoma en otro ordenador: el entorno reverificado

El proyecto estuvo parado del 2026-09-20 al 2026-10-06 y se retoma **en una máquina
distinta**. Antes de tocar código se comprobó que las herramientas **se resuelven**, no que
estén instaladas, como manda `CLAUDE.md` §7. La tabla de §7 quedó reescrita.

**Lo que difiere de la máquina anterior:**

- **MySQL no está instalado.** Ni `mysql.exe`, ni servicio `MySQL80`, ni nada escuchando en
  el 3306. Los cuatro scripts de `SQL/` **no se pudieron correr**: es el bloqueo de esta
  sesión y la capa `bd` espera a que se resuelva.
- Java es **26.0.2.1** (`jdk-26.0.2.1`, no `jdk-26`) y Python **3.14.7** (no 3.13).
- **Maven suelto sí está** en el PATH. No cambia la regla: se compila con `.\mvnw.cmd`.
- El PATH del shell está al día; ya no hace falta exportarlo en cada comando.

**Pruebas: 22 de 22 pasan** con `.\mvnw.cmd -B test`, **BUILD SUCCESS**.

**Hallazgo que corrige una afirmación de `PARA-RETOMAR.md` §4.4:** `contextLoads()` **pasó sin
MySQL instalado**. No es que la base funcione: es que el pool de Hikari se abre de forma
perezosa y, con la capa `bd` vacía, ningún bean pide una conexión. Hoy las 22 pruebas en verde
**no dicen nada de la base de datos**. La verificación contra MySQL tendrán que darla las
pruebas de integración de `bd`.

**Higiene:** aparece `.vscode/settings.json` sin seguimiento en la raíz. No se toca; queda por
decidir si va al `.gitignore`.

**No hecho:** la base de datos, y por tanto la capa `bd`. Pendiente de decidir cómo se instala
MySQL en esta máquina.

## [2026-10-06] scaffold | La capa bd: los 17 procedimientos, y el circuito que no pierde horas

**MySQL 8.0.46** lo instaló el usuario; los cuatro scripts se cargaron y se verificaron entrando
como `asistencia`: 6 tablas, 17 procedimientos, el evento de purga, y las tildes y eñes
guardadas como UTF-8 real. **Trampa encontrada antes de pisarla:** en PowerShell 5.1,
`Get-Content … | mysql` —la receta de `PARA-RETOMAR.md`— convierte las tildes en `?` sin
avisar. Se carga con `cmd /c "mysql … < archivo"`. Anotado en `CLAUDE.md` §7.

**El modelo vio sus primeras filas reales, y encontró un error en el semillero.**
`CruceModeloContraBaseTest` recalcula en Java las 1 178 celdas que el semillero calculó en
SQL. **Cinco no cuadraban, y en las cinco tenía razón Java:** los casos raros de
`03_datos_prueba.sql` insertaban marcaciones sin rehacer su celda —el mismo fallo que los
conteos MAÑANA/TARDE congelados del Excel del cliente—. Corregido el semillero sin perder
ningún escenario: **1 180 de 1 180** celdas idénticas carácter por carácter.

**El circuito se escribió primero ingenuo, para verlo fallar.** Con un `@Transactional` a secas:

- **5 de 5 rondas** de 10 envíos simultáneos dejaron la celda mal —una con **más** minutos de
  los reales—. Es el doble toque del botón.
- La marcación 512 del día **se perdió** por un error al guardar su celda (`turnos` es
  `TINYINT`, máximo 255).

Arreglo en [[adr-013-la-transaccion-de-una-marcacion]]: **candado por persona** dentro del
procedimiento, **`READ COMMITTED`** y **punto de guardado** alrededor de la celda. Las mismas
pruebas, sin tocarlas: **25 de 25 rondas** correctas y la 512 guardada.

**La misma regla para los textos:** MySQL estricto da error con un texto más largo que su
columna, y deshace la llamada entera. `Recorte` corta la nota, nunca la marcación, contando
caracteres de verdad (un emoji son dos unidades en Java y uno en MySQL).

**Dos cambios de columnas en `02_procedimientos.sql`, con el mismo criterio que el cronograma
diario/semanal:** `sp_registrar_marcacion` devuelve las nueve columnas de una marcación y
`sp_historial_trabajadora` las mismas que `sp_marcaciones_del_dia`. Resultado: **un mapper por
concepto**, no uno por procedimiento.

**Lo escrito:** 6 clases `*BD` con los 17 procedimientos, 2 mappers a mano (`MarcacionMapper`,
`FilaMarcacion`) y 6 records con `DataClassRowMapper`. **46 pruebas, todas pasan**: las 22 de
antes y 24 nuevas contra MySQL.

**Corregido de paso:** `bd/package-info.java` seguía diciendo que la jornada se recalcula
«dentro del motor»; [[decisiones]] no listaba ADR-011 ni ADR-012; [[huecos-abiertos]] seguía con
el 27 y el 28 abiertos, que ADR-012 ya había cerrado.

**Huecos nuevos, 30 a 35** en [[huecos-abiertos]]. Los dos 🔴 bloquean la capa `control`:
**no hay procedimiento para listar sedes ni el padrón**, y **la foto choca con la
inmutabilidad de la marcación** (la regla 2 manda guardar antes de procesar la foto, pero la
ruta se escribe en el `INSERT` y la fila no se edita nunca).

**No hecho:** la capa `control` (fachada de marcación, Excel con Apache POI) y la `vista`.

## [2026-10-06] decision | Hueco 32 cerrado y ADR-014 propuesto: marcar sin esperar

**El usuario confirma las 7 sedes** (Los Olivos, Surco, Miraflores, San Miguel, San Borja,
Lince, La Molina): coinciden con las del semillero. El formulario real de trabajadoras llegará
después; mientras, sirven los **98 nombres ficticios** del semillero.

**Hueco 32 cerrado:** `sp_sedes` y `sp_padron`, con `SedesBD` y `TrabajadorasBD.padron`. Ya son
**19 procedimientos**. El padrón **no devuelve el documento**: alimenta un formulario público
sin login. Sale con las provisionales marcadas, para que quien escribió su nombre ayer lo toque
hoy en vez de escribirlo distinto. Y una prueba carga por primera vez el Singleton
`CatalogoSedes` con datos reales. **49 pruebas, todas pasan.**

**Pedido del usuario:** que marcar sea rápido y que nunca se quede cargando ni sin registro,
haya o no internet. Respuesta en [[adr-014-marcar-sin-esperar]], **como propuesta**, porque
cambia tres cosas de [[adr-008-alcance-interfaz-y-cola-sin-senal]]. La idea central: **la
marcación queda registrada en el celular al tocar el botón, y enviarla es trabajo de fondo.**

- La página abre sin internet (service worker con sedes y padrón guardados).
- **Dos vistos, como WhatsApp**: ✓ guardada en el celular, ✓✓ recibida. Nunca «error».
- **Datos y foto por separado**: los datos llegan con una rayita de señal; la foto, después.
  Eso **resuelve el hueco 33** si se aprueba: la foto a una tabla propia, solo inserción.
- **Clave de envío** contra el caso más traicionero: el servidor guardó, la respuesta se perdió,
  el celular reenvía. Sin `UNIQUE` —es una búsqueda bajo el candado de ADR-013—, así que no
  contradice ADR-012: dos toques siguen siendo dos marcaciones.
- **La hora la calcula el servidor**: el celular manda cuánto tiempo pasó desde el toque, no
  qué hora es. Un teléfono con la hora mal puesta deja de importar.

**Dicho sin adornos en el ADR:** una página web no puede salvar una marcación de un celular que
se rompe antes de enviar, iPhone no envía en segundo plano, y puede borrar los datos de una web
no instalada tras 7 días sin uso.

**Pendiente:** las cuatro preguntas del final de [[adr-014-marcar-sin-esperar]].

## [2026-10-06] decision | ADR-014 aceptado: la foto en su tabla, la clave de envío construida

**Respuestas del usuario:** foto en su propia tabla, **sí**; pedir que instalen la página en la
pantalla de inicio, **sí**; dos minutos como frontera de la hora, **sí**; aparato fijo por sede,
**no** —son demasiadas podólogas, y el teléfono de la sede tiene otros usos—: queda solo para
excepciones.

**El usuario preguntó si instalar la página en iPhone hace que las pendientes se envíen solas.
La respuesta fue que no**, y se le dijo antes de pedirle nada a nadie: Safari no tiene
*Background Sync*, instalada o no. Instalarla sí evita que iPhone borre las pendientes a los 7
días. Registrado en [[adr-014-marcar-sin-esperar]], con el hueco 36 (¿cuántas usan iPhone?).

**Construida la parte de datos de ADR-014:**

- `marcacion` pierde las tres columnas de foto y gana `clave_envio` (índice **no único**: el
  reenvío se reconoce bajo el candado de ADR-013, sin ninguna restricción que pueda hacer fallar
  un `INSERT`). Tabla nueva `foto_marcacion`. **Cierra el hueco 33.**
- `sp_registrar_marcacion` devuelve la marcación original con `p_repetida` cuando es un
  reenvío. Nuevo `sp_registrar_foto`, que busca por clave y no por id —el celular puede no
  conocer el id, que es justo el caso que se protege—. **20 procedimientos, 7 tablas.**
- **Las consultas del dashboard devuelven las mismas columnas de foto que antes**, leídas de la
  tabla nueva. Ni `FilaMarcacion` ni su mapper cambiaron: el contrato de columnas aguantó un
  cambio de esquema sin que Java se enterara.
- **Un tropiezo útil al recargar:** el semillero borraba una marcación que ahora tenía foto, y
  la clave foránea lo impidió. Hizo justo lo que debía.

**51 pruebas, todas pasan.** Las nuevas: la foto llega después y reenviarla no la duplica; un
reenvío con la hora recalculada sigue siendo la misma marcación; **8 reenvíos simultáneos de la
misma clave dejan una sola marcación**, estable en 5 de 5 corridas; dos toques con claves
distintas siguen siendo dos marcaciones, como manda ADR-012.

**Pendiente para `control`:** `momento = hora del servidor − segundos transcurridos`, la
frontera de 2 minutos y qué hacer con una foto de clave inexistente (hueco 37). **Para
`vista`:** el service worker, la cola en IndexedDB y los dos vistos.

## [2026-10-06] decision | Foto sin marcación: se guarda y se avisa

**Decisión del usuario sobre el hueco 37:** si llega una foto cuya clave de envío no corresponde
a ninguna marcación, **se guarda y queda un aviso para la administradora**. Nunca se tira: si
pasa, algo raro ocurrió por el camino, y la foto es la evidencia para entender qué.

- Nuevo tipo `FOTO_SIN_MARCACION` en el `ENUM` de `incidencia` y en `TipoIncidencia`.
- `incidencia` gana `clave_envio` y `foto_ruta`, para que la administradora **vea** la foto y la
  relacione con quien corresponda. `sp_registrar_incidencia` y `sp_incidencias` los llevan.
- **Dónde vive cada cosa:** la regla («guardar y avisar») es de negocio y va en la capa
  `control`; la capa `bd` solo pone las piezas. Los procedimientos siguen sin decidir nada.
- El aviso sale **en la fila de la persona** que declaró el celular al mandar la foto, en la
  pestaña del dashboard de ese día, no en una lista aparte. Hay prueba que lo comprueba.

**52 pruebas, todas pasan.** Hueco 37 cerrado.
