---
titulo: S09 — Decorator y Composite
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S09_s1-Patrones-Estructurales-DC.pptx
actualizado: 2026-09-05
tags: [estructural, decorator, composite, unidad-3]
---

# S09 — Decorator y Composite

Unidad 3, patrones estructurales. 31 diapositivas. **La sesión más útil para este proyecto.**

## Contenido

**Decorator.** Permite "agregar funcionalidades adicionales a un objeto de manera dinámica
sin modificar su estructura". `Decorator` y `BasicComponent` derivan ambos de `Component`, y
el decorador **agrega una instancia** del componente básico: la operación del decorador está
"decorada" con la del componente.

El ejemplo es `Transport`: sobre un `BasicTransport` se encadenan `InsuranceDecorator` y
`TrackingDecorator`, cada uno añadiendo descripción y costo:

```java
Transport t = new TrackingDecorator(new InsuranceDecorator(new BasicTransport()));
```

Distinción fina que da la sesión: **Decorator es estructuralmente como Proxy**, pero con
intención opuesta — en Decorator al cliente le interesa *lo que se agrega*; en Proxy, *el
objeto agregado*.

**Composite.** Trata de manera uniforme objetos individuales y composiciones, en relaciones
**parte-todo**. La sesión lo describe como "una generalización del patrón Decorator con una
colección agregada de instancias de Component": Decorator ofrece composición *vertical*
simple; Composite, una jerarquía completa.

El ejemplo es un **sistema de gestión de pacientes**: `Treatment` como interfaz,
`SingleTreatment` como hoja y `CompositeTreatment` como nodo que agrega tratamientos y suma
sus costos recorriendo la lista.

## Qué aporta al proyecto

Los dos encajes más fuertes de todo el curso, y ambos salen del dominio sin forzar nada:

- **Decorator para el sellado de la foto.** [[requisitos]] RF-06 pide que la fecha y la hora
  **salgan en la imagen**. Eso es literalmente envolver un objeto añadiéndole
  responsabilidades sin cambiar su interfaz: `FotoBase` → `SelloFechaHora` → `SelloSede`.
  La cadena de decoradores del ejemplo `Transport` se traduce uno a uno.
- **Composite para el cronograma.** [[formato-cronograma-excel]] muestra que la celda de
  jornada es una **agregación** de marcaciones, no un dato plano: marcación (hoja) → jornada
  → día → semana de la sede (compuestos). Es el mismo `CompositeTreatment` que suma costos,
  pero sumando y emparejando horas.

Además, el ejemplo del curso es de **pacientes y tratamientos** — dominio clínico, como la
podología. Sirve para el informe casi sin adaptación.

## Referencias de la sesión

- Hu, C. (2023). *An Introduction to Software Design*. Springer Nature.
- bin Uzayr, S. (2023). *Software Design Patterns: The Ultimate Guide*. CRC Press.
