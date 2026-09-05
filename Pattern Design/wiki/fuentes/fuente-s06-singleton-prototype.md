---
tipo: fuente
titulo: S06 — Patrones creacionales: Singleton y Prototype
archivo: "Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx"
sesion: 6
tags: [fuente, creacional]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S06 — Patrones creacionales: Singleton y Prototype

34 diapositivas. La sesión más rica en código Java de todo el bloque.

## Resumen

Introduce la familia creacional: patrones que **abstraen el proceso de instanciación** y
hacen al sistema independiente de cómo se crean, componen y representan los objetos
(diapositiva 10). Distingue dos subfamilias: los **patrones de creación de clases** usan
**herencia**, los **de creación de objetos** usan **delegación** (diapositivas 8 y 30).

Enumera como creacionales: Singleton, Prototype, Factory Method, Abstract Factory, Builder
y Object Pool (diapositivas 8 y 10).

## Conceptos clave

- [[patron-singleton]] — una sola instancia y punto de acceso global. Dos variedades:
  **instanciación temprana** (al cargar) y **perezosa** (al primer uso), diapositiva 12.
- [[patron-prototype]] — crear objetos **clonando** una instancia existente, útil cuando
  crear directamente es costoso o complejo (diapositiva 17).
- [[clasificacion-gof]] — creación por herencia frente a creación por delegación.

## Código del curso

Java completo y utilizable para ambos patrones:

- Singleton con instanciación perezosa (diapositivas 13 y 15) y su demo (diapositiva 15).
- Prototype con `implements Cloneable` y `super.clone()` (diapositiva 17), y el ejemplo
  `Shape` / `Rectangle` / `Circle` (diapositivas 19–21).

## Advertencias que da el propio curso sobre Singleton

Vale la pena retenerlas porque condicionan su uso en el proyecto (diapositiva 14):

- Es problemático en **entornos multihilo** si no se protege la construcción.
- **Viola el principio de responsabilidad única**: resuelve dos problemas a la vez.
- **Dificulta mucho las pruebas unitarias** porque añade estado global.

## Qué aporta al proyecto

El caso motivador de la sesión —`DBConnection` como singleton para no abrir conexiones de
más (diapositiva 11)— es justamente lo que **Spring ya resuelve solo** con sus beans. Ver la
discusión en [[patron-singleton]].

## Ejercicio propuesto

Gestor de conexiones a base de datos con Singleton en una app de gestión de pedidos;
entregable: **diagrama de clases UML** (diapositiva 25).
