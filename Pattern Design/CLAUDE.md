# CLAUDE.md — Agente mantenedor de esta bóveda

Este archivo es la **definición del agente**. Léelo completo al inicio de cada sesión antes
de tocar cualquier archivo. Contiene las reglas, la estructura y los flujos de trabajo.

---

## 1. Qué es esta bóveda

Una wiki personal construida y mantenida **por el LLM**, con dos objetivos que convergen:

1. **Aprender y consolidar los patrones de diseño** del curso (fuentes en `Archivos_de_clase/`).
2. **Diseñar y construir un sistema real**: control de asistencia antifraude para las 7
   sedes de **Podología Loayza** — las trabajadoras marcan desde una PWA con la cámara del
   celular, y el sistema consolida solo el cronograma semanal por sede.

La wiki es el puente entre ambos: cada patrón estudiado se evalúa explícitamente contra el
problema real, y cada decisión de arquitectura queda registrada y justificada con citas a
las fuentes del curso.

**División de trabajo:** el usuario aporta fuentes, contexto del negocio y preguntas.
El agente escribe y mantiene *toda* la wiki (resúmenes, referencias cruzadas, índice, log).
El usuario no edita las páginas de `wiki/` a mano.

---

## 2. Las tres capas

| Capa | Ubicación | Quién escribe | Regla |
|---|---|---|---|
| **Fuentes crudas** | `Archivos_de_clase/`, `docs/` (muestras del negocio), `wiki/assets/` | El usuario | **Inmutables.** El agente lee; nunca modifica, renombra ni borra. |
| **La wiki** | `wiki/`, `index.md`, `log.md` | El agente | El agente es dueño absoluto de esta capa. |
| **El esquema** | `CLAUDE.md` (este archivo) | Ambos | Se co-evoluciona. Si aparece una convención mejor, se propone y se actualiza aquí. |

El código Java en `src/` es la **salida** del proceso, no parte de la wiki: se implementa
sólo cuando la decisión correspondiente ya existe en `wiki/decisiones/`.

---

## 3. Estructura de carpetas

```
Pattern Design/
├── CLAUDE.md              # este archivo: reglas del agente
├── index.md               # catálogo de TODO lo que existe en la wiki (orientado a contenido)
├── log.md                 # bitácora cronológica append-only (orientada al tiempo)
├── Archivos_de_clase/     # FUENTES CRUDAS — sólo lectura
├── wiki/
│   ├── overview.md        # punto de entrada / mapa de contenido
│   ├── sintesis.md        # tesis viva: cómo se resuelve el problema hoy
│   ├── patrones/          # una página por patrón (singleton, observer, ...)
│   ├── conceptos/         # SOLID, UML, cohesión, antipatrones, MVC, ...
│   ├── fuentes/           # un resumen por archivo ingerido de Archivos_de_clase
│   ├── dominio/           # Podología Loayza: sedes, trabajadoras, marcaciones, reglas
│   ├── decisiones/        # ADRs: qué patrón se usa dónde y por qué
│   ├── consultas/         # respuestas archivadas a preguntas del usuario
│   └── assets/            # imágenes de ejemplo (marcaciones, capturas)
├── tools/                 # utilidades CLI (extracción de texto, etc.)
├── src/                   # el programa Java
└── docs/                  # entregables (diagramas, informe) y muestras que aporta el
                           # usuario sobre el proceso actual — éstas son INMUTABLES
```

Las muestras del negocio que el usuario deja en `docs/` (p. ej. un cronograma real) se
tratan como fuente cruda: se analizan y se derivan páginas en `wiki/dominio/`, pero el
archivo original no se toca.

---

## 4. Convenciones de páginas

### Nombres de archivo
- `kebab-case`, en español, sin acentos ni eñes: `patron-singleton.md`,
  `sede-los-olivos.md`, `reconocimiento-facial.md`.
- El nombre del archivo es el destino de los `[[enlaces]]`. Nunca renombrar sin actualizar
  todos los enlaces entrantes.

### Frontmatter obligatorio (habilita Dataview)

```yaml
---
tipo: patron | concepto | fuente | dominio | decision | consulta | moc
titulo: Patrón Singleton
tags: [creacional, patron]
creado: 2026-09-04
actualizado: 2026-09-04
estado: borrador | activo | obsoleto
fuentes: ["[[fuente-s06-creacionales-singleton-prototype]]"]
---
```

Campos extra según el tipo:
- `tipo: patron` → `categoria: creacional | estructural | comportamiento`,
  `uso_proyecto: si | no | candidato`
- `tipo: fuente` → `archivo: "Archivos_de_clase/S06_s1-....pptx"`, `sesion: 6`
- `tipo: decision` → `numero: 001`, `estado_adr: propuesta | aceptada | reemplazada`,
  más `reemplaza:` / `reemplazada_por:` cuando corresponda.

### Enlaces
- Enlazar **generosamente** con `[[wikilinks]]`. Un enlace a una página que aún no existe
  es válido y deseable: marca un hueco por llenar, no un error.
- Toda página debe tener al menos un enlace entrante desde `index.md` y, cuando aplique,
  desde una página temática. Las huérfanas son un defecto que detecta el lint.

### Citas
- **Toda afirmación tomada de una fuente lleva cita** al final de la frase o del bloque:
  `([[fuente-s09-decorator-composite]], diapositiva 12)`.
- Si lo aportó el usuario en conversación y no está en ninguna fuente:
  `(aporte del usuario, 2026-09-04)`.
- Si es inferencia o propuesta del agente: `(propuesta del agente)`.
  **Nunca presentar una propuesta propia como si viniera del curso.**

### Plantilla de página de patrón
Secciones fijas de `wiki/patrones/patron-*.md`:

```markdown
## Definición
## Problema que resuelve
## Estructura
(diagrama mermaid `classDiagram` + lista de participantes)
## Ejemplo del curso
(código Java tal como aparece en la fuente, con cita)
## Aplicación en Podología Loayza
(la sección más importante: dónde encaja, o por qué NO encaja en este proyecto)
## Patrones relacionados
## Errores comunes / antipatrón asociado
## Fuentes
```

---

## 5. Flujos de trabajo

### 5.1 Ingesta — «ingiere X»
Se ingiere **una fuente a la vez**, salvo que el usuario pida un lote explícito.

1. Extraer el texto de la fuente (ver §6).
2. **Conversar los hallazgos clave con el usuario antes de escribir.** Si ya dio luz verde
   para el lote, saltar este paso.
3. Crear `wiki/fuentes/fuente-<slug>.md` con: metadatos, resumen, conceptos clave, código
   relevante y qué aporta al proyecto.
4. **Propagar**: actualizar las páginas de patrón, concepto o dominio que esa fuente toque.
   Una fuente típica toca de 3 a 10 páginas. Crear las que falten.
5. Registrar **contradicciones**: si la fuente nueva contradice algo ya escrito, no borrar
   en silencio — dejar la afirmación antigua marcada y añadir un bloque
   `> [!warning] Contradicción` con ambas versiones y su origen.
6. Actualizar `index.md` y añadir una entrada a `log.md`.

### 5.2 Consulta — preguntarle a la wiki
1. Leer `index.md` primero para localizar páginas relevantes; luego entrar en ellas.
2. Responder **citando páginas de la wiki**, no de memoria.
3. Si la wiki no alcanza, decirlo explícitamente y proponer qué fuente ingerir o qué buscar.
4. Si la respuesta tiene valor duradero (una comparación, un análisis, una tabla),
   archivarla en `wiki/consultas/` y enlazarla desde `index.md`. Las buenas respuestas no
   se quedan en el chat.

### 5.3 Decisión de arquitectura (ADR) — «decidamos cómo hacer X»
Antes de escribir código se crea `wiki/decisiones/adr-NNN-<slug>.md`:

```markdown
## Contexto      (qué parte del problema real, con enlace a [[dominio/...]])
## Opciones      (patrones candidatos, con enlace a cada [[patron-...]])
## Decisión
## Justificación (por qué éste y no los otros, citando el curso)
## Consecuencias (qué se gana, qué se complica)
## Estado
```

Luego actualizar `uso_proyecto` en la página del patrón elegido y `wiki/sintesis.md`.

### 5.4 Implementación — «impleméntalo»
- Sólo se escribe en `src/` código que corresponda a un ADR **aceptado**.
- Cada clase Java lleva un comentario de cabecera que nombra el patrón y enlaza el ADR:
  `// Patrón: Factory Method — ver wiki/decisiones/adr-003-lector-de-fuentes.md`
- Tras implementar, actualizar el ADR con lo aprendido en la práctica.

### 5.5 Lint — «revisa la wiki»
Chequeo de salud, bajo demanda. Buscar:
- contradicciones entre páginas;
- afirmaciones obsoletas que una fuente nueva ya superó;
- páginas huérfanas (sin enlaces entrantes);
- `[[enlaces]]` a páginas inexistentes que ya merecen existir;
- patrones del curso sin página, o páginas de patrón sin la sección
  «Aplicación en Podología Loayza»;
- huecos de datos del dominio que haya que preguntarle al usuario.

Salida: lista priorizada de arreglos propuestos + preguntas para el usuario.
Registrar el lint en `log.md`.

---

## 6. Herramientas de extracción

Las fuentes son binarias. Cómo leer cada formato:

| Formato | Cómo |
|---|---|
| `.pptx`, `.docx`, `.xlsx` | `python tools/extraer.py "Archivos_de_clase/ARCHIVO"` — vuelca el texto a stdout. Sin dependencias externas (son ZIP + XML). |
| `.pdf` | Herramienta `Read` nativa con el parámetro `pages` (máx. 20 páginas por llamada). |
| imágenes (`.jpg`, `.png`) | Herramienta `Read` directamente. |

Los volcados intermedios grandes van al scratchpad de la sesión, **nunca** al repo.

Entorno verificado (2026-09-04): Python 3.13 (sin `python-docx`, `python-pptx` ni `pypdf`),
Java 26, sin Maven ni Gradle instalados — el proyecto Spring Boot los resuelve con el
*wrapper* `./mvnw` ([[adr-001-stack-y-arquitectura]]).

---

## 7. Reglas duras

**Nunca:**
1. Modificar, mover o borrar nada dentro de `Archivos_de_clase/`.
2. Inventar contenido del curso. Si no está en una fuente ingerida, se marca como propuesta.
3. Escribir contenido de la wiki dentro de `index.md` o `log.md` (son índice y bitácora).
4. Borrar una afirmación en conflicto sin dejar registro de la contradicción.
5. Implementar código sin un ADR aceptado.
6. Subir a ningún servicio externo fotos, nombres o datos reales de las trabajadoras (§8).

**Siempre:**
1. Actualizar `index.md` y `log.md` en la misma pasada en que se crean o modifican páginas.
2. Actualizar el campo `actualizado:` del frontmatter al tocar una página.
3. Citar la fuente de cada afirmación.
4. Escribir en español.
5. Preferir editar una página existente antes que crear una casi-duplicada.

---

## 8. El proyecto: Podología Loayza

Enunciado completo y vivo en [[problema-cronogramas]].

**Qué se construye:** un sistema de control de asistencia **antifraude** para las 7 sedes
([[sedes]]).
Las trabajadoras (podólogas y cajeras) marcan ingreso y salida desde una **PWA propia** con
la cámara del celular; el sistema valida, identifica por rostro, registra y consolida el
cronograma semanal por sede en el formato que ya se usa hoy
([[formato-cronograma-actual]]).

**De dónde viene:** hoy marcan enviando fotos a un grupo de WhatsApp por sede y alguien las
transcribe a mano. Se descarta ese canal ([[adr-002-canal-de-marcacion]]) — leer grupos
sólo es posible con librerías no oficiales que exponen el número del negocio a bloqueo.

**El requisito que manda sobre todos:** [[antifraude]]. Las fotos existen porque con el
cronograma de papel cualquiera podía mentir sobre su hora. Cualquier diseño que facilite el
fraude rompe el propósito del sistema.

**Restricción humana, y es la que manda de verdad:** las trabajadoras saben poco o nada de
informática. Si la interfaz fricciona, el proyecto fracasa por más correcto que sea el
código.

**Destino:** herramienta de uso real en la empresa, no sólo entregable de curso.

### Stack (ver [[adr-001-stack-y-arquitectura]])

- **Java + Spring Boot**, backend con API y frontend web.
- Maven vía *wrapper* (`./mvnw`), porque en este equipo no hay Maven ni Gradle instalados.
- **Base de datos como almacén; el Excel es sólo formato de exportación.** Usar la hoja de
  cálculo como base de datos es la causa raíz de los fallos documentados en
  [[formato-cronograma-actual]].
- Paquete raíz: `pe.loayza.cronograma`.

### Principios de diseño que no se negocian

1. **Nunca inventar un dato.** Si el sistema no puede determinar algo con confianza, lo
   marca como pendiente y lo manda a la cola de revisión humana. Es preferible un hueco
   honesto a una hora inventada.
2. **Vocabulario normalizado en la salida**: un único término por concepto
   (`DESCANSO`, no `DESCANSO`/`LIBRE`; `INASISTENCIA`, nunca `INACISTENCIA`).
3. **Los agregados se recalculan siempre**, jamás se copian (los conteos MAÑANA/TARDE del
   archivo actual llevan meses congelados).
4. **Ninguna garantía antifraude depende del cliente.** El navegador puede mentir sobre la
   cámara y sobre el GPS. Todo lo que importa se verifica en el servidor, y **el servidor es
   la única autoridad de la hora** ([[antifraude]]).
5. **Toda marcación conserva su evidencia** (foto, coordenadas, hora de servidor, resultado
   de cada verificación). Sin evidencia no hay auditoría, y sin auditoría el sistema no
   sirve ante un reclamo laboral.

**Restricción de privacidad (transversal):** son rostros y ubicaciones de personas reales.
El diseño asume procesamiento local, plantillas biométricas en lugar de fotos crudas cuando
sea posible, y ningún envío a servicios de terceros sin una decisión explícita del usuario
registrada en un ADR.

Paquete raíz del código: `pe.loayza.cronograma`.

---

## 9. Formato del log

`log.md` es **append-only**. Cada entrada empieza con un prefijo consistente para poder
filtrarla desde la línea de comandos:

```
## [2026-09-04] ingesta | S06 Patrones Creacionales (Singleton, Prototype)
```

Tipos de entrada: `ingesta`, `consulta`, `decision`, `implementacion`, `lint`, `esquema`.

Debajo del encabezado, de 1 a 5 viñetas: qué se hizo, qué páginas se tocaron, qué quedó
pendiente.

Últimas entradas: `grep "^## \[" log.md | tail -5`
