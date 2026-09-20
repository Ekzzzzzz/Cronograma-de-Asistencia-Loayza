---
titulo: Estructura del repositorio
tipo: arquitectura
estado: estable
fuentes:
  - Archivos_de_clase/PC-3 (Anexo 1)
  - decisiones del usuario (2026-09-07)
actualizado: 2026-09-07
tags: [arquitectura, repositorio, entregable, pc3]
---

# Estructura del repositorio

Cómo se organiza el código entregable. Dos nombres los fija [[pc3-entregable]] —`MVC` y `SQL`,
en el anexo— y el resto se decide pensando en **quién abre el repositorio**: un docente que
evalúa una arquitectura de cuatro capas y que no va a leer este wiki.

## El árbol

```
Cronograma-de-Asistencia-Loayza/
├─ MVC/                                    ← nombre exigido por el anexo
│  ├─ pom.xml
│  ├─ src/main/java/pe/loayza/asistencia/
│  │  ├─ vista/                            Proxy · Observer · Command · Decorator
│  │  ├─ control/                          Facade · Factory
│  │  ├─ modelo/                           Singleton · Composite · Bridge · Adapter
│  │  ├─ bd/                               sin patrón: llama a los procedimientos
│  │  └─ AsistenciaApplication.java
│  ├─ src/main/resources/
│  │  ├─ templates/                        marcar.html · dashboard/dia.html · semana.html
│  │  ├─ static/app.js                     cámara, compresión, sello y cola sin señal
│  │  └─ application.yml
│  └─ src/test/java/…                      una prueba de integración por capa
├─ SQL/                                    ← nombre exigido por el anexo
│  ├─ 01_esquema.sql                       6 tablas
│  ├─ 02_procedimientos.sql                10 procedimientos almacenados
│  ├─ 03_datos_prueba.sql                  7 sedes y dos semanas de marcaciones
│  └─ 04_purga_fotos.sql                   el evento nocturno de los 3 meses
├─ Pattern Design/                         este wiki
└─ README.md
```

## Los paquetes se llaman como las capas

`vista`, `control`, `modelo` y `bd` — **no** `controller`, `service` y `repository`, que es la
convención de Spring.

> **El motivo es la rúbrica, y conviene decirlo sin adornos.** [[pc3-entregable]] da 2 puntos
> por «arquitectura de 4 capas» y 6 por «uso de patrones a 4 capas». Un examinador que abre
> `src/main/java` y ve `vista`, `control`, `modelo` y `bd` **está viendo la respuesta**. Uno que
> ve `controller` y `service` tiene que traducir, y en una entrevista de veinte minutos esa
> traducción la hacemos nosotras o no la hace nadie.

**El costo:** nos apartamos de la convención del ecosistema. Si el proyecto creciera con más
gente, lo correcto sería renombrar. Es una decisión tomada **para esta entrega** y así queda
escrita.

## Un solo módulo Maven, no cuatro

Se podría hacer un módulo por capa y que el compilador impidiera que `modelo` importe `vista`.
Es más riguroso y **se descarta**: con la entrega a menos de un mes, cuatro `pom.xml` son cuatro
sitios donde algo se rompe la semana de la entrega.

La separación se sostiene **por disciplina y por las pruebas**, no por el compilador. Es un
compromiso consciente y va en el informe, no escondido.

## Qué vive en cada capa

| Capa | Paquete | Contenido | Patrones |
|---|---|---|---|
| **Vista** | `vista/` | Controladores de Thymeleaf, endpoint público de marcación, filtro de límites | Proxy · Observer · Decorator · Command |
| **Control** | `control/` | El caso de uso «registrar marcación» completo, de la petición al evento | Facade · Factory |
| **Modelo** | `modelo/` | `Jornada`, `Turno`, `Marcacion`, catálogo de sedes, almacén de fotos | Singleton · Composite · Bridge · Adapter |
| **Base de datos** | `bd/` | Llamadas a los 10 procedimientos. Sin lógica | *ninguno, a propósito* |

Ver [[cuatro-capas]] para el reparto que fija el curso y [[mapa-patron-requisito]] para el que
proponemos nosotras.

## Las pruebas

**Una prueba de integración por capa, cuatro en total.** No es una elección de calidad sino un
requisito: la estructura obligatoria del informe pide «pruebas de integración por capa» **con
capturas por caso de prueba**.

Se escriben en la semana 3, no al final: son lo primero que se sacrifica cuando aprieta el
calendario y aquí no se puede sacrificar.

## El repositorio es público

Confirmado por el usuario el 2026-09-07. Tiene una consecuencia inmediata:

> **Este wiki contiene nombres reales de trabajadoras**, tomados de
> [[formato-cronograma-excel]]. En un repositorio público el historial de *commits* no se borra
> fácil. Hay que reemplazarlos por ficticios cuanto antes — y los datos de prueba de
> `03_datos_prueba.sql` deben serlo de todos modos, porque el video se sube al aula virtual.

## Enlaces

[[pc3-entregable]] · [[cuatro-capas]] · [[modelo-datos]] · [[mapa-patron-requisito]] ·
[[adr-009-estructura-y-dos-patrones-que-vuelven]]
