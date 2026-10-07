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

**Reverificado el 2026-10-06** al retomar el proyecto **en otro ordenador**. El trabajo del
2026-09-05 al 2026-09-20 se hizo en una máquina distinta; la tabla de esa máquina quedó
desfasada en cinco puntos respecto a esta. Esta es la buena:

| Herramienta | Estado | Ruta / versión |
|---|---|---|
| Java / javac | ✅ **26.0.2.1** | `C:\Program Files\Java\jdk-26.0.2.1` (resuelve vía `C:\Program Files\Common Files\Oracle\Java\javapath`) |
| `JAVA_HOME` | ✅ ámbito **máquina** | `C:\Program Files\Java\jdk-26.0.2.1` |
| Maven Wrapper | ✅ **la única forma de compilar** | `MVC/mvnw.cmd` — ya descargó su Maven 3.9.16 en `~/.m2/wrapper/dists` |
| Maven suelto | ⚠️ instalado y en el PATH, **no se usa** | `%LOCALAPPDATA%\Programs\apache-maven-3.9.16` |
| Python | ✅ **3.14.7** | `C:\Python314\python.exe` |
| Node | ✅ | `C:\Program Files\nodejs` |
| **MySQL** | ✅ **8.0.46, servicio `MySQL80` automático** (instalado el 2026-10-06) | `C:\Program Files\MySQL\MySQL Server 8.0\bin` — el cliente **no** está en el PATH |
| Chocolatey / winget | ✅ | `C:\ProgramData\chocolatey`, `WindowsApps\winget.exe` |
| Docker | ❌ no instalado | No hace falta |
| `unzip` | ✅ | `/usr/bin/unzip` |

> **Que esta máquina tenga `mvn` en el PATH no cambia la regla.** El proyecto se compila
> **siempre** con el wrapper: `.\mvnw.cmd` en Windows y `./mvnw` en Linux, nunca `mvn`. En la
> máquina anterior Maven desapareció del disco entre dos sesiones; el wrapper viaja dentro de
> `MVC/` y descarga su propio Maven, así que compila igual en las dos máquinas del equipo y en
> el servidor ARM de Oracle **sin depender de lo que haya instalado**.

> **`contextLoads()` NO comprueba MySQL todavía.** Verificado el 2026-10-06: pasa **sin MySQL
> instalado**. Spring Boot crea el pool de Hikari de forma perezosa —no abre ninguna conexión
> hasta que alguien pide la primera— y mientras la capa `bd` esté vacía nadie la pide. Las 22
> pruebas en verde **no decían nada de la base de datos**. Sigue siendo perezoso con la capa
> `bd` escrita (los `SimpleJdbcCall` leen el catálogo en su primer uso, no al arrancar). **La
> verificación contra MySQL la dan las pruebas `@JdbcTest` de `src/test/.../bd/`**: si fallan
> esas y no las de `modelo/`, el problema es la base, no el código.

En esta máquina **el PATH del shell está al día**: `java`, `javac`, `python`, `node` y `unzip`
se resuelven tanto desde PowerShell como desde Bash sin exportar nada.

**El PATH es frágil, y por eso se comprueba en vez de suponerlo.** En la máquina anterior,
entre dos sesiones, la instalación de Chocolatey y Python reescribió el PATH de usuario y
borró la entrada de Maven; más tarde Maven desapareció del disco entero. Moraleja: **antes de
dar por bueno el entorno, comprueba que las herramientas se resuelvan de verdad**, no que
estén instaladas. Comando de diagnóstico:

```powershell
$env:Path = [Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')
foreach ($t in @('java','javac','mysql','python','node')) { $c = Get-Command $t -ErrorAction SilentlyContinue; if ($c) { "$t -> $($c.Source)" } else { "$t -> NO SE RESUELVE" } }
Get-Service | Where-Object { $_.Name -match 'mysql' } | Select-Object Name, Status
```

El cliente `mysql` **no suele quedar en el PATH** aunque MySQL esté instalado: buscarlo en
`C:\Program Files\MySQL\MySQL Server 8.0\bin`. Si tampoco está ahí, comprobar el servicio
antes de concluir nada.

> **Trampa de PowerShell 5.1 al cargar los `.sql`, comprobada el 2026-10-06.** En Windows
> PowerShell 5.1, `$OutputEncoding` vale `us-ascii`: pasar un script con
> `Get-Content … | mysql` convierte **en silencio** cada tilde y cada eñe en `?`. Se carga con
> `cmd`, que entrega el archivo byte a byte:
>
> ```powershell
> $env:Path += ';C:\Program Files\MySQL\MySQL Server 8.0\bin'
> cmd /c "mysql -u root -p --default-character-set=utf8mb4 asistencia_loayza < SQL\02_procedimientos.sql"
> ```
>
> `01_esquema.sql` va sin el nombre de la base (la crea él). Recargar `02` no toca datos;
> recargar `03` **borra y vuelve a sembrar** todo.

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

**Spring Boot** (reconsultado el 2026-09-20): la versión por defecto sigue siendo
**4.1.1**, pero las versiones de Java ofrecidas cambiaron a **27, 25, 21 y 17** — el 26 ya
no está. El proyecto compila con **Java 25**, que es LTS, lo soporta Boot 4.1.1 y el JDK 26
instalado compila hacia él sin problema (`javac ... release 25`).

> **Trampa de Initializr, comprobada el 2026-09-20.** El generador escribe en el `pom.xml`
> `<version>4.1.1.RELEASE</version>`, y **ese artefacto no existe en Maven Central**: Spring
> abandonó el sufijo `.RELEASE` hace años. La versión publicada de verdad es `4.1.1`, sin
> sufijo. Si el build falla con *«Non-resolvable parent POM»*, es esto.

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
