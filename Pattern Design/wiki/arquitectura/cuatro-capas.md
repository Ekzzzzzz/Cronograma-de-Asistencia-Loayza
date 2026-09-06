---
titulo: Arquitectura de cuatro capas
tipo: arquitectura
estado: borrador
fuentes:
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-05
tags: [arquitectura, mvc, capas, curso]
---

# Arquitectura de cuatro capas

**No es una elección de diseño: es un requisito de evaluación.** PC-3 destina 2 puntos a
"Arquitectura de 4 capas (informe)" y 6 puntos a "Uso de patrones de diseño **a 4 capas**"
(ver [[pc3-entregable]]).

## Las capas y sus patrones

PC-3 fija qué patrón se prueba en qué capa, en su sección de pruebas de integración:

| Capa | Patrones exigidos | Responsabilidad en este proyecto |
|---|---|---|
| **Vista** | Proxy, Bridge, Observer, Command | Formulario de marcación y dashboard de administradora |
| **Control** | Facade, Factory | Orquestación del caso de uso "registrar marcación" |
| **Modelo** | Singleton, Prototype | Entidades del dominio y acceso a datos |
| **Base de datos** | *(no lleva patrón)* scripts y **stored procedures** | Persistencia de marcaciones y evidencias |

Además, el informe exige desarrollar por familia:

- **Creacionales:** Singleton, Prototype, **Factory**
- **Estructurales:** **Facade**, **Decorator**, **Composite**

> **Inferencia:** Decorator y Composite aparecen en la exigencia por familia pero **no** en
> el reparto por capa. Hay que ubicarlos por cuenta propia y justificarlo en el informe.
> Decorator encaja natural en el sellado de la foto ([[requisitos]] RF-06) y Composite en
> la agregación del cronograma por sede y semana. Ver [[mapa-patron-requisito]].

Son **diez patrones obligatorios** en total: Singleton, Prototype, Factory, Facade,
Decorator, Composite, Proxy, Bridge, Observer, Command.

## Lo que esto obliga

1. **Base de datos con SQL escrito a mano.** PC-3 pide "scripts y store procedure" como
   evidencia. Un diseño que delegue todo a un ORM no cumple. Habrá que elegir un motor con
   stored procedures (MySQL, PostgreSQL o SQL Server) y escribirlos.
2. **Repositorio GitHub con carpetas `MVC` y `SQL`.** La estructura del repo es parte del
   entregable, no un detalle.
3. **Separación estricta Vista / Control / Modelo / BD.** Debe ser visible en el árbol de
   carpetas, porque el informe se evalúa con capturas.
4. **Principios ISP** (unidad I) documentados con capturas de código.
5. **Evaluación de antipatrones** con evidencias, no solo aplicación de patrones.

## Encaje con Spring Boot

El usuario pidió Spring Boot ([[requisitos]] RNF-03) y el curso pide cuatro capas con SQL
propio. Son compatibles, pero condicionan el diseño:

- Las cuatro capas se mapean a paquetes: vista (controladores web y plantillas), control
  (servicios), modelo (entidades y repositorios) y la capa SQL como scripts versionados.
- Los stored procedures se invocan desde el modelo. Spring lo soporta con
  `@Procedure`/`SimpleJdbcCall` o JDBC directo.
- **Riesgo:** el nombre "Vista" en PC-3 aloja Proxy, Bridge, Observer y Command, que son
  patrones de comportamiento y estructura poco habituales en una capa de presentación web.
  Habrá que justificarlos con cuidado en el informe.

> **Hueco:** no está decidido el motor de base de datos ni si la interfaz será renderizada
> en servidor (Thymeleaf) o una SPA. La decisión afecta dónde viven Proxy, Bridge, Observer
> y Command. Anotado en [[huecos-abiertos]].

## Pendiente

Esta página se escribió solo con PC-3. Falta contrastarla con `S10_s1s2 - PC-2-DPA.pdf` y
con las guías de taller, que aún no se ingieren.
