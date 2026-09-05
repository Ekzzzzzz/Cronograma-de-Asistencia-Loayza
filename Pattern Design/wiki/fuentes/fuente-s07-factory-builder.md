---
tipo: fuente
titulo: S07 — Patrones creacionales: Factory, Abstract Factory y Builder
archivo: "Archivos_de_clase/S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx"
sesion: 7
tags: [fuente, creacional]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S07 — Patrones creacionales: Factory, Abstract Factory y Builder

## Resumen

Cubre tres patrones creacionales, con código Java para los tres.

**Factory** (diapositivas 10–16): generar objetos **sin revelar el mecanismo de creación al
cliente**, que usa siempre la misma interfaz estándar. El curso lo presenta con una función
miembro estática, a la que llama «patrón Factory estático». Motivación: un sitio que sólo
vendía libros y luego añade ropa y calzado; sin fábrica hay que tocar toda la base de código
(diapositivas 13–14).

**Abstract Factory** (diapositivas 17–21): interfaz para crear **familias de objetos
relacionados** sin especificar sus clases concretas. Participantes: Abstract Factory,
Concrete Factory, Abstract Product, Concrete Product y Client (diapositiva 17).

**Builder** (diapositivas 22 en adelante): construir un objeto complejo **paso a paso**,
evitando los **constructores telescópicos** — constructores con demasiados parámetros
(diapositiva 22).

## Nota valiosa sobre la evolución del diseño

Diapositiva 18, digna de citarse en cualquier ADR creacional:

> Normalmente, los diseños comienzan con el Factory Method (menos difícil, más adaptable,
> proliferan las subclases) y progresan hacia el Abstract Factory, Prototype o Builder
> (más flexible, más complejo) cuando el diseñador se da cuenta de que se requiere más
> flexibilidad.

También: **Factory Method usa herencia; Prototype usa delegación**.

## Conceptos clave

- [[patron-factory]] · [[patron-abstract-factory]] · [[patron-builder]]

## Código del curso

- Factory: interfaz `Notificacion`, implementaciones `NotificacionCorreo` y
  `NotificacionSMS`, y `NotificacionFactory.crearNotificacion(String tipo)`
  (diapositivas 15–16).
- Abstract Factory: `Silla` / `Sofa` con familias moderna y victoriana (diapositivas 19–21).
- Builder: `Coche` con `CocheBuilder` estático anidado y setters encadenados
  (diapositivas 22–23).

## Qué aporta al proyecto

Los tres son candidatos reales. El Builder del curso —un objeto con muchos atributos
opcionales— es exactamente la forma de `Marcacion`. Ver [[patron-builder]].
