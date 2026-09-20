---
titulo: Índice del wiki
tipo: sintesis
estado: borrador
actualizado: 2026-09-06
tags: [indice]
---

# Índice del wiki

Catálogo de todo el contenido. Se lee **primero** al responder cualquier consulta y se
actualiza con **toda** página nueva. La vista cronológica está en [[log]].

## Raíz

| Página | Resumen |
|---|---|
| [[overview]] | El proyecto en una plana: problema, solución y estado. Punto de entrada. |
| [[index]] | Esta página. |
| [[log]] | Bitácora append-only de ingestas, consultas y lints. |

## Dominio

| Página | Resumen |
|---|---|
| [[requisitos]] | RF-01…RF-13 y RNF-01…RNF-04 derivados del brief del usuario. |
| [[sedes]] | Las 7 sedes de Lima y sus Plus Codes. |
| [[formato-cronograma-excel]] | Estructura verificada del Excel que la exportación debe reproducir. |
| [[marcacion-multiple]] | La regla central: N turnos por día y sede. Introduce el nivel `Turno`. |

## Síntesis

| Página | Resumen |
|---|---|
| [[huecos-abiertos]] | Bloqueos del entorno, preguntas para el usuario y decisiones pendientes. |
| [[mapa-patron-requisito]] | Evaluación de encaje de los 15 patrones del curso: 6 de núcleo, 7 descartes, Adapter según alcance. |
| [[patrones-en-cristiano]] | Cada patrón explicado sin jerga: qué hace, dónde entra y qué pasaría sin él. Material para el informe y la entrevista. |

## Patrones

**Los siete que se implementan**, confirmados en [[adr-001-patrones-seleccionados]] y
revisados en [[adr-003-login-y-cronograma-simple]]. Cubren las tres familias que exige PC-3.
De los siete descartados, solo State tiene página: los otros seis viven en
[[mapa-patron-requisito]].

| Página | Resumen |
|---|---|
| [[patron-decorator]] | Sellado de la foto con fecha, hora y sede. El mejor encaje del proyecto. |
| [[patron-composite]] | Jerarquía del cronograma: marcación → jornada → día → semana de sede. |
| [[patron-factory]] | Creación de la marcación según sea entrada o salida. |
| [[patron-facade]] | `RegistrarMarcacion`: un solo punto de entrada para los cinco pasos del envío. |
| [[patron-proxy]] | Protection para el dashboard y Virtual para las fotos. |
| [[patron-singleton]] | Catálogo de las 7 sedes, con la letra chica de Spring y la Singletonitis. |
| [[patron-observer]] | Recalculo de la jornada y refresco del dashboard al registrar. Única familia de comportamiento. |
| [[patron-state]] | ⚠️ **Descartado el 2026-09-06.** Se conserva como evidencia para el capítulo de antipatrones. |

## Arquitectura

| Página | Resumen |
|---|---|
| [[cuatro-capas]] | Vista / Control / Modelo / BD, con el reparto de patrones que fija PC-3. |
| [[modelo-datos]] | Esquema MySQL 8: cinco tablas, índices y stored procedures. |
| [[estructura-repositorio]] | Las carpetas del entregable, los paquetes por capa y por qué se llaman así. |

## Decisiones

| Página | Resumen |
|---|---|
| [[decisiones]] | Índice de ADRs y lista de lo que sigue pendiente de decidir. |
| [[adr-001-patrones-seleccionados]] | Spring Boot, núcleo de 7 patrones, periferia de 5, 3 descartes. |
| [[adr-002-arquitectura-produccion]] | MySQL 8, fotos en disco, VPS, lista cerrada de trabajadoras y estrategia de rendimiento. |
| [[adr-003-login-y-cronograma-simple]] | Login de administrador, celda solo con horas, State descartado y Observer al núcleo. |
| [[adr-004-despliegue-azure]] | Una VM B1s con todo dentro, fotos a Blob Storage y el crédito de 100 USD dimensionado. |
| [[adr-005-hosting-largo-plazo]] | Azure para el curso, Oracle Always Free para producción. ODS 8 y equipo de dos. |
| [[adr-006-dashboard-y-turnos-multiples]] | Seis funciones del dashboard, hasta 6 pares por celda, cargos y correcciones trazables. |
| [[adr-007-seguridad-endpoint-publico]] | Cuatro barreras contra el abuso del formulario público, y jornada completa o media. |
| [[adr-008-alcance-interfaz-y-cola-sin-senal]] | Doce huecos cerrados de golpe, reconciliación con el prototipo, cola sin señal y la pila de la interfaz. |
| [[adr-009-estructura-y-dos-patrones-que-vuelven]] | Bridge y Command vuelven, la estructura del entregable y el plan a cuatro semanas. |
| [[adr-010-sin-rechazos-oracle-y-nombre-libre]] | El sistema deja de rechazar marcaciones, se puede escribir el nombre, y la plataforma pasa a ser Oracle. |

## Fuentes

| Página | Resumen |
|---|---|
| [[pc3-entregable]] | Qué exige el curso: rúbrica de 20 puntos, formato del informe y patrones obligatorios. |
| [[s06-singleton-prototype]] | Creacionales: Singleton (ejemplo `DBConnection`) y Prototype (`Cloneable`). |
| [[s07-factory-abstractfactory-builder]] | Creacionales: Factory (ejemplo `NotificacionFactory`), Abstract Factory y Builder. |
| [[s08-adapter-facade]] | Estructurales: Adapter (sistema legado) y Facade (caso hospital). |
| [[s09-decorator-composite]] | Estructurales: Decorator (`Transport`) y Composite (tratamientos de pacientes). |
| [[s11-proxy-bridge]] | Estructurales: Proxy con sus cuatro tipos, y Bridge. Sin código Java. |
| [[s12-state-observer]] | Comportamiento: State y Observer. Sin código Java. |
| [[s13-command-memento]] | Comportamiento: Command y Memento. Sin código Java. |
| [[s14-antipatrones]] | Ocho antipatrones de software y ocho organizacionales. Base del capítulo que exige PC-3. |

---

## Inventario de fuentes crudas

Estado: `pendiente` (legible, sin ingerir) · `bloqueado` (no legible en este entorno) ·
`ingerido` (tiene página en `wiki/fuentes/`).

### Material del cliente — `docs/`

| Fuente | Estado | Nota |
|---|---|---|
| `Cronograma_Ejemplo.xlsx` | **ingerido** | Decodificado en [[formato-cronograma-excel]]. Sede Los Olivos, semana de mayo. |

### Teoría de patrones — `Archivos_de_clase/*.pptx`

| Fuente | Estado | Patrones (según el nombre) |
|---|---|---|
| `S1_s1 - Material diseño.pptx` | pendiente | Introducción al diseño |
| `S2_s1 - Material diseño3.pptx` | pendiente | Introducción al diseño |
| `S2_s1 - Material diseño3_FHJEBU.pptx` | pendiente | Variante del anterior; comparar antes de ingerir |
| `S3_s1 - Material diseño.pptx` | pendiente | Introducción al diseño |
| `S4_s1 - Material de diseño.pptx` | pendiente | Introducción al diseño |
| `S06_s1-Patrones-Creacionales-SP_DPA.pptx` | **ingerido** → [[s06-singleton-prototype]] | Creacionales: Singleton, Prototype |
| `S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx` | **ingerido** → [[s07-factory-abstractfactory-builder]] | Creacionales: Factory, Abstract Factory, Builder *(confirmado)* |
| `S08_s1-Patrones-Estructurales-AF.pptx` | **ingerido** → [[s08-adapter-facade]] | Estructurales: Adapter, Facade *(confirmado)* |
| `S09_s1-Patrones-Estructurales-DC.pptx` | **ingerido** → [[s09-decorator-composite]] | Estructurales: Decorator, Composite *(confirmado)* |
| `S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx` | **ingerido** → [[s11-proxy-bridge]] | Estructurales: Proxy, Bridge |
| `S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx` | **ingerido** → [[s12-state-observer]] | Comportamiento: State, Observer |
| `S13_s1 - Patrones comportamiento_Command_Memento.pptx` | **ingerido** → [[s13-command-memento]] | Comportamiento: Command, Memento |
| `S14_s1 - Antipatrones Concepto, Propósito.pptx` | **ingerido** → [[s14-antipatrones]] | Antipatrones |
| `S16_s1_.pptx` | pendiente | Tema no identificable por el nombre |
| `S17_s1_.pptx` | pendiente | Tema no identificable por el nombre |
| `Repaso sesion 05 .pptx` | pendiente | Repaso |
| `Repaso sesion 10.pptx` | pendiente | Repaso |
| `Repaso sesion 15.pptx` | pendiente | Repaso |

### Guías de taller y ejemplos de código — `Archivos_de_clase/*.docx`, `*.xlsx`

| Fuente | Estado | Nota |
|---|---|---|
| `S01_s2 - Guia-Taller_RFHMRU.docx` | pendiente | Guía de taller |
| `S02_s2-DP - Guia-Taller_TGJTKM.docx` | pendiente | Guía de taller |
| `S03_s2 - Guia-Taller.docx` | pendiente | Guía de taller |
| `S04_s2 - Guia-Taller.docx` | pendiente | Guía de taller |
| `S06_s2 - Guia-Taller_DPA.docx` | pendiente | Taller de creacionales |
| `S07_s2 - Guia-Taller_DPA_HERZGI.docx` | pendiente | Taller de creacionales |
| `S08_s2 - Guia-Taller_DPA.docx` | pendiente | Taller de estructurales |
| `S09_s2 - Guia-Taller_DPA.docx` | pendiente | Taller de estructurales |
| `S12-GUIA-TALLER_DPA.docx` | pendiente | Taller de State/Observer |
| `S13-Taller-ejemplo.docx` | pendiente | Taller de Command/Memento |
| `S14-GUIA-TALLER.xlsx` | pendiente | Taller de antipatrones |
| `S16-GUIA-TALLER.xlsx` | pendiente | Guía de taller |
| `Ejemplo de Patrón Singleton y Prototype en Java.docx` | pendiente | Código de ejemplo en Java |
| `factory-decorator.docx` | pendiente | Código Java: Factory + Decorator sobre citas médicas. Muy cercano al dominio. |
| `S11-Ejemplo-MVC-Proxy-Bridge.docx` | pendiente | Código de ejemplo: MVC, Proxy, Bridge |

### Evaluaciones y entregables — `Archivos_de_clase/*.pdf`

**Desbloqueados el 2026-09-05** al instalarse Python: se leen con
`python tools/extraer_pdf.py <ruta>`. Ojo, el script pierde tildes en algunos de ellos.

| Fuente | Estado | Nota |
|---|---|---|
| `S10_s1s2 - PC-2-DPA.pdf` | pendiente | Práctica calificada 2. ~104 000 caracteres extraíbles. |
| `S10_s1s2 - PC-2-DPA (1).pdf` | omitir | **Copia idéntica** de la anterior (mismo MD5) |
| `S10_s1s2 - PC-2-DPA (2).pdf` | omitir | **Copia idéntica** de la anterior (mismo MD5) |
| `S15_s1s2 - PC-3-DPA.pdf` | **ingerido** | Práctica calificada 3 → [[pc3-entregable]]. El entregable del curso. |
| `Poster-Indicaciones.pdf` | pendiente | No son indicaciones del curso: es el artículo *"Recomendaciones para la elaboración de un póster científico"* (Díaz V., Pediátrica de Panamá, 2016). |
| `Cartel-Indicaciones.pdf` | pendiente | ~101 000 caracteres extraíbles |
| `Diseño-Poster.pdf` | omitir | **Copia idéntica** de `Cartel-Indicaciones.pdf` (mismo MD5) |
| `CASO DE EJEMPLO 2.pdf` | ⚠️ bloqueado | Solo 510 caracteres: PDF de imágenes, necesitaría OCR. |

## Enlaces pendientes de escribir

Páginas ya referenciadas desde el wiki que aún no existen. Son trabajo señalado, no errores:

- `[[patron-adapter]]` — depende de si se importan los Excel históricos
  ([[adr-001-patrones-seleccionados]], pregunta 12 de [[huecos-abiertos]]).

Bridge, Command, Builder, Prototype, Abstract Factory y Memento **no tendrán página**: están
descartados, y su razón vive en [[mapa-patron-requisito]]. State sí la tiene, porque se
descartó *después* de haberse aceptado.
