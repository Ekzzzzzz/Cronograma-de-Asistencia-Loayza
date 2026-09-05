---
tipo: fuente
titulo: S12 — Patrones de comportamiento: State y Observer
archivo: "Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx"
sesion: 12
tags: [fuente, comportamiento]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S12 — Patrones de comportamiento: State y Observer

## Resumen

Introduce la familia: los patrones de comportamiento se centran en **cómo los objetos
interactúan y se comunican**, y en cómo se reparten las responsabilidades entre ellos
(diapositiva 12).

**State** (diapositivas 18–21): una interfaz `State` declara los métodos que implementan los
estados concretos; el objeto **Context** guarda una referencia a un `State` y **delega** en
él (diapositiva 18). Es clave cuando el comportamiento del objeto depende de su estado
interno (diapositiva 19). Ventaja: encapsula los estados y mejora la legibilidad;
desventaja: resulta excesivo si hay pocos estados (diapositiva 20).

**Observer** (diapositivas 22–23): interfaz `Observer` con el método `update()`; la clase
`Subject` mantiene la lista de observadores y **los notifica** cuando cambia su estado
(diapositiva 22). Esencial cuando un cambio en un objeto debe reflejarse en otros.

## Conceptos clave

- [[patron-state]] · [[patron-observer]]

## Código del curso

> [!warning] Hueco en esta fuente
> S12 **no trae código Java**: sólo definiciones y tablas de ventajas y desventajas. Las
> diapositivas 13–16 son diagramas sin texto extraíble. El código habría que sacarlo de
> `Archivos_de_clase/S12-GUIA-TALLER_DPA.docx`, **aún sin ingerir**.

## Qué aporta al proyecto

Los dos son candidatos fuertes. State modela el ciclo de vida de una marcación; Observer es
lo que Spring ya ofrece con sus eventos de aplicación. Ver [[patron-state]] y
[[patron-observer]].
