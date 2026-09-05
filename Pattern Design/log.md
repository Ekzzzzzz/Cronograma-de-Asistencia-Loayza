---
tipo: moc
titulo: Bitácora
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Bitácora

Registro cronológico **append-only** de lo que ha pasado en esta wiki. Las entradas nuevas
se añaden **al final**. Formato definido en `CLAUDE.md` §9.

Últimas entradas: `grep "^## \[" log.md | tail -5`

---

## [2026-09-04] esquema | Creación de la bóveda

- Se crea `CLAUDE.md` con las reglas del agente: tres capas, convenciones de página,
  frontmatter, citas y los cinco flujos (ingesta, consulta, decisión, implementación, lint).
- Se crea la estructura: `wiki/{patrones,conceptos,fuentes,dominio,decisiones,consultas,assets}`,
  más `tools/`, `src/` y `docs/`.
- Se crean [[index]], [[log]], [[overview]] y [[sintesis]], y un MOC por carpeta.
- Se registra el enunciado del problema en [[problema-cronogramas]] a partir del aporte
  del usuario, con las preguntas abiertas marcadas.
- Se añade `tools/extraer.py` para leer `.pptx`, `.docx` y `.xlsx` sin dependencias.
- Entorno verificado: Python 3.13 sin librerías Office, Java 26, sin Maven ni Gradle.
- **Pendiente:** ingerir las 8 sesiones de teoría de patrones (backlog en [[index]]) y
  responder las preguntas abiertas de [[problema-cronogramas]].

## [2026-09-04] ingesta | docs/Cronograma_2026_Olivos.xlsx — cronograma real de Los Olivos

- El usuario aporta una muestra del proceso actual: el Excel de horarios de la sede
  Los Olivos, dic 2025 – jul 2026. 2 hojas, 25 bloques semanales, 5.025 celdas con valor.
- Se crea [[formato-cronograma-actual]] con la anatomía del archivo, el vocabulario de
  estados (NO TURNO, DESCANSO, LIBRE, VACACIONES, INASISTENCIA, PERMISO, FERIADO) y la
  evidencia de los fallos del proceso manual.
- Hallazgo principal: el archivo **ya no es una programación prevista, es un registro de
  asistencia real** transcrito a ojo desde las fotos. Los turnos planificados sólo
  aparecen en el primer bloque.
- Segundo hallazgo: los conteos MAÑANA/TARDE al pie están **congelados en 22 de 25
  bloques** (copiados y nunca recalculados). El indicador de dotación es falso hace meses.
- Se actualizan [[sintesis]] (eslabón 7 con destino conocido), [[problema-cronogramas]]
  (2 preguntas respondidas, 3 nuevas), [[dominio-moc]] e [[index]].
- Se ajusta `CLAUDE.md` §2 y §3: `docs/` también aloja muestras del negocio, inmutables.
- **Pendiente:** 10 preguntas abiertas en [[problema-cronogramas]]; la bloqueante sigue
  siendo cómo llegan las imágenes de WhatsApp al programa.

## [2026-09-04] decision | ADR-001 y ADR-002 — stack, arquitectura e ingesta

- El usuario fija el alcance: **Spring Boot (backend + frontend)**, el profesor lo acepta
  mientras se apliquen patrones sobre un lenguaje orientado a objetos. Destino: **uso real
  en la empresa**, no sólo entregable de curso.
- Requisito nuevo: el sistema es **reactivo**. Al llegar una imagen la analiza y decide si
  es foto de marcación (la incorpora) o no (la ignora). La **clasificación previa** pasa a
  ser etapa de primera clase de la tubería.
- Se crea [[adr-001-stack-y-arquitectura]] (propuesta): Spring Boot con `./mvnw`, base de
  datos como almacén y Excel sólo como exportación, tubería de 7 etapas y cola de revisión
  humana para todo lo que quede bajo el umbral de confianza.
- Se crea [[adr-002-ingesta-whatsapp]] (propuesta): puerto de ingesta con adaptadores.
  **Riesgo externo documentado**: la API oficial de WhatsApp no cubre lectura de grupos, y
  las librerías que sí lo hacen (Baileys, whatsapp-web.js) violan los términos de servicio
  y exponen el número a bloqueo. Se arranca con adaptadores sin riesgo (subida manual y
  exportación `.zip`).
- Se actualiza `CLAUDE.md` §6 y §8 con el stack y **cuatro principios de diseño no
  negociables**: nunca inventar un dato, vocabulario normalizado, agregados siempre
  recalculados, fuente de imágenes intercambiable.
- Se actualizan [[sintesis]] (tubería de 7 etapas y riesgos), [[decisiones-moc]] e [[index]].
- **Pendiente:** aceptar ambos ADR e ingerir S06–S13; las decisiones de qué patrón va en
  cada etapa están bloqueadas hasta tener el catálogo.

## [2026-09-04] decision | Giro estratégico — se abandona WhatsApp, canal propio (PWA)

- El usuario **rechaza el enfoque WhatsApp**: no se acepta el riesgo de bloqueo del número.
  Se quiere un sistema 100 % automatizado, antifraude y profesional.
- **Nuevo canal**: PWA *mobile-first* en Vercel; las trabajadoras marcan con la cámara del
  celular y pueden añadir un comentario. Backend Spring Boot con Spring Security + JWT,
  sesión persistente atada al dispositivo. Panel de administración responsive. El padrón se
  construye al enrolarlas.
- Se reescribe [[adr-001-stack-y-arquitectura]] y se reemplaza `adr-002-ingesta-whatsapp`
  por [[adr-002-canal-de-marcacion]] (archivo anterior eliminado, decisión nunca aceptada).
  Se crea [[adr-003-autenticacion-jwt]]. **Los tres quedan aceptados.**
- Se crea [[antifraude]], la página que da sentido al sistema: 7 vectores de fraude con su
  defensa, y el reconocimiento de que **el cliente nunca es confiable** — `capture` y la
  geolocalización del navegador se pueden falsear, así que toda verificación vive en el
  servidor y **el servidor es la única autoridad de la hora**.
- **Consecuencia principal del giro**: la tubería se acorta de 7 etapas a 6 y desaparecen
  el OCR, la clasificación de imágenes y el emparejamiento por nombre escrito a mano. El
  problema pasa de *interpretar evidencia ajena* a *generar evidencia propia*.
- Se actualizan `CLAUDE.md` §1 y §8 (dos principios nuevos: nada de antifraude depende del
  cliente; toda marcación conserva su evidencia), [[sintesis]], [[problema-cronogramas]]
  (5 preguntas resueltas, 7 nuevas), [[dominio-moc]], [[decisiones-moc]] e [[index]].
- **Pendiente:** ingerir S06–S13 para desbloquear las 8 decisiones de patrón.

## [2026-09-04] ingesta | S06–S13 de corrido — catálogo completo de patrones

- Ingeridas **7 sesiones de teoría** (S06, S07, S08, S09, S11, S12, S13) con
  `tools/extraer.py`. Se crean 7 páginas en `wiki/fuentes/` y **15 páginas de patrón**,
  cada una con su sección «Aplicación en Podología Loayza».
- **Hallazgo sobre el material**: S06–S09 traen código Java completo; **de S11 en adelante
  las diapositivas sólo traen definiciones y tablas de ventajas y desventajas**. El código
  de Proxy, Bridge, State, Observer, Command y Memento está en los `.docx` de taller, sin
  ingerir. Marcado como hueco en cada página afectada.
- **Error detectado en la fuente**: S13 diapositiva 10, titulada «Patrón Command», describe
  en realidad el Memento. Registrado en [[fuente-s13-command-memento]] y
  [[patron-command]].
- **Evaluación**: 11 patrones candidatos, **2 descartados a propósito** —
  [[patron-singleton]] (Spring ya da alcance singleton; hacerlo a mano añade estado global
  e impide probar) y [[patron-abstract-factory]] (no hay familias de productos coherentes).
  El propio curso respalda ambas decisiones.
- **Mejor encaje encontrado**: [[patron-composite]] para la cadena de verificaciones de
  [[antifraude]], y [[patron-command]] + [[patron-memento]] para convertir las correcciones
  de la administradora en un registro de auditoría con deshacer — que es un requisito real
  del negocio, no un adorno.
- Se actualizan [[patrones-moc]] (reescrito), [[fuentes-moc]] (reescrito), [[sintesis]]
  (asignación preliminar de patrón por etapa) e [[index]].
- **Pendiente:** ingerir S14 (antipatrones) antes de escribir código — el curso pide
  evaluarlos sobre el propio proyecto; e ingerir los 3 `.docx` de taller si hace falta el
  código Java del curso. Luego, los 8 ADR de patrón.

## [2026-09-04] ingesta | Sedes — direcciones y coordenadas

- **Corrección del usuario: son 7 sedes, no 8.** Propagado a `CLAUDE.md` §1 y §8,
  [[problema-cronogramas]], [[antifraude]] y [[adr-002-canal-de-marcacion]].
- Se crea [[sedes]] con las 7 direcciones. **5 de 7 con coordenadas**, obtenidas
  decodificando los Plus Codes con `tools/pluscode.py` (implementación del algoritmo público
  Open Location Code, validada antes de usarla contra el Plus Code de Google Zúrich).
  Faltan San Miguel y Surco, que no traían Plus Code.
- **Hallazgo que fija el diseño de la geocerca**: las dos sedes más cercanas —San Borja y
  Miraflores— están a **3,3 km**. Ninguna geocerca razonable puede confundirlas, así que el
  radio puede ser generoso. Se propone **200 m**.
- **Y hace falta que lo sea**: 4 de las 7 sedes están en pisos altos o dentro de galerías
  (Miraflores está en el piso 6 de la Galería Multicentro). El GPS en interiores y en altura
  se degrada de 5–10 m a 50–200 m. Una cerca estrecha rechazaría marcaciones legítimas todos
  los días.
- Se propone una **regla de geocerca sensible a `coords.accuracy`** con cuatro veredictos,
  **ninguno de rechazo automático**: la geocerca es una señal más de la cadena, no un
  portero. Pendiente de fijar en ADR.
- Se añade `tools/pluscode.py` (decodifica Plus Codes y calcula distancias).
- **Pendiente:** Plus Code o coordenadas de San Miguel y Surco; dirección completa de San
  Borja; verificar una coordenada en el mapa antes de fijarla como configuración.

## [2026-09-05] ingesta | Sedes completas — las 7 con coordenadas

- El usuario aporta los Plus Codes que faltaban (**San Miguel** `WWF4+47`, **Surco**
  `R2X6+FC`), la dirección completa de **San Borja** (Av. Aviación 3550) y **el piso de cada
  sede**. [[sedes]] queda cerrado: 7 de 7 resueltas.
- Contraste de coherencia: San Miguel cae sobre Av. La Marina a la altura de la UPC y
  Miraflores sobre Av. Larco junto al Parque Kennedy, como dicen sus direcciones.
- **Dato nuevo que endurece el problema del GPS**: son **5 de 7 sedes en pisos altos**, no 4.
  Lince está en el **piso 12** — el caso más extremo, por encima de Miraflores (piso 6).
  Sólo Los Olivos y Surco están a pie de calle.
- La distancia mínima entre sedes **no cambia**: San Borja–Miraflores, 3 333 m. La geocerca
  de 200 m sigue siendo segura y necesaria.
- Se añade que el radio es **configuración por sede, no constante global**, y que conviene
  calibrarlo midiendo la precisión real que reporten los dispositivos las primeras semanas.
- Se actualiza `tools/pluscode.py` con las 7 sedes, más [[antifraude]],
  [[problema-cronogramas]] e [[index]].
- **Pendiente:** verificar una coordenada en el mapa antes de fijarla como configuración.
  Siguen abiertas las preguntas 2 a 10.

## [2026-09-05] decision | Flujo de marcación, enrolamiento y llenado del cronograma

Cuatro respuestas del usuario, todas registradas:

- **Sede: preseleccionada, ella confirma.** El GPS valida pero no decide — con 5 de 7 sedes
  en pisos altos ([[sedes]]), dejar que una lectura mala eligiera la sede sería frágil.
- **Entrada o salida: lo infiere el sistema y lo muestra** antes de enviar. Se mantiene el
  flujo de un solo botón y se evita marcar salida al llegar.
- **Enrolamiento presencial** con la administradora, aprovechando la toma de la foto de
  referencia que hace falta igual para el padrón. La trabajadora **nunca ve una contraseña**
  y el dispositivo queda vinculado desde el primer segundo. Registrado en
  [[adr-003-autenticacion-jwt]].
- **No existe programación formal de turnos.** Consecuencia analizada en el ADR nuevo.

Se crea [[adr-004-llenado-del-cronograma]] (propuesta), con la medición sobre el Excel real:

- El sistema llena solo **2 955 de 3 616 celdas = 81,7 %**: las 2 514 horas reales, más
  `DESCANSO`/`LIBRE` (284) con el día de descanso semanal, `VACACIONES` (70) y `PERMISO`
  (45) cargados como rango, y `FERIADO` (42) con el calendario del Perú.
- **`NO TURNO` (589) e `INASISTENCIA` (72) no se pueden deducir**: exigen saber si le
  tocaba. Se dejan en un estado `SIN MARCACIÓN` que la administradora resuelve con una
  pregunta de dos opciones — **~26 por semana y sede**.
- **El sistema nunca elige entre las dos por su cuenta**: escribir `INASISTENCIA` sin
  saberlo es una acusación; escribir `NO TURNO` cuando sí faltó, un encubrimiento.
- **Coste real de no tener programación**: no se pueden detectar tardanzas ni salidas antes
  de tiempo. Queda la puerta abierta en el modelo de datos.

Se actualizan [[adr-002-canal-de-marcacion]] (flujo de 7 pasos con pantalla de
confirmación), [[adr-003-autenticacion-jwt]], [[sintesis]], [[decisiones-moc]] e [[index]].

## [2026-09-05] ingesta | S14 antipatrones + cuatro decisiones más

Cuatro respuestas del usuario:

- **Despliegue del backend: sin decidir por ahora.** Se convierte en restricción de diseño
  en [[adr-001-stack-y-arquitectura]]: no atarse a proveedor — almacenamiento de fotos tras
  interfaz propia, JPA + PostgreSQL, reconocedor por [[patron-adapter]].
- **Las cajeras marcan igual que las podólogas**: mismo flujo y mismo padrón, sólo cambia el
  rol. Queda abierto si comparten cronograma o se exportan aparte.
- **Contingencia**: si no puede marcar, la administradora la registra a mano, marcada como
  **registro manual y no como marcación verificada**, con rastro de auditoría. Se descarta
  marcar desde el celular de una compañera (choca con el vector 5 de [[antifraude]]).
- **S14 se ingiere antes de codificar.**

Ingesta de S14:

- Se crean [[fuente-s14-antipatrones]] y [[antipatrones]], con los **16 antipatrones
  evaluados contra el proyecto** — es el borrador directo del entregable
  `S14-GUIA-TALLER.xlsx`, que exige descripción, problema, solución alternativa, evidencia y
  puntaje por cada uno.
- **Convergencia inesperada**: la decisión de dejar el despliegue sin decidir *es* la defensa
  contra **Vendor Lock-In**, uno de los ocho antipatrones de la rúbrica. Y **Not Invented
  Here** y **Overengineering** respaldan los dos patrones que [[patrones-moc]] ya descartaba.
- **Guardia nueva que aporta S14**: *Magic Numbers*. El diseño ya acumula cinco números
  (radio 200 m, precisión 500 m, token 90 s, umbral facial, 2 marcaciones/día) que deben ser
  configuración con nombre, nunca literales.
- **Lectura defendible de los 8 organizacionales**: no describen al equipo de un proyecto de
  una persona, pero varios describen el **proceso manual actual** — el turno que vive en la
  memoria de alguien (Silo Mentality), los conteos congelados 22 semanas (Management by
  Objectives), las excepciones perdidas en WhatsApp (Ineffective Communication). Y *Blame
  Culture* refuerza que el sistema no escriba `INASISTENCIA` por su cuenta.
- **8 de los 16 ya tienen evidencia documental** antes de escribir la primera línea de código.

**Pendiente:** ADR-005 de la cadena de verificaciones antifraude y el modelo de datos.

## [2026-09-05] decision | Interfaz de marcación (ADR-005) y prototipo navegable

- **Cajeras y podólogas van en el mismo cronograma** *(decisión del usuario)*. Dotación
  registrada en [[sedes]]: 2–3 cajeras y de 3 a 15 podólogas por sede, entre *full time* y
  *part time*. Consecuencia: la rejilla es muy desigual entre sedes (de 5 a 18 filas), y el
  exportador no puede asumir un alto fijo.
- **La administradora lleva login obligatorio y segundo factor** *(decisión del usuario)*.
  Registrado en [[adr-003-autenticacion-jwt]] como una asimetría intencionada: la
  trabajadora nunca ve un login; la administradora se autentica siempre, porque ve datos de
  personal de toda la empresa. Falta elegir el método de segundo factor.
- El usuario plantea eliminar las cuentas: entrar por enlace, elegir sede, **escribir el
  nombre** y una nota, y enviar. Se crea [[adr-005-interfaz-de-marcacion]] para responderlo:
  - Su razonamiento —«registrarse complicaría todo»— **es correcto**, pero lleva a la
    conclusión contraria: con enrolamiento presencial la trabajadora **nunca se registra**.
  - Comparación de lo que ocurre cada día: **~15 interacciones con teclado** en el flujo sin
    cuentas frente a **3 toques sin teclado** con sesión abierta. La sesión no es una carga:
    es lo que elimina el trabajo.
  - Escribir el nombre se rechaza por dos motivos independientes: reintroduce el problema de
    las **dos LAURA** y las `MARY P.` ([[formato-cronograma-actual]]), y rompe el vector 4 de
    [[antifraude]] — cualquiera con el enlace marcaría por otra.
  - Lo que el usuario intuía como «formulario» se conserva, pero invertido: la pantalla de
    confirmación **muestra lo que el sistema ya sabe** y ella confirma, en vez de pedirle
    datos. La nota opcional se mantiene, plegada.
- Se define la regla para el rostro no reconocido: **nunca decirle «no eres tú»**. El
  reconocimiento falla por luz o ángulo; trasladar ese error a la persona es inaceptable.
  Se envía igual y se resuelve en la cola.
- **Prototipo navegable publicado**: https://claude.ai/code/artifact/8ed44102-37c0-4620-8019-d4b56f991b78
  Cuatro pantallas con estado real (entrada → salida → jornada completa), modo a pantalla
  completa para enseñárselo a una podóloga desde un celular.
- **Pendiente:** probarlo con una podóloga real antes de desplegar; elegir método de segundo
  factor; ADR-006 de la cadena de verificaciones antifraude.

## [2026-09-05] ingesta | PC-2 y PC-3 — los entregables reales, y el enlace personalizado

Al revisar qué archivos eran prescindibles apareció lo contrario: **los dos PDF que estaban
catalogados como «prioridad baja» son los más importantes de la carpeta**. Se añade
`tools/extraer_pdf.py` (extractor de texto de PDF sin dependencias) y se crea
[[fuente-pc2-pc3-entregables]].

Seis hallazgos que reordenan el proyecto:

- **La arquitectura debe ser de cuatro capas** — Vista, Control, Modelo y Base de datos —
  con patrones asignados por capa. PC-3 lo repite en tres criterios de calificación.
- **Contradicción con [[patron-singleton]]**: la rúbrica exige Singleton y Prototype en la
  capa modelo; la wiki lo había descartado con buen argumento técnico. Registrada como
  contradicción abierta en la página del patrón y en [[patrones-moc]]; `uso_proyecto` pasa de
  `no` a `candidato`. **Decisión pendiente del usuario.**
- Se exigen **stored procedures**, que JPA no usa por defecto.
- El proyecto debe estar ligado a un **ODS**, y el capítulo 1 del informe debe describirlo.
  Nunca se había mencionado. Candidatos: ODS 8 y, secundariamente, ODS 5.
- **Es un trabajo en grupo de tres** (`Apellidos1 - 2 - 3`). Toda la wiki asumía un solo
  desarrollador.
- Se pide el principio **ISP** de la Unidad I, con capturas de código.

También se descubre que **S16 y S17 son patrones GRASP**, con su propia rúbrica de
evaluación sobre el proyecto (`S16-GUIA-TALLER.xlsx`). Estaban catalogados como
«fundamentos, prioridad media»; suben a prioridad alta.

Duplicados confirmados por md5: las tres copias de PC-2 son idénticas, y
`Diseño-Poster.pdf` es copia byte a byte de `Cartel-Indicaciones.pdf`.

**Enlace personalizado por trabajadora** *(propuesta del usuario)*: aceptada en
[[adr-003-autenticacion-jwt]] con un matiz — **la URL no puede ser la credencial**, porque
los enlaces se reenvían por WhatsApp y quedan en capturas e historial. Se resuelve con un
enlace de activación de un solo uso que se canjea por una sesión ligada al dispositivo y se
quema; el acceso directo permanente identifica pero no autentica.

**Pendiente:** decidir qué hacer con Singleton; elegir el ODS; ingerir GRASP (S16–S17) y el
ISP de S01–S04; ADR de arquitectura de cuatro capas **antes** de escribir código.

## [2026-09-05] decision | Enlace global (ADR-006) y jornadas múltiples

- **El usuario descarta el enlace personalizado y pide uno global**, por una restricción del
  negocio que ningún ADR había considerado: **hay trabajadoras sin celular propio**. Eso
  invalida por sí solo la sesión ligada al dispositivo.
- Se crea [[adr-006-acceso-sin-sesion]]. El principio que lo ordena: **el nombre es una
  afirmación que acelera la comparación; el rostro es la prueba**. El enlace global no empeora
  el proceso actual —donde el nombre también se escribe a mano— sino que lo iguala y le añade
  hora sellada por el servidor y ubicación verificada.
- **Corrección a la propuesta del usuario**: el nombre **se elige del padrón, no se escribe**.
  Tras la foto aparece «¿Quién eres?» con las caras de la sede y ella toca la suya. Evita el
  teclado, elimina las **dos LAURA** y las `MARY P.`, y enlaza con su rostro de referencia.
  La **función** no se pregunta: si el nombre viene del padrón, el rol ya se conoce; se
  muestra como comprobación.
- **Consecuencias asumidas**: el rostro pasa de defensa secundaria a **eje del sistema**; el
  dispositivo baja de garantía a señal; el vector 4 de [[antifraude]] («que una compañera
  marque por ella») se convierte en el riesgo central. Queda dicho sin adornos: la identidad
  deja de estar garantizada y pasa a estar **verificada con margen de error**.
- Propuesta añadida sin decidir: **un celular o tableta registrada por sede**, más controlable
  que un teléfono personal prestado.
- [[adr-003-autenticacion-jwt]] queda vigente **sólo para la administradora**.
- Se crea [[jornadas-multiples]]: hasta **6 marcaciones diarias** por rotación entre sedes.
  Rompe dos cosas — la inferencia de entrada/salida por orden (se sustituye por «¿tiene
  jornada abierta?») y el formato del cronograma, que tiene **una celda por trabajadora y
  día** y no puede expresar dos sedes.
- El límite de 6 se incorpora como **verificación antifraude**, junto con un intervalo mínimo
  entre marcaciones que además evita el doble envío accidental.
- **Prototipo actualizado** al flujo nuevo.
- **Pendiente:** celular por sede; regla de conteo MAÑANA/TARDE con dos sedes; y las
  decisiones abiertas de PC-3 (Singleton, ODS, cuatro capas).
