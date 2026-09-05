---
tipo: moc
titulo: Fuentes — mapa de ingesta
tags: [moc, fuente]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Fuentes — mapa de ingesta

Un resumen por archivo de `Archivos_de_clase/`. Los originales son **inmutables**: aquí
viven sólo las páginas derivadas.

**Progreso: 9 de 40 archivos.** Teoría de patrones, antipatrones y **los entregables**.

> [!important] Lo más importante ingerido hasta ahora
> [[fuente-pc2-pc3-entregables]] define **qué hay que entregar y cómo se califica**:
> arquitectura de cuatro capas, patrones asignados por capa, *stored procedures*, un ODS
> y un informe en APA 7. Estaba mal clasificado como «prioridad baja».

| Fuente | Sesión | Patrones | ¿Trae código Java? |
|---|---|---|---|
| [[fuente-s06-singleton-prototype]] | 6 | Singleton, Prototype | **Sí**, completo |
| [[fuente-s07-factory-builder]] | 7 | Factory, Abstract Factory, Builder | **Sí**, completo |
| [[fuente-s08-adapter-facade]] | 8 | Adapter, Facade | **Sí**, completo |
| [[fuente-s09-decorator-composite]] | 9 | Decorator, Composite | **Sí**, completo |
| [[fuente-s11-proxy-bridge]] | 11 | Proxy, Bridge | **No** — está en un `.docx` aparte |
| [[fuente-s12-state-observer]] | 12 | State, Observer | **No** — sólo definiciones |
| [[fuente-s13-command-memento]] | 13 | Command, Memento | **No** — sólo definiciones |
| [[fuente-s14-antipatrones]] | 14 | 16 antipatrones | No aplica — sesión conceptual |
| [[fuente-pc2-pc3-entregables]] | 10 y 15 | Rúbricas y formato del informe | No aplica — evaluación |

## Patrón observado en el material

Las sesiones 6 a 9 traen código Java completo y utilizable. **De la 11 en adelante, las
diapositivas sólo traen definiciones, importancia y tablas de ventajas y desventajas**; los
ejemplos se movieron a los archivos de taller (`S11-Ejemplo-MVC-Proxy-Bridge.docx`,
`S12-GUIA-TALLER_DPA.docx`, `S13-Taller-ejemplo.docx`), **aún sin ingerir**.

Consecuencia práctica: las páginas de los seis últimos patrones tienen la sección «Ejemplo
del curso» marcada como hueco. Si hace falta código del curso para el entregable, hay que
ingerir esos tres `.docx`.

## Errores detectados en las fuentes

- **S13, diapositiva 10**: titulada «Patrón Command», describe en realidad el Memento
  (texto idéntico al de la diapositiva 14). Ver [[fuente-s13-command-memento]].

El backlog completo, ordenado por prioridad, está en [[index]].

Relacionado: [[patrones-moc]] · [[index]] · [[log]]
