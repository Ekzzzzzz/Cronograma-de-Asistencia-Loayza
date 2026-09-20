---
titulo: ADR-009 — Estructura del repositorio y dos patrones que vuelven
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-07)
  - Archivos_de_clase/PC-3 (Anexo 1 y rúbrica)
actualizado: 2026-09-07
tags: [adr, decision, patrones, repositorio, plan, entregable]
---

# ADR-009 — Estructura del repositorio y dos patrones que vuelven

**Fecha:** 2026-09-07 · **Estado:** aceptada

## Contexto

Al recapitular el proyecto contra la rúbrica aparecieron tres cosas que el wiki no había mirado
de frente:

1. **De los diez patrones que PC-3 exige, teníamos siete.** Ese criterio vale **6 puntos**, más
   que ningún otro de la rúbrica.
2. **La entrega es en menos de un mes.** El plan de [[adr-001-patrones-seleccionados]] estaba
   dibujado a ocho semanas.
3. **No existe ni una carpeta del entregable.** El anexo pide `MVC` y `SQL` en un repositorio
   público, y el repositorio hoy solo tiene el wiki.

## 1. Bridge y Command vuelven

Los dos estaban descartados desde [[adr-001-patrones-seleccionados]]. **Vuelven, y el motivo
importa más que el hecho: no cambiamos de opinión sobre los patrones — cambió el problema.**

| Patrón | Por qué se descartó | Qué apareció después |
|---|---|---|
| **Bridge** | «Con dos formatos de salida añade una jerarquía doble sin la explosión de clases que viene a evitar» | Las fotos viven en **tres sitios** —Azure en el curso, Oracle en producción, disco en pruebas ([[adr-005-hosting-largo-plazo]])— y ahora hay **dos tipos de foto**: la evidencia que se purga a los 3 meses y la de referencia de la rejilla de caras, que es permanente ([[adr-008-alcance-interfaz-y-cola-sin-senal]]). Dos jerarquías que varían por su cuenta |
| **Command** | «Se solapa con Factory y Facade. Solo aportaría el *undo*, que nadie pidió» | La **cola sin señal** de [[adr-008-alcance-interfaz-y-cola-sin-senal]]: la marcación se captura como objeto, se guarda en el teléfono y se ejecuta más tarde en el servidor. Encolar y reejecutar es la razón de ser del patrón, y no es algo que Factory ni Facade hagan |

> **Esto no es rescatar patrones para llenar la cuenta.** Los dos usos nacieron de requisitos
> que el usuario pidió después del descarte: la migración de nube y la marcación sin conexión.
> El criterio de [[adr-001-patrones-seleccionados]] no se toca — un patrón entra si deja el
> código mejor que sin él. Lo que cambió es que ahora lo dejan.

**Van con borde punteado, como Adapter: se confirman escribiendo la clase.** Si Bridge acaba
siendo una interfaz con una sola implementación real, es una interfaz y se dice así. **Nueve
patrones bien defendidos valen más en la entrevista que diez que no se sostienen**, y el
capítulo de antipatrones evalúa exactamente eso.

> **Riesgo concreto a vigilar:** Bridge y [[patron-adapter]] caen sobre la misma familia de
> clases. Bridge es la separación entre abstracción e implementación; Adapter es lo que hace
> *una* de esas implementaciones al traducir el SDK. Es coherente, pero hay que poder
> sostenerlo en dos frases delante del docente.

## 2. Prototype se queda fuera — y se entrega igual

Sigue sin haber un uso honesto: una marcación es un objeto barato de crear, que es justo la
condición que [[s06-singleton-prototype]] exige. Forzarlo sería el Overengineering que este
proyecto se comprometió a evitar.

**Pero omitirlo del informe sería peor que no tenerlo.** Va como **sección propia —«Prototype:
evaluación y descarte»—** con la cita de S06. Así deja de ser un hueco en el índice y pasa a ser
evidencia para el capítulo de antipatrones, que es obligatorio de todos modos.

**Cuenta final: 9 de los 10 que PC-3 nombra**, más Adapter, que no pide.

## 3. Estructura del repositorio

Detalle completo en [[estructura-repositorio]]. Las decisiones:

- **`MVC/` y `SQL/` en la raíz**, porque el anexo los nombra.
- **Los paquetes de Java se llaman `vista`, `control`, `modelo` y `bd`**, no `controller` /
  `service` / `repository`. La rúbrica da 8 de 20 puntos por arquitectura y patrones a cuatro
  capas: quien abre `src/main/java` tiene que **ver la respuesta**, no traducirla.
- **Un solo módulo Maven**, no uno por capa. Cuatro `pom.xml` son cuatro sitios donde algo se
  rompe la semana de la entrega. La separación se sostiene por disciplina y por las pruebas.
- **Los archivos de `SQL/` van numerados** por orden de ejecución.
- **Una prueba de integración por capa**, escritas en la semana 3.

## 4. El plan pasa de ocho semanas a cuatro

| Semana | Qué | Patrones |
|---|---|---|
| **1** | Esqueleto, esquema completo y marcación de punta a punta | Singleton · Factory · Facade · Decorator · Bridge |
| **2** | El dashboard con sus ocho funciones | Proxy · Observer · Composite |
| **3** | Cola sin señal, correcciones, historial, totales e intentos · **las pruebas** | Command · Adapter |
| **4** | Video y ensayo de la entrevista | — |
| **En paralelo** | **Capítulo 1 del informe**, desde hoy | — |

**El capítulo 1 empieza ya.** Antecedentes nacionales e internacionales de artículos o tesis,
marco teórico a base de libros y redacción APA 7 no dependen de que exista una línea de código,
y es la parte que más caro se paga si se deja para el final.

### Lo que se cae, dicho ahora y no en la semana 4

| Qué | Cuándo se retoma |
|---|---|
| **La migración a Oracle** | Después de la entrega. El curso va sobre Azure, que ya está decidido y con crédito de sobra |
| **Las fotos de referencia** | Antes del despliegue real. La rejilla funciona con iniciales sobre color |
| **La presentación a la dueña** | Después de PC-3, con el sistema estable. Enseñarla a medio hacer es peor que tarde |

**Lo que no se cae, aunque tiente:** las pruebas de integración por capa y la evaluación de
antipatrones. Las dos son obligatorias en la estructura del informe. La buena noticia de la
segunda es que **la evidencia ya existe y es real**: la violación de ISP que corregimos al
introducir `Turno` ([[marcacion-multiple]]), la validación duplicada que era *Shotgun Surgery*, y
los cinco descartes razonados para no caer en *Golden Hammer*. No hay que fabricar nada, hay que
redactarlo.

## 5. Aparece un décimo procedimiento

`sp_intentos_rechazados` — la lista que alimenta la octava función del dashboard
([[adr-007-seguridad-endpoint-publico]] exigía que los intentos se vieran, y el procedimiento
faltaba). Ver [[modelo-datos]].

## Consecuencias

- **Nueva página [[estructura-repositorio]]**, que es ahora la referencia del entregable.
- **Hacen falta [[patron-bridge]] y [[patron-command]]**, más [[patron-adapter]], que sigue sin
  escribirse.
- **[[modelo-datos]] gana `sp_intentos_rechazados`.**
- **Aparece una tarea que no es de código y es urgente:** el repositorio **ya es público** y este
  wiki lleva **nombres reales de trabajadoras**. Hay que reemplazarlos por ficticios.
- **El plan deja de tener holgura.** Cualquier cosa que se añada a partir de ahora sale de otra.

## Enlaces

[[adr-001-patrones-seleccionados]] · [[adr-008-alcance-interfaz-y-cola-sin-senal]] ·
[[estructura-repositorio]] · [[pc3-entregable]] · [[cuatro-capas]] · [[modelo-datos]]
