# CLAUDE.md — Esquema del wiki

Este archivo es el **contrato de trabajo**. Léelo entero al inicio de cada sesión antes de
tocar cualquier archivo del wiki.

## 1. Para qué existe este repositorio

Tiene dos objetivos encadenados:

1. **Construir un wiki** (`wiki/`) que destile el material del curso de Diseño de Patrones
   y los requisitos del cliente en páginas interconectadas y siempre vigentes.
2. **Usar ese wiki para construir el software**: una aplicación web en **Spring Boot** de
   registro de asistencia para una cadena de podología con 7 sedes en Lima.

El wiki no es documentación del código: es lo que **precede** al código. Cada patrón que se
implemente debe primero tener su página, su justificación y su enlace a un requisito real.
Si un patrón no resuelve un problema concreto del dominio, no entra al software.

## 2. Las tres capas

| Capa | Ubicación | Quién escribe | Regla |
|---|---|---|---|
| Fuentes crudas | `Archivos_de_clase/`, `docs/` | El usuario | **Inmutables.** Nunca las edites, muevas ni renombres. |
| Wiki | `wiki/` | Claude | Claude tiene propiedad total. El usuario lee y dirige. |
| Esquema | `CLAUDE.md` (este archivo) | Ambos | Se co-evoluciona cuando una convención deja de servir. |
| Herramientas | `tools/` | Ambos | Scripts de apoyo. Ver §7. |

Las fuentes nuevas se depositan en `Archivos_de_clase/` (material de curso) o en `docs/`
(material del cliente: Excel, fotos, capturas). No se crea otra carpeta de fuentes.

## 3. Estructura del wiki

```
wiki/
├── index.md            Catálogo de todo el wiki. Orientado a CONTENIDO.
├── log.md              Bitácora append-only. Orientada al TIEMPO.
├── overview.md         Página raíz: el proyecto en una plana.
├── fuentes/            Una página por fuente ingerida. Espejo de Archivos_de_clase/ y docs/.
├── patrones/           Una página por patrón de diseño (singleton, factory, observer...).
├── dominio/            Entidades del problema real (sede, podóloga, marcación, evidencia...).
├── arquitectura/       Cómo se construye: capas, stack, modelo de datos, endpoints.
├── decisiones/         Decisiones tomadas y por qué. Formato ADR ligero.
└── sintesis/           Páginas transversales: mapas patrón→requisito, tesis en evolución.
```

Nombres de archivo en **kebab-case y sin tildes** (`marcacion-multiple.md`, no
`Marcación Múltiple.md`). El título con tildes va dentro, en el frontmatter y el `#` H1.

## 4. Formato de página

Toda página del wiki empieza con frontmatter YAML:

```yaml
---
titulo: Patrón Singleton
tipo: patron          # fuente | patron | dominio | arquitectura | decision | sintesis
estado: borrador      # borrador | estable | obsoleto
fuentes:              # rutas relativas a las fuentes crudas que respaldan la página
  - Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx
actualizado: 2026-09-05
tags: [creacional, gof]
---
```

Reglas de cuerpo:

- **Enlaza con generosidad.** `[[nombre-archivo]]` sin extensión. Un enlace a una página que
  todavía no existe es válido y deseable: marca trabajo pendiente, no es un error.
- **Cita siempre la fuente.** Toda afirmación no trivial lleva su origen entre paréntesis:
  `(S06 pptx, diapositiva 12)` o `(brief del usuario, 2026-09-05)`.
- **Separa lo comprobado de lo inferido.** Lo que deduces va marcado explícitamente:
  `> **Inferencia:** ...`. Lo que no sabes va como `> **Hueco:** ...`.
- **Registra las contradicciones, no las resuelvas en silencio.** Si una fuente nueva choca
  con una página existente, añade una sección `## Tensiones` con ambas versiones y su fuente.
- **Español.** Todo el wiki se escribe en español, como el material de origen.
- Páginas cortas y muchas, antes que pocas y largas. Si una página pasa de ~200 líneas,
  parte los subtemas en páginas propias y enlázalas.

## 5. Operaciones

### Ingesta

Cuando el usuario señale una fuente nueva (o una ya presente sin ingerir):

1. Extrae su texto (§7). Léelo completo antes de escribir nada.
2. Comenta con el usuario los 3–5 puntos clave. **Espera su reacción** antes de escribir si
   la fuente es densa o ambigua; si es rutinaria, procede y reporta.
3. Crea `wiki/fuentes/<slug>.md`: qué es, qué aporta, qué patrones toca y en qué se conecta
   con el proyecto. Enlaza a las páginas afectadas.
4. **Propaga.** Actualiza las páginas de `patrones/`, `dominio/` y `sintesis/` que esa
   fuente refuerza, matiza o contradice. Una fuente buena toca varias páginas: si solo
   tocaste una, probablemente no propagaste lo suficiente.
5. Actualiza `index.md` y añade una entrada a `log.md`.

Ingiere **una fuente a la vez** salvo que el usuario pida lote explícitamente.

### Consulta

1. Lee `index.md` primero, luego baja a las páginas relevantes.
2. Responde citando páginas del wiki (`[[pagina]]`) y fuentes crudas.
3. **Si la respuesta tiene valor duradero, archívala como página nueva** (normalmente en
   `sintesis/` o `decisiones/`) en lugar de dejarla morir en el chat. Pregunta al usuario si
   no está claro que valga la pena.

### Lint

Cuando el usuario lo pida, revisa el wiki y reporta:

- Contradicciones entre páginas y afirmaciones que una fuente nueva volvió obsoletas.
- Páginas huérfanas (sin enlaces entrantes) y enlaces `[[...]]` a páginas inexistentes que
  ya merecen escribirse.
- Fuentes en `Archivos_de_clase/` o `docs/` que aún no tienen página en `fuentes/`.
- Patrones documentados que no están enlazados a ningún requisito real (candidatos a
  descartar) y requisitos sin patrón asignado (huecos de diseño).
- Frontmatter incompleto o `actualizado` desfasado respecto al contenido.

Reporta primero, corrige después de que el usuario decida.

## 6. index.md y log.md

- **`index.md`** es el catálogo por contenido: cada página con su enlace y una línea de
  resumen, agrupada por carpeta. Se actualiza en **toda** ingesta y con **toda** página
  nueva. Es lo primero que se lee al responder una consulta.
- **`log.md`** es cronológico y **append-only**: nunca se reescriben entradas pasadas. Cada
  entrada abre con el prefijo exacto:

  ```
  ## [AAAA-MM-DD] <operacion> | <titulo>
  ```

  donde `<operacion>` es `ingesta`, `consulta`, `lint`, `decision` o `scaffold`. El prefijo
  fijo permite `grep "^## \[" wiki/log.md | tail -5` para ver lo último que pasó.

## 7. Herramientas y limitaciones del entorno

Estado verificado el 2026-09-05 en esta máquina, **después** de que el usuario instalara el
toolchain:

| Herramienta | Estado | Ruta / versión |
|---|---|---|
| Python | ✅ 3.14.7 | `C:\Python314\python.exe` |
| Java / javac | ✅ 26.0.2.1 | `C:\Program Files\Common Files\Oracle\Java\javapath` |
| Node / npm | ✅ 24.20.0 / 11.19.0 | `C:\Program Files\nodejs` |
| Maven | ✅ 3.9.16 | `%LOCALAPPDATA%\Programs\apache-maven-3.9.16\bin` |
| `JAVA_HOME` | ✅ definido (ámbito **máquina**) | `C:\Program Files\Java\jdk-26.0.2.1` |
| Chocolatey | ✅ | `C:\ProgramData\chocolatey\bin` — se usó para instalar Python |
| `unzip` | ✅ | `/usr/bin/unzip` |

**El toolchain está completo.** No falta nada para generar, compilar y correr el proyecto.

**El PATH del shell de esta sesión está desactualizado**: se capturó antes de la
instalación, así que `python`, `java` y `node` "no se encuentran" aunque estén instalados.
Exporta el PATH en cada comando hasta reiniciar la sesión:

```bash
export PATH="/c/Python314:/c/Program Files/Common Files/Oracle/Java/javapath:/c/Program Files/nodejs:$PATH"
```

**El PATH de usuario es frágil en esta máquina.** Verificado el 2026-09-05: entre dos
sesiones, la instalación de Chocolatey y Python reescribió el PATH de usuario y **borró la
entrada de Maven**, dejando `mvn` irresoluble aunque los binarios seguían intactos en disco.
La entrada muerta `C:\Program Files\Java\jdk-26\bin` desapareció en el mismo cambio y
`JAVA_HOME` pasó de ámbito usuario a ámbito máquina.

Moraleja: **antes de dar por bueno el entorno, comprueba que `mvn`, `java` y `python` se
resuelvan de verdad**, no que estén instalados. Comando de diagnóstico:

```powershell
$env:Path = [Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')
foreach ($t in @('java','javac','mvn','python','node')) { Get-Command $t -ErrorAction SilentlyContinue | Select-Object Name, Source }
```

Si falta Maven, se reañade con:

```powershell
$b = "$env:LOCALAPPDATA\Programs\apache-maven-3.9.16\bin"
[Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path','User').TrimEnd(';') + ";$b", 'User')
```

- Los tres scripts de `tools/` (`extraer.py`, `extraer_pdf.py`, `pluscode.py`) **ya
  funcionan**. `extraer_pdf.py` desbloqueó los PDF del curso.
- `extraer_pdf.py` **pierde tildes y ligaturas** en algunos PDF (`presentacin` por
  `presentación`, `cient!co` por `científico`). El texto sigue siendo utilizable, pero no
  cites literalmente de esos archivos sin corregir; otros PDF salen con acentos intactos.
- **Sí hay `unzip`.** Los formatos Office son ZIP con XML dentro, así que la extracción de
  `.docx`, `.pptx` y `.xlsx` se puede hacer también sin Python:

  ```bash
  # docx
  unzip -p "ruta.docx" word/document.xml | sed 's|</w:p>|\n|g; s|<[^>]*>||g'
  # pptx (una diapositiva)
  unzip -p "ruta.pptx" ppt/slides/slide1.xml | sed 's|</a:p>|\n|g; s|<[^>]*>||g'
  # xlsx (textos)
  unzip -p "ruta.xlsx" xl/sharedStrings.xml | sed 's|</si>|\n|g; s|<[^>]*>||g'
  ```

- **Los PDF se leen con `python tools/extraer_pdf.py <ruta>`.** La herramienta Read nativa
  sigue sin funcionar con PDF (falta `poppler-utils`), así que usa siempre el script.
  Única excepción: `CASO DE EJEMPLO 2.pdf` rinde 510 caracteres — es un PDF de imágenes y
  necesitaría OCR. Ver [[huecos-abiertos]].
- **Los heredocs de Bash fallan en este entorno** (finales de línea CRLF rompen el
  terminador). Para escribir páginas del wiki usa la herramienta Write, no `cat <<EOF`.

**Compatibilidad con Spring Boot** (consultado a `start.spring.io/metadata/client` el
2026-09-05): la versión por defecto es **Spring Boot 4.1.1** y las versiones de Java
ofrecidas son **26, 25, 21 y 17**. El JDK 26 instalado está soportado, así que **no hace
falta un segundo JDK**.

## 8. Contexto del dominio (resumen operativo)

Detalle completo en [[requisitos]] y [[sedes]]. Lo mínimo para trabajar:

- **7 sedes** en Lima: Los Olivos, La Molina, San Borja, Lince, San Miguel, Surco,
  Miraflores.
- **Usuarias**: podólogas, mayores de 30 años, con poca familiaridad tecnológica. La
  interfaz manda sobre la elegancia técnica: **simple, grande y en pocos pasos**.
- **Flujo de marcación**: nombre → sede (desplegable) → entrada o salida → foto con fecha y
  hora impresas → notas (opcional) → enviar.
- **Multi-marcación**: una misma trabajadora marca varias veces al día en sedes distintas.
  Ninguna regla puede asumir una entrada y una salida por día.
- **Salidas**: dashboard de administradora con una pestaña por sede, y exportación a Excel
  con el formato de `docs/Cronograma_Ejemplo.xlsx` (ver [[formato-cronograma-excel]]).

## 9. Reglas duras

1. **No inventes contenido de fuentes que no leíste.** Si un PDF está bloqueado, la página
   lo dice; no se rellena con lo que "seguramente dice".
2. **No edites `Archivos_de_clase/` ni `docs/`.**
3. **Toda página nueva entra en `index.md`** en el mismo paso en que se crea.
4. **Toda operación deja rastro en `log.md`.**
5. **Un patrón sin requisito que lo justifique no se implementa.** El curso pide patrones;
   el cliente pide una solución. La página de cada patrón debe decir a qué requisito sirve o
   declararse como "solo estudio, fuera del software".
