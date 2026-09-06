---
titulo: S14 — Antipatrones
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S14_s1 - Antipatrones Concepto, Propósito.pptx
actualizado: 2026-09-05
tags: [antipatrones, unidad-4]
---

# S14 — Antipatrones

Unidad 4. 42 diapositivas. **Clave para este proyecto**, porque [[pc3-entregable]] exige un
capítulo de "Evaluación de antipatrones (planificación y evaluación con evidencias)".

## Concepto

Los antipatrones son "soluciones a problemas de diseño que, aunque pueden parecer válidas en
un principio, resultan ser **ineficaces o perjudiciales a largo plazo**". Su propósito de
estudio es identificarlos y evitarlos.

La sesión los clasifica en cuatro tipos: de **diseño de software** (la clase muy gorda con
toda la lógica de negocio), de **diseño orientado a objetos** (la **"Singletonitis"**, el
abuso del Singleton), de **programación** (código espagueti) y **metodológicos**
(programación de copiar y pegar).

## Antipatrones de desarrollo de software

| Antipatrón | Descripción | Solución alternativa |
|---|---|---|
| **Big Ball of Mud** | Sistema sin estructura, diseño caótico | Arquitectura definida, diseño modular, patrones |
| **Shotgun Surgery** | Un cambio obliga a tocar muchas partes dispersas | Cohesión y bajo acoplamiento |
| **Vendor Lock-In** | Dependencia excesiva de un proveedor | Estándares abiertos, interfaces bien definidas |
| **Not Invented Here** | Rechazar soluciones existentes y rehacerlas | Evaluar herramientas probadas antes de construir |
| **Overengineering** | Complejidad innecesaria para los requisitos actuales | **YAGNI** (*You Aren't Gonna Need It*) |
| **Lava Flow** | Código viejo acumulado, nunca eliminado ni refactorizado | Limpieza y refactorización periódicas |
| **Magic Numbers** | Literales sueltos en el código | Constantes con nombres descriptivos |
| **Golden Hammer** | Usar la misma herramienta para todo problema | Elegir la herramienta adecuada a cada caso |

## Antipatrones organizacionales

Silo Mentality · Top-Down Management · Micromanagement · Blame Culture · Management by
Objectives · Overloaded Teams · Lack of Vision · Ineffective Communication.

## Qué aporta al proyecto

Esta sesión da **el argumento formal para no meter los quince patrones del curso**:

- **Overengineering** es exactamente "desarrollar un sistema con características y
  complejidades innecesarias que no se requieren para cumplir con los requisitos actuales".
  Forzar un patrón sin problema que lo justifique es eso.
- **Golden Hammer** es aplicar la misma solución a todo sin evaluar si corresponde.
- **Singletonitis** es la advertencia concreta sobre [[s06-singleton-prototype]].

Es decir: **descartar los patrones que no encajan no es incumplir el curso, es aplicar la
unidad 4**. Y como PC-3 pide evaluar antipatrones *con evidencias*, cada descarte razonado
es evidencia. Ver [[mapa-patron-requisito]].

## Antipatrones que este proyecto ya arrastra o roza

- **Magic Numbers y Big Ball of Mud** aparecen en el Excel actual: horas escritas a mano con
  formatos inconsistentes (`10.02AM`, `9:57AM - 8PM`) y sin estructura verificable. Ver
  [[formato-cronograma-excel]]. Es el problema que el sistema viene a resolver, así que sirve
  de línea base para el capítulo.
- **Overengineering** es el riesgo activo del proyecto, precisamente por la presión de los
  diez patrones obligatorios.
