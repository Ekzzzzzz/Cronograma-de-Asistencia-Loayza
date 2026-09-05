---
tipo: moc
titulo: Patrones — mapa
tags: [moc, patron]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Patrones — mapa

15 patrones del curso, ingeridos de S06–S13. Cada página sigue la plantilla de
`CLAUDE.md` §4 e incluye la sección obligatoria **«Aplicación en Podología Loayza»**.

La columna «uso» es la evaluación contra el proyecto real. Ninguna es todavía una decisión
formal: eso son los ADR de [[decisiones-moc]].

## Creacionales — [[fuente-s06-singleton-prototype]], [[fuente-s07-factory-builder]]

| Patrón | Uso | Por qué |
|---|---|---|
| [[patron-singleton]] | **en disputa** | Técnicamente innecesario (Spring ya lo da), pero **la rúbrica de PC-3 lo exige en la capa modelo** — ver [[fuente-pc2-pc3-entregables]] |
| [[patron-prototype]] | candidato | Clonar la plantilla del cronograma semanal y los perfiles de verificación por sede |
| [[patron-factory]] | **candidato** | Exportadores del cronograma; verificaciones antifraude por sede |
| [[patron-abstract-factory]] | no | No hay familias de productos que deban ser coherentes entre sí |
| [[patron-builder]] | **candidato** | `Marcacion` tiene demasiados campos, muchos opcionales y aportados por etapas distintas |

## Estructurales — [[fuente-s08-adapter-facade]], [[fuente-s09-decorator-composite]], [[fuente-s11-proxy-bridge]]

| Patrón | Uso | Por qué |
|---|---|---|
| [[patron-adapter]] | **candidato** | Envolver el motor de reconocimiento facial y la librería de Excel; es lo que permite probar el sistema sin motor real |
| [[patron-facade]] | **candidato** | `ServicioDeMarcacion` como único punto de entrada a la tubería |
| [[patron-decorator]] | **candidato** | Apilar caché, auditoría y métricas sobre el reconocedor facial |
| [[patron-composite]] | **candidato** | Componer las verificaciones de [[antifraude]]; agregar conteos por el árbol del cronograma |
| [[patron-proxy]] | **candidato** | Protection Proxy sobre fotos y biometría (`CLAUDE.md` §8); Virtual Proxy para no cargar imágenes |
| [[patron-bridge]] | no | La explosión de clases que justifica el puente todavía no existe |

## De comportamiento — [[fuente-s12-state-observer]], [[fuente-s13-command-memento]]

| Patrón | Uso | Por qué |
|---|---|---|
| [[patron-state]] | **candidato** | Ciclo de vida de una marcación: 6 estados con reglas de transición reales |
| [[patron-observer]] | **candidato** | Registrar una marcación dispara 5 consecuencias; Spring lo trae con eventos |
| [[patron-command]] | **candidato** | Las correcciones de la administradora *son* el registro de auditoría, con deshacer |
| [[patron-memento]] | candidato | Guarda el estado previo que el `deshacer()` de Command necesita |

## Los descartes, y por qué importan

[[patron-abstract-factory]] y [[patron-bridge]] se descartan a propósito, y conviene
defender ambas decisiones en el entregable: reconocer que la variabilidad que justifica un
patrón aún no existe demuestra más criterio que aplicarlo por cumplir.

**[[patron-singleton]] queda en disputa**: el argumento técnico para descartarlo sigue en
pie, pero la rúbrica de PC-3 lo exige en la capa modelo
([[fuente-pc2-pc3-entregables]]). Es una decisión del usuario, no del agente.

El propio curso avisa de las dos cosas
([[fuente-s06-singleton-prototype]] diapositiva 14; [[fuente-s07-factory-builder]]
diapositiva 18).

## Pendientes

- ~~Antipatrones (S14)~~ → ingerido: [[antipatrones]]. Dos de sus entradas respaldan los
  descartes de esta tabla (Not Invented Here, Overengineering).
- El código Java de Proxy, Bridge, State, Observer, Command y Memento **no está en las
  diapositivas**: vive en los `.docx` de taller, sin ingerir.
- **GRASP** (S16 y S17): Experto, Creador, Alta cohesión, Bajo acoplamiento, Controlador,
  Fabricación Pura y Polimorfismo. Sin ingerir, y `S16-GUIA-TALLER.xlsx` pide **evaluarlos
  sobre el proyecto** con la misma rúbrica que los antipatrones.
- **Mapeo a las cuatro capas** que exige PC-3 ([[fuente-pc2-pc3-entregables]]): Vista
  (Proxy, Bridge, Observer, Command), Control (Facade, Factory), Modelo (Singleton,
  Prototype). Pendiente de ADR.

Relacionado: [[conceptos-moc]] · [[decisiones-moc]] · [[sintesis]] · [[index]]
