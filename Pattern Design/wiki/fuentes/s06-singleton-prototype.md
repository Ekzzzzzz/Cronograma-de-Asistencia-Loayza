---
titulo: S06 — Singleton y Prototype
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx
actualizado: 2026-09-05
tags: [creacional, singleton, prototype, unidad-2]
---

# S06 — Singleton y Prototype

Unidad 2, patrones creacionales. 34 diapositivas.

## Contenido

Los patrones creacionales abstraen el proceso de instanciación. La sesión los divide en
**creación de clases** (usan herencia) y **creación de objetos** (usan delegación), y lista
seis: Factory Method, Abstract Factory, Builder, Singleton, Object Pool y Prototype.

**Singleton.** "Crea una clase que tenga una única instancia y proporcione un punto de
acceso global". Dos variedades: **instanciación temprana** (en la carga) y **perezosa**
(solo cuando se necesita). El ejemplo canónico de la sesión es una clase `DBConnection`:
sin Singleton se abren conexiones redundantes a la base de datos; con él se controla el
balanceo de carga desde una sola instancia.

Desventajas que la propia sesión reconoce:

- Es complicado en entornos **multihilo**: hay que evitar que se construya varias veces.
- **Viola la responsabilidad única**, porque resuelve dos problemas a la vez.
- **Dificulta las pruebas unitarias**, porque agrega estado global.

**Prototype.** Crea objetos **copiando una instancia existente**. Útil "cuando la creación
directa de un objeto es costosa o compleja". En Java se implementa con `Cloneable` y
sobrescribiendo `clone()`. El ejemplo es una jerarquía `Shape` → `Rectangle` / `Circle`.

## Qué aporta al proyecto

- **Singleton es aplicable** al catálogo de las 7 sedes y a la conexión de base de datos, y
  el ejemplo `DBConnection` de la sesión es literalmente nuestro caso. Ver
  [[mapa-patron-requisito]].
- **Prototype es el encaje más débil.** La condición que la sesión exige —creación costosa
  o compleja— no se cumple: una marcación es un objeto barato. Registrado como tensión.

## Tensiones

La sesión enseña Singleton como buena práctica, pero [[s14-antipatrones]] nombra la
**"Singletonitis"** —su abuso— como antipatrón de diseño orientado a objetos. Ambas cosas
son ciertas: el patrón sirve una vez, justificado; multiplicarlo es el antipatrón. Esta
tensión es material directo para el capítulo de antipatrones que exige [[pc3-entregable]].

## Referencias de la sesión

bin Uzayr, S. (2023). *Software Design Patterns: The Ultimate Guide*. CRC Press.
