---
titulo: Decisiones
tipo: decision
estado: estable
actualizado: 2026-09-06
tags: [adr, indice]
---

# Decisiones

Índice de las decisiones tomadas, en formato ADR ligero. Una decisión entra aquí cuando deja
de ser una pregunta abierta en [[huecos-abiertos]].

| ADR | Decisión | Estado | Fecha |
|---|---|---|---|
| [[adr-001-patrones-seleccionados]] | Stack Spring Boot, núcleo de 7 patrones, periferia de 5 y 3 descartes razonados | aceptada | 2026-09-06 |

## Pendientes de decidir

Las que siguen abiertas están en [[huecos-abiertos]]. Las más urgentes, porque bloquean el
modelo de datos:

1. Identidad de la trabajadora: ¿texto libre o lista cerrada?
2. Cómo se autentica la administradora.
3. De dónde sale el cronograma planificado que distingue `DESCANSO` de `NO TURNO` e
   `INASISTENCIA`.
4. Qué motor de base de datos, dado que RC-03 exige stored procedures.
5. A qué ODS responde el proyecto (propuesta: ODS 8, Trabajo decente).
