---
titulo: Visión general del proyecto
tipo: sintesis
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
actualizado: 2026-09-06
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
- **Cara administradora**: **detrás de login**, dashboard con una pestaña por sede y
  exportación a Excel en el formato que la empresa ya usa.

El sistema escribe en el cronograma **solo horas** (`10:12AM - 8:46PM`). Descansos,
inasistencias y tardanzas los anota la administradora a mano
([[adr-003-login-y-cronograma-simple]]).

Detalle de requisitos numerados en [[requisitos]].

## El objetivo

**Código limpio aplicando los patrones del curso que encajen con el problema.** No usar
todos, ni maximizar la cuenta: usar los que dejan el código mejor de lo que estaría sin
ellos.

De los quince patrones que enseña el curso, **se implementan siete**
([[adr-001-patrones-seleccionados]], revisado en [[adr-003-login-y-cronograma-simple]]):
Decorator, Composite, Facade, Proxy, Factory, Singleton y Observer — las tres familias que el
curso pide. Los descartes se documentan con su razón, que es material para el capítulo de
antipatrones que exige [[pc3-entregable]]. **State es el más instructivo**: se aceptó, se
diseñó y se descartó cuando un cambio de requisitos le quitó el problema que resolvía.

El mapa completo, con el veredicto de cada patrón, está en [[mapa-patron-requisito]].

## Estado

**Diseño cerrado, código sin empezar.** Están ingeridas las fuentes de patrones (S06–S14) y
PC-3; hay tres ADR que fijan stack, patrones, modelo de datos y despliegue; y
[[modelo-datos]] ya define el esquema MySQL con el que se puede empezar a construir.

Falta el proyecto Maven, el SQL y la aplicación. Ver [[huecos-abiertos]] para lo que sigue
pendiente de decidir — sobre todo el ODS, que bloquea el informe.

## Por dónde seguir

- [[requisitos]] — qué tiene que hacer el sistema.
- [[marcacion-multiple]] — la regla central del negocio.
- [[modelo-datos]] — el esquema con el que se construye.
- [[sedes]] — las 7 sedes y sus datos.
- [[formato-cronograma-excel]] — el formato de salida que hay que reproducir.
- [[decisiones]] — los tres ADR y lo que sigue abierto.
- [[huecos-abiertos]] — qué falta decidir y qué está bloqueado.
