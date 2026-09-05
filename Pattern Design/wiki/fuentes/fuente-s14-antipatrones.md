---
tipo: fuente
titulo: S14 — Antipatrones: concepto y propósito
archivo: "Archivos_de_clase/S14_s1 - Antipatrones Concepto, Propósito.pptx"
sesion: 14
tags: [fuente, antipatron]
creado: 2026-09-05
actualizado: 2026-09-05
estado: activo
---

# S14 — Antipatrones: concepto y propósito

33 diapositivas. **16 antipatrones** con la misma ficha cada uno: descripción, problema y
solución alternativa.

## Resumen

Un antipatrón es una solución que **parece válida al principio pero resulta ineficaz o
perjudicial a largo plazo** (diapositiva 10). Estudiarlos sirve para identificar y evitar
prácticas ya conocidas como problemáticas (diapositiva 11).

Cuatro tipos (diapositiva 12): de **diseño de software** (la clase gorda con toda la lógica
de negocio), de **diseño orientado a objetos** (la *singletonitis*, abuso del Singleton), de
**programación** (código espagueti) y **metodológicos** (copiar y pegar en vez de
generalizar).

## Los 16 antipatrones

**De desarrollo de software** (diapositivas 14–21): Big Ball of Mud, Shotgun Surgery,
Vendor Lock-In, Not Invented Here, Overengineering, Lava Flow, Magic Numbers, Golden Hammer.

**Organizacionales** (diapositivas 23–30): Silo Mentality, Top-Down Management,
Micromanagement, Blame Culture, Management by Objectives, Overloaded Teams, Lack of Vision,
Ineffective Communication.

Los 16 están desarrollados y evaluados contra el proyecto en [[antipatrones]].

## Código del curso

No aplica: la sesión es conceptual, sin ejemplos en Java.

## El entregable asociado

`S14-GUIA-TALLER.xlsx` pide **evaluar los 16 antipatrones sobre el propio proyecto**, en dos
hojas:

| Columna | Contenido |
|---|---|
| descripción, problema, solución alternativa | «Completar de la ppt o de libro o de asistente» |
| completo (5) / parcial (3) / no aplicado (1) | Marcar con una X donde aplica |
| Evidencia | «Captura del código en la hoja de evidencias» |
| Puntaje obtenido / deseado | Para el cálculo |

Se calcula un **nivel alcanzado** por categoría —desarrollo de software y organizacionales—
y uno **global**, con la fórmula `(PO/PD)*100`.

> [!note] Observación sobre la rúbrica
> Ocho de los dieciséis son **organizacionales** (mentalidad de silos, micromanejo, cultura
> de culpar). En un proyecto de un solo desarrollador no describen al equipo. Pero varios sí
> describen el **proceso manual actual de Podología Loayza**, que es lo que el sistema viene
> a reemplazar — y así están evaluados en [[antipatrones]], que es una lectura defendible y
> mucho más útil que marcarlos como «no aplica».

## Qué aporta al proyecto

Confirma decisiones ya tomadas y añade una guardia nueva:

- **Vendor Lock-In** valida dejar el despliegue sin decidir y meter el almacenamiento y el
  reconocedor facial detrás de interfaces propias.
- **Not Invented Here** y **Overengineering** respaldan los dos patrones descartados en
  [[patrones-moc]].
- **Magic Numbers** obliga a que el radio de la geocerca, los umbrales y los tiempos de vida
  sean configuración con nombre, nunca literales ([[sedes]]).
- **Blame Culture** refuerza que el sistema no escriba `INASISTENCIA` por su cuenta
  ([[adr-004-llenado-del-cronograma]]).

## Enlaces

[[antipatrones]] · [[patrones-moc]] · [[fuentes-moc]]
