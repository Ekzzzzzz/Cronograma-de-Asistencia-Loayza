---
tipo: fuente
titulo: PC-2 y PC-3 — los entregables reales del curso
archivo: "Archivos_de_clase/S10_s1s2 - PC-2-DPA.pdf, S15_s1s2 - PC-3-DPA.pdf"
sesion: 10
tags: [fuente, entregable, rubrica]
creado: 2026-09-05
actualizado: 2026-09-05
estado: activo
---

# PC-2 y PC-3 — los entregables reales del curso

> [!important] Estos son los dos archivos más importantes de `Archivos_de_clase/`
> Definen **qué hay que entregar y cómo se califica**. Hasta ahora estaban catalogados como
> «casos y evaluaciones, prioridad baja» en [[index]] — era un error de clasificación por no
> haberlos abierto.

Extraídos con `tools/extraer_pdf.py`. Ambos comparten estructura; PC-3 es la versión final y
acumulativa.

## Qué se entrega

| | **PC-2** (semana 10) | **PC-3** (semana 15) |
|---|---|---|
| Alcance | Patrones estructurales y creacionales | + patrones de interacción y responsabilidades |
| Informe | Formato del anexo 1 | Formato del anexo 1, **APA 7** |
| Demo | Vídeo mp4, **1 funcionalidad por módulo** | Vídeo, **con el estudiante exponiendo** |
| Entrevista | **Presencial e individual** | **Presencial e individual** |

Reparto de puntos en PC-3: informe 2 · arquitectura 2 · uso de patrones **6** · demo en
vídeo **5** · entrevista presencial **5**.

## Hallazgos que cambian el proyecto

### 1. La arquitectura debe ser de **cuatro capas**

PC-3 lo repite en tres criterios distintos: *«Arquitectura de 4 capas (informe)»*, *«Uso de
patrones de diseño a 4 capas»*, *«Demo del software a 4 capas»*. No es una sugerencia: es
criterio de calificación.

Las capas y sus patrones esperados, según el anexo:

| Capa | Patrones que el curso espera ver |
|---|---|
| **Vista** | Proxy, Bridge, Observer, Command |
| **Control** | Facade, Factory |
| **Modelo** | **Singleton y Prototype** |
| **Base de datos** | *scripts* y **stored procedures** |

### 2. Hay una contradicción con nuestro diseño

> [!warning] Contradicción: Singleton
> **La rúbrica** exige Singleton y Prototype en la capa modelo.
> **[[patron-singleton]]** lo descarta con el argumento de que Spring ya provee alcance
> *singleton* y que implementarlo a mano añade estado global e impide probar — argumento
> que el propio curso respalda ([[fuente-s06-singleton-prototype]], diapositiva 14) y que
> [[antipatrones]] refuerza vía *Not Invented Here*.
>
> Ambas posturas están bien fundadas y **no se resuelven solas**. Es una decisión del
> usuario, no del agente. Ver las opciones abajo.

Opciones *(propuesta del agente)*:

1. **Aplicarlo donde sí aporta** y documentarlo: un `ConfiguracionAntifraude` como singleton
   real (constructor privado y `getInstance()`) que cargue umbrales una sola vez. Cumple la
   rúbrica con un caso defendible en vez de forzado.
2. **Aplicarlo y argumentar el matiz** en el informe: mostrar la implementación clásica y
   explicar en qué se diferencia del alcance que da Spring. Demuestra más criterio.
3. **No aplicarlo** y sostener el descarte. Arriesga puntos en el criterio de mayor peso.

La primera parece la más sensata: cumple sin mentir.

### 3. Se exigen *stored procedures*

La capa de base de datos pide **scripts y stored procedures** con capturas por caso de
prueba. [[adr-001-stack-y-arquitectura]] asume JPA sobre PostgreSQL, que no los usa por
defecto. Habrá que incluir algunos deliberadamente.

### 4. El proyecto debe estar ligado a un **ODS**

*«Caso de estudio: proyecto designado por el grupo de trabajo relacionado a ODS»*, y el
capítulo 1 del informe debe **describir el ODS, el problema, la solución y el objetivo**.

No se había mencionado nunca en esta wiki. Candidatos naturales para un sistema de control
de asistencia justo y auditable *(propuesta del agente, a confirmar con el usuario)*:
**ODS 8 — Trabajo decente y crecimiento económico**, y de forma secundaria **ODS 5 —
Igualdad de género**, dado que la plantilla es íntegramente femenina.

### 5. Es un trabajo **en grupo**

El nombre del archivo del informe es `Apellidos1 - 2 - 3 - PC 3 - informe.pdf`: tres
apellidos. Toda la wiki asume hasta ahora un solo desarrollador — incluida la lectura de los
antipatrones organizacionales en [[antipatrones]], que habría que revisar si hay equipo.

### 6. Se pide el principio **ISP** de la Unidad I

*«Desarrollo de principios ISP (capturas de código)»*. Es el principio de segregación de
interfaces, de SOLID. Está en las sesiones S01–S04, **sin ingerir**.

## Estructura exacta del informe (anexo 1)

- Carátula · Índice de tablas
- **Capítulo 1 — Introducción** (citas APA 7): describir ODS, problema, solución, objetivo.
  Antecedentes internacionales y nacionales, de artículos o tesis con solución de
  arquitectura y patrones similares.
- **Capítulo 2 — Desarrollo**
  - Unidad I: principios **ISP** con capturas de código
  - Unidades II, III y IV: diseño de la arquitectura a cuatro capas con los patrones usados
  - Patrones creacionales (Singleton, Prototype, Factory…)
  - Patrones estructurales (Facade, Decorator, Composite…)
  - Vista, control, modelo y base de datos, con **capturas por caso de prueba**
  - Evaluación de antipatrones ([[antipatrones]] ya es su borrador)
- **Capítulo 3 — Conclusiones**: lecciones aprendidas por unidad y por estudiante
- Referencias en **APA 7**

## Qué aporta al proyecto

Reordena las prioridades. La wiki venía diseñando el sistema correcto; faltaba saber que el
curso exige además una forma concreta de presentarlo — cuatro capas, patrones asignados por
capa, *stored procedures* y un ODS.

Conviene **redactar el ADR de arquitectura de cuatro capas antes de escribir código**, para
que el mapeo entre nuestra tubería y las capas Vista/Control/Modelo/BD no sea una
justificación a posteriori.

## Enlaces

[[patron-singleton]] · [[antipatrones]] · [[adr-001-stack-y-arquitectura]] · [[fuentes-moc]]
