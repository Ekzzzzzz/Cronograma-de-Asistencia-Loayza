---
tipo: moc
titulo: Overview
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Overview

Punto de entrada de la bóveda. Si no sabes por dónde empezar, empieza aquí.

## Para qué existe esto

Dos cosas a la vez:

1. **Consolidar el curso de patrones de diseño.** Las diapositivas, guías y ejemplos viven
   en `Archivos_de_clase/`. Cada vez que se ingiere una, su contenido se destila en páginas
   de [[patrones-moc|patrón]] y [[conceptos-moc|concepto]] que se van enriqueciendo.
2. **Construir un programa Java que resuelva un problema real**: los cronogramas diarios de
   Podología Loayza. Ver [[problema-cronogramas]].

Lo que une ambas mitades: **cada página de patrón tiene una sección obligatoria
«Aplicación en Podología Loayza»**. El curso deja de ser teoría suelta y se convierte en un
catálogo de herramientas evaluadas contra un problema concreto.

## Cómo navegar

| Si buscas… | Ve a |
|---|---|
| El catálogo completo de páginas | [[index]] |
| Qué pasó y cuándo | [[log]] |
| El estado actual del diseño del programa | [[sintesis]] |
| El problema de negocio | [[problema-cronogramas]] |
| Un patrón concreto | [[patrones-moc]] |
| Por qué se eligió tal patrón | [[decisiones-moc]] |
| El resumen de una clase | [[fuentes-moc]] |

## Cómo trabajar con el agente

- **«Ingiere S06»** → lee la fuente, la resume y propaga los cambios por la wiki.
- **«¿Qué patrón conviene para X?»** → responde citando la wiki; si vale la pena, archiva
  la respuesta en [[consultas-moc|consultas]].
- **«Decidamos cómo leer las fotos»** → escribe un ADR en [[decisiones-moc|decisiones]].
- **«Impleméntalo»** → escribe código en `src/`, sólo si ya hay un ADR aceptado.
- **«Revisa la wiki»** → pasada de lint: contradicciones, huérfanas, huecos.

Las reglas completas están en `CLAUDE.md`.
