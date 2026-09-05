---
tipo: fuente
titulo: S09 — Patrones estructurales: Decorator y Composite
archivo: "Archivos_de_clase/S09_s1-Patrones-Estructurales-DC.pptx"
sesion: 9
tags: [fuente, estructural]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S09 — Patrones estructurales: Decorator y Composite

## Resumen

**Decorator** (diapositivas 9–13): añade responsabilidades a un objeto **envolviéndolo**.
`BasicComponent` y `Decorator` derivan ambos de `Component`, y el decorador **agrega una
instancia** del componente y decora su operación (diapositiva 9).

**Composite** (diapositivas 14 en adelante): para elementos **jerárquicos**, construidos
sobre relaciones **parte-todo**. Permite que el cliente trate de forma uniforme a un
elemento simple y a una composición (diapositiva 14).

## Dos distinciones que da el curso, y que conviene no perder

Diapositiva 9 — **Decorator frente a Proxy**: son estructuralmente parecidos pero con
intención distinta. *Para Decorator, el interés del cliente está en el objeto que se agrega,
mientras que para Proxy, el objetivo del cliente es el objeto agregado.*

Diapositiva 14 — **Composite frente a Decorator**: el decorador también tiene relación
parte-todo, pero *sólo ofrece una composición vertical simple entre el todo y sus partes*.
El Composite es **una generalización del Decorator con una colección agregada de instancias
de Component**.

## Conceptos clave

- [[patron-decorator]] · [[patron-composite]] · [[patron-proxy]] (por contraste)

## Código del curso

- Decorator: `Transport` / `BasicTransport` / `TransportDecorator` abstracto, con
  `InsuranceDecorator` y `TrackingDecorator` **apilables** (diapositivas 11–13).
- Composite: sistema de gestión de pacientes, `Treatment` con tratamientos individuales y
  compuestos tratados de forma uniforme (diapositivas 16–17).

## Qué aporta al proyecto

La pareja Decorator + Composite es la respuesta natural a la **cadena de verificaciones
antifraude** de [[antifraude]]: verificaciones apilables y agrupables. Es la decisión más
rica del proyecto.
