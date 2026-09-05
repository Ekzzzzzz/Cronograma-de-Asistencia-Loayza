---
tipo: moc
titulo: Índice de la wiki
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Índice

Catálogo de todo lo que existe en esta wiki. El agente lo actualiza en cada ingesta,
consulta archivada y decisión. Para leer la evolución en el tiempo, ver [[log]].

Punto de entrada narrativo: [[overview]] · Tesis viva del proyecto: [[sintesis]]

---

## Dominio — Podología Loayza

| Página | Resumen |
|---|---|
| [[problema-cronogramas]] | Enunciado del proceso manual que se reemplaza, y las preguntas abiertas que quedan. |
| [[jornadas-multiples]] | Varias marcaciones al día, rotación entre sedes, límite de 6 y lo que eso rompe del formato del cronograma. |
| [[sedes]] | Las 7 sedes completas: dirección, piso, coordenadas decodificadas de los Plus Codes, matriz de distancias y diseño de la geocerca (200 m, sensible a la precisión del GPS). |
| [[antifraude]] | Por qué existe el requisito antifraude, los 7 vectores de fraude y su defensa. El cliente nunca es confiable: todo se verifica en el servidor. |
| [[formato-cronograma-actual]] | Anatomía del Excel actual (Los Olivos, dic 2025 – jul 2026): rejilla semanal, vocabulario de estados y evidencia de los fallos del proceso manual. Define el formato de salida objetivo. |
| [[dominio-moc]] | Mapa de las páginas de dominio. |

Pendientes de crear: `trabajadoras`, `marcacion`, `jornada`, `cronograma-diario`,
`reconocimiento-facial`, `reglas-de-negocio`.

## Patrones de diseño

Los 15 patrones del curso, ingeridos de S06–S13. Mapa completo con la evaluación de cada
uno contra el proyecto: [[patrones-moc]].

| Patrón | Categoría | Uso |
|---|---|---|
| [[patron-singleton]] | creacional | no — Spring ya lo da |
| [[patron-prototype]] | creacional | candidato |
| [[patron-factory]] | creacional | **candidato** |
| [[patron-abstract-factory]] | creacional | no — sobra aquí |
| [[patron-builder]] | creacional | **candidato** |
| [[patron-adapter]] | estructural | **candidato** |
| [[patron-facade]] | estructural | **candidato** |
| [[patron-decorator]] | estructural | **candidato** |
| [[patron-composite]] | estructural | **candidato** |
| [[patron-proxy]] | estructural | **candidato** |
| [[patron-bridge]] | estructural | no — aún no hace falta |
| [[patron-state]] | comportamiento | **candidato** |
| [[patron-observer]] | comportamiento | **candidato** |
| [[patron-command]] | comportamiento | **candidato** |
| [[patron-memento]] | comportamiento | candidato |

## Conceptos

| Página | Resumen |
|---|---|
| [[antipatrones]] | Los 16 antipatrones del curso evaluados contra el proyecto. Borrador del entregable de `S14-GUIA-TALLER.xlsx`. |
| [[conceptos-moc]] | Mapa de conceptos transversales (SOLID, UML, MVC). |

## Fuentes ingeridas

**9 de 40 archivos.** Teoría de patrones, antipatrones y los entregables del curso. Mapa e inventario:
[[fuentes-moc]].

| Página | Sesión | Contenido |
|---|---|---|
| [[fuente-s06-singleton-prototype]] | 6 | Singleton, Prototype — con código Java |
| [[fuente-s07-factory-builder]] | 7 | Factory, Abstract Factory, Builder — con código |
| [[fuente-s08-adapter-facade]] | 8 | Adapter, Facade — con código |
| [[fuente-s09-decorator-composite]] | 9 | Decorator, Composite — con código |
| [[fuente-s11-proxy-bridge]] | 11 | Proxy (4 tipos), Bridge — sin código |
| [[fuente-s12-state-observer]] | 12 | State, Observer — sin código |
| [[fuente-s13-command-memento]] | 13 | Command, Memento — sin código |
| [[fuente-s14-antipatrones]] | 14 | Los 16 antipatrones — conceptual |
| [[fuente-pc2-pc3-entregables]] | 10 y 15 | **Los entregables y su rúbrica**: 4 capas, patrones por capa, ODS, APA 7 |

Muestras del negocio (en `docs/`, analizadas en `wiki/dominio/`):

| Archivo | Página derivada |
|---|---|
| `docs/Cronograma_2026_Olivos.xlsx` | [[formato-cronograma-actual]] |

## Decisiones (ADR)

| ADR | Título | Estado |
|---|---|---|
| [[adr-001-stack-y-arquitectura]] | PWA en Vercel + backend Spring Boot + base de datos; Excel sólo como exportación. | **aceptada** |
| [[adr-002-canal-de-marcacion]] | Canal propio: las trabajadoras marcan desde una PWA con la cámara. Se descarta WhatsApp. | **aceptada** |
| [[adr-003-autenticacion-jwt]] | Spring Security + JWT, sesión persistente atada al dispositivo, dos roles, enrolamiento presencial. | **aceptada** |
| [[adr-004-llenado-del-cronograma]] | Sin programación de turnos el sistema llena solo el 81,7 % de la rejilla; el resto va a una cola de dos opciones. | propuesta |
| [[adr-005-interfaz-de-marcacion]] | Cuatro pantallas, tres toques y cero teclado. Incluye [prototipo navegable](https://claude.ai/code/artifact/8ed44102-37c0-4620-8019-d4b56f991b78). | propuesta |
| [[decisiones-moc]] | Mapa de decisiones | — |

## Consultas archivadas

| Página | Pregunta que responde |
|---|---|
| [[consultas-moc]] | Mapa de consultas archivadas. |

---

## Backlog de ingesta

Archivos en `Archivos_de_clase/` aún sin página en `wiki/fuentes/`.
Orden sugerido: primero las sesiones de patrones (S06→S14), luego los talleres y casos.

### Teoría de patrones
- [x] `S06_s1-Patrones-Creacionales-SP_DPA.pptx` — Singleton, Prototype
- [x] `S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx` — Factory, Abstract Factory, Builder
- [x] `S08_s1-Patrones-Estructurales-AF.pptx` — Adapter, Facade
- [x] `S09_s1-Patrones-Estructurales-DC.pptx` — Decorator, Composite
- [x] `S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx` — Proxy, Bridge
- [x] `S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx` — State, Observer
- [x] `S13_s1 - Patrones comportamiento_Command_Memento.pptx` — Command, Memento
- [x] `S14_s1 - Antipatrones Concepto, Propósito.pptx` — los 16 antipatrones

### Ejemplos de código en Java (prioridad alta)
Contienen el Java que falta en las diapositivas de S11–S13:
- [ ] `S11-Ejemplo-MVC-Proxy-Bridge.docx` — Proxy, Bridge y MVC
- [ ] `S12-GUIA-TALLER_DPA.docx` — State, Observer
- [ ] `S13-Taller-ejemplo.docx` — Command, Memento
- [ ] `Ejemplo de Patrón Singleton y Prototype en Java.docx`
- [ ] `factory-decorator.docx`

### GRASP — prioridad alta (hay rúbrica sobre el proyecto)
- [ ] `S16_s1_.pptx` — Experto, Creador, Alta cohesión, Bajo acoplamiento, Controlador
- [ ] `S17_s1_.pptx` — Experto en información, Fabricación Pura, Polimorfismo
- [ ] `S16-GUIA-TALLER.xlsx` — rúbrica de evaluación de GRASP sobre el proyecto

### Fundamentos de diseño (prioridad media — PC-3 pide el principio ISP)
- [ ] `S1_s1 - Material diseño.pptx`
- [ ] `S2_s1 - Material diseño3.pptx`
- [ ] `S2_s1 - Material diseño3_FHJEBU.pptx`
- [ ] `S3_s1 - Material diseño.pptx`
- [ ] `S4_s1 - Material de diseño.pptx`

### Guías de taller (prioridad media — dan el formato de entregable esperado)
- [ ] `S01_s2 - Guia-Taller_RFHMRU.docx`
- [ ] `S02_s2-DP - Guia-Taller_TGJTKM.docx`
- [ ] `S03_s2 - Guia-Taller.docx`
- [ ] `S04_s2 - Guia-Taller.docx`
- [ ] `S06_s2 - Guia-Taller_DPA.docx`
- [ ] `S07_s2 - Guia-Taller_DPA_HERZGI.docx`
- [ ] `S08_s2 - Guia-Taller_DPA.docx`
- [ ] `S09_s2 - Guia-Taller_DPA.docx`
- [ ] `S12-GUIA-TALLER_DPA.docx`
- [ ] `S14-GUIA-TALLER.xlsx`

### Evaluaciones — **prioridad máxima, ya ingeridas**
- [x] `S10_s1s2 - PC-2-DPA.pdf` — rúbrica de PC-2
- [x] `S15_s1s2 - PC-3-DPA.pdf` — rúbrica de PC-3 y formato del informe
  *(las variantes `(1)` y `(2)` de PC-2 son copias byte a byte del original)*

### Repasos y casos (prioridad baja)
- [ ] `Repaso sesion 05 .pptx`, `Repaso sesion 10.pptx`, `Repaso sesion 15.pptx`
- [ ] `CASO DE EJEMPLO 2.pdf` — 2 páginas, sin texto extraíble ni imágenes; hay que abrirlo

### Fuera de alcance (material gráfico, no aporta al diseño)
- `Cartel-Indicaciones.pdf`, `Diseño-Poster.pdf`, `Poster-Indicaciones.pdf`
