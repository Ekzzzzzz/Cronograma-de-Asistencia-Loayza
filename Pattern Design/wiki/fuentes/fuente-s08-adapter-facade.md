---
tipo: fuente
titulo: S08 — Patrones estructurales: Adapter y Facade
archivo: "Archivos_de_clase/S08_s1-Patrones-Estructurales-AF.pptx"
sesion: 8
tags: [fuente, estructural]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S08 — Patrones estructurales: Adapter y Facade

## Resumen

**Adapter** (diapositivas 10–16): resuelve problemas de **desajuste**, desde parámetros que
no coinciden hasta protocolos entre sistemas distintos. El curso lo enmarca en la migración
a la nube: los sistemas nuevos siguen dependiendo de los existentes (diapositiva 10).
Participantes: **Target** (la abstracción que el cliente usa), **Adapter** (implementa
Target) y **Adaptee** (lo que se adapta), diapositiva 11.

**Facade** (diapositivas 17–21): interfaz **unificada** sobre un sistema complejo compuesto
de varios subsistemas; **un único punto de entrada** que simplifica el acceso
(diapositiva 17). La analogía es la fachada de un edificio.

## Conceptos clave

- [[patron-adapter]] · [[patron-facade]]

## Código del curso

- Adapter: `InventoryService` (target) + `OldInventorySystem` (adaptee) +
  `InventoryAdapter`, que traduce el formato de salida del sistema antiguo
  (diapositivas 13–16).
- Facade: `LavadoraFacade` sobre los subsistemas `Lavado`, `Enjuague` y `Centrifugado`
  (diapositivas 19–21).

## Qué aporta al proyecto

Los dos patrones más directamente aplicables del bloque estructural: Adapter para envolver
el motor de reconocimiento facial y la librería de Excel; Facade para dar un único punto de
entrada a la tubería de marcación. Ver [[patron-adapter]] y [[patron-facade]].
