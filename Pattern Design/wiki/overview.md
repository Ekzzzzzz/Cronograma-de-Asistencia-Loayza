---
titulo: Visión general del proyecto
tipo: sintesis
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
actualizado: 2026-09-05
tags: [raiz, proyecto]
---

# Visión general del proyecto

Página raíz del wiki. Si llegas nuevo, empieza aquí y sigue los enlaces.

## El problema

Una cadena de podología con **7 sedes** en Lima (ver [[sedes]]) controla la asistencia de
sus podólogas a mano. El control real que hoy existe es un Excel mensual por sede con el
horario efectivo de cada trabajadora (ver [[formato-cronograma-excel]]): alguien lo llena
observando, y eso no escala ni es verificable.

Dos rasgos del negocio hacen que la solución obvia no sirva:

1. **Una trabajadora rota entre sedes el mismo día.** No hay "una entrada y una salida por
   día": hay N marcaciones por día, cada una atada a una sede distinta.
2. **Las usuarias no son técnicas.** Podólogas mayores de 30 años con poca familiaridad con
   aplicaciones. Cualquier fricción de interfaz se traduce en marcaciones no registradas.

## La solución que se va a construir

Aplicación web en **Spring Boot** con dos caras:

- **Cara trabajadora**: un enlace único, sin login, con un flujo de cinco pasos —
  nombre → sede → entrada/salida → foto con fecha y hora impresas → notas → enviar.
- **Cara administradora**: dashboard con **una pestaña por sede** y exportación a Excel en
  el formato que la empresa ya usa.

Detalle de requisitos numerados en [[requisitos]].

## El objetivo

**Código limpio aplicando los patrones del curso que encajen con el problema.** No usar
todos, ni maximizar la cuenta: usar los que dejan el código mejor de lo que estaría sin
ellos.

De los quince patrones que enseña el curso, **se implementan siete**
([[adr-001-patrones-seleccionados]]): Decorator, Composite, Factory, Facade, Proxy,
Singleton y State. Los descartes se documentan con su razón, que es material para el
capítulo de antipatrones que exige [[pc3-entregable]].

El mapa completo, con el veredicto de cada patrón, está en [[mapa-patron-requisito]].

## Estado

Wiki recién inicializado. Ninguna fuente del curso ha sido ingerida todavía y el material
en PDF está bloqueado por el entorno. Ver [[huecos-abiertos]].

## Por dónde seguir

- [[requisitos]] — qué tiene que hacer el sistema.
- [[sedes]] — las 7 sedes y sus datos.
- [[formato-cronograma-excel]] — el formato de salida que hay que reproducir.
- [[huecos-abiertos]] — qué falta decidir y qué está bloqueado.
