---
titulo: S12 — State y Observer
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
actualizado: 2026-09-05
tags: [comportamiento, state, observer, unidad-4]
---

# S12 — State y Observer

Unidad 4, "Patrones para interacción y responsabilidades". 37 diapositivas. Conceptual, sin
código Java.

## Contenido

Los patrones de comportamiento "se centran en **cómo los objetos interactúan y se comunican**
entre sí" y en cómo se distribuyen las responsabilidades.

**State.** Permite a un objeto "cambiar su comportamiento cuando cambia su estado interno.
El objeto parecerá cambiar su clase". Se implementa con una interfaz `State`, clases
concretas por estado, y un `Context` que **delega** las solicitudes al estado actual.

- Ventajas: facilita mantenimiento y extensión; mejora la legibilidad al encapsular estados.
- Desventajas: incrementa la complejidad; **"puede ser excesivo si solo hay pocos estados"**.

**Observer.** "Define una dependencia uno a muchos entre objetos, de manera que cuando un
objeto cambie de estado, todos sus dependientes sean notificados y actualizados
automáticamente". Interfaz `Observer` con `update()`, y un `Subject` que mantiene la lista y
notifica.

- Ventajas: desacopla quien notifica de quien recibe.
- Desventajas: difícil de gestionar con muchos observadores; posibles problemas de
  rendimiento.

## Qué aporta al proyecto

- **State no lo exige [[pc3-entregable]], pero es el mejor encaje no obligatorio del
  curso.** La jornada de una trabajadora es una máquina de estados real:
  `SIN_INICIAR → ABIERTA → CERRADA`, más `INCOMPLETA` cuando hay entrada sin salida. Y el
  Excel actual ya distingue tres estados no derivables de las marcaciones —`DESCANSO`,
  `NO TURNO`, `INASISTENCIA`— documentados en [[formato-cronograma-excel]]. State resuelve
  de paso la pregunta 5 de [[huecos-abiertos]].
- **Observer** cubre el refresco del dashboard y el recálculo de la fila del cronograma
  cuando entra una marcación nueva ([[requisitos]] RF-11 y RF-12). Encaje razonable, aunque
  con un solo tipo de evento la ganancia es moderada.

> **Inferencia:** con seis estados en juego (los tres de jornada más los tres del Excel), la
> desventaja que advierte la sesión —"excesivo si solo hay pocos estados"— **no aplica** a
> este proyecto. Es un argumento a favor de incluirlo.

## Referencias de la sesión

Refactoring Guru (patrones de comportamiento) y reactiveprogramming.io, más seis videos.
