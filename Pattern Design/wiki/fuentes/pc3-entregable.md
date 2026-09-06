---
titulo: PC-3 — Práctica Calificada 3 (el entregable del curso)
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-05
tags: [entregable, evaluacion, curso, rubrica]
---

# PC-3 — Práctica Calificada 3

**La fuente más importante del wiki hasta ahora.** No enseña patrones: dice exactamente qué
hay que entregar, con qué arquitectura y con qué patrones. Convierte varias decisiones que
parecían libres en requisitos cerrados.

Extraído con `python tools/extraer_pdf.py`. El texto útil son las primeras ~39 líneas; el
resto del volcado es basura binaria de fuentes incrustadas. Los acentos salieron intactos.

## Qué pide

**Objetivo.** Aplicar patrones **estructurales, creacionales y de interacción y
responsabilidades** (comportamiento) para resolver problemas del proyecto de curso, sobre
las unidades de aprendizaje 1 a 4.

**Caso de estudio.** "Proyecto designado por el grupo de trabajo relacionado a **ODS**"
(Objetivos de Desarrollo Sostenible). El proyecto debe declarar a qué ODS responde — ver
[[huecos-abiertos]].

**Cuatro entregables:**

1. Informe con el formato del Anexo 1, redacción **APA 7**, archivo
   `Apellidos1-2-3-PC3-informe.pdf`.
2. **Demo del software en video mp4**, con datos de prueba, mostrando las funcionalidades
   **por módulo**: `Apellidos1-2-3-DPA-PC3-video.mp4`.
3. Entrega de informe y video en el aula virtual, **semana 15, antes de la sesión**.
4. **Entrevista individual presencial** sobre el proyecto.

## Rúbrica (20 puntos)

| Criterio | Puntos |
|---|---|
| Presentación de informe, redacción APA 7 | 2 |
| Arquitectura de 4 capas (informe) | 2 |
| **Uso de patrones de diseño a 4 capas (informe)** | **6** |
| Demo del software a 4 capas con pruebas, estudiante exponiendo (video) | 5 |
| Entrevista presencial | 5 |

Dos lecturas que importan para priorizar:

- **Los patrones valen 6 puntos, más que ningún otro criterio.** Es el corazón de la nota.
- **10 de 20 puntos son de exposición** (video + entrevista), no de código. El software
  tiene que ser *demostrable con datos de prueba*, no solo compilar.

## Estructura obligatoria del informe (Anexo 1)

- Carátula · Índice general · Índice de figuras · Índice de tablas
- **Capítulo 1: Introducción** (citas APA 7)
  - Describir ODS, problema, solución, objetivo
  - Antecedentes internacionales y nacionales, de artículos o tesis de solución similar
  - Marco teórico: definición de patrón de arquitectura y patrones de diseño, **a base de
    libros**
- **Capítulo 2: Desarrollo**
  - Unidad I: desarrollo de **principios ISP**, con capturas de código y ejecución
  - Unidades II, III y IV, con capturas de código y ejecución
  - **Diseño de la arquitectura a cuatro capas con los patrones usados**
  - Patrones creacionales: **Singleton, Prototype, Factory** (código y ejecución)
  - Patrones estructurales: **Facade, Decorator, Composite** (código y ejecución)
  - **Pruebas de integración por capa** (capturas por caso de prueba)
  - **Evaluación de antipatrones**: planificación y evaluación con evidencias
- **Capítulo 3: Conclusiones**
  - Lecciones aprendidas por cada unidad (I, II, III, IV)
  - Lecciones aprendidas del equipo de trabajo, por estudiante
- Referencias bibliográficas en APA 7
- Anexos en **modo público**: enlace del video mp4 y **enlace del repositorio GitHub del
  proyecto (carpeta MVC y SQL)**

## Lo que esto cierra

Detalle en [[cuatro-capas]] y [[mapa-patron-requisito]]. En resumen:

1. **La arquitectura no se elige: son cuatro capas**, y el informe se evalúa sobre eso.
2. **Los patrones no se eligen libremente.** PC-3 nombra diez y los reparte por capa.
3. **Tiene que haber base de datos con scripts y stored procedures.** No es opcional.
4. **El repositorio va en GitHub con carpetas `MVC` y `SQL`.**
5. **Hay que evaluar antipatrones con evidencias**, no solo aplicar patrones.

## Tensiones

- **Spring Boot vs. "carpeta MVC y SQL".** El usuario pidió Spring Boot ([[requisitos]]
  RNF-03); PC-3 pide una estructura de repositorio con carpetas `MVC` y `SQL` y stored
  procedures. No es incompatible — Spring Boot puede organizarse en cuatro capas y llamar
  stored procedures — pero **descarta un diseño solo-JPA** y obliga a escribir SQL a mano.
- **El equipo parece ser de tres.** Los nombres de archivo son `Apellidos1-2-3` y las
  conclusiones piden lecciones "por estudiante". El brief del usuario está redactado en
  singular. Anotado en [[huecos-abiertos]].

> **Inferencia:** PC-3 evalúa las unidades 1 a 4, y menciona ISP (unidad I) y antipatrones
> (unidad IV, por [[index]]). Eso sugiere que PC-3 es la entrega **acumulativa final**, no
> una más. Confirmar contra `S10_s1s2 - PC-2-DPA.pdf`, aún sin ingerir.
