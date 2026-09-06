---
titulo: S11 — Proxy y Bridge
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx
actualizado: 2026-09-05
tags: [estructural, proxy, bridge, unidad-3]
---

# S11 — Proxy y Bridge

Unidad 3, patrones estructurales. 37 diapositivas. Más conceptual que las anteriores: define,
lista ventajas y desventajas, pero **no trae ejemplos de código en Java**.

## Contenido

**Proxy.** "Proporciona un sustituto o marcador de posición para otro objeto **para
controlar el acceso** a él". La sesión distingue cuatro tipos:

| Tipo | Para qué |
|---|---|
| **Virtual Proxy** | Retrasa la creación y carga del objeto hasta que sea necesario |
| **Protection Proxy** | Controla el acceso, con permisos distintos por usuario |
| Remote Proxy | Proxy local de un objeto en otro espacio de direcciones |
| Smart Proxy | Añade seguimiento de referencias o contabilidad de recursos |

Ventajas: control de acceso, carga diferida, administración de recursos, seguridad.
Desventajas: indirección adicional, más complejo de implementar, puede introducir latencia y
dificultar la depuración.

**Bridge.** "Separa la abstracción de su implementación, permitiendo que ambas varíen
independientemente". Sirve para evitar la **explosión de clases** cuando hay múltiples
variantes de una implementación. Ventajas: variar la implementación sin tocar la
abstracción, menos acoplamiento. Desventajas: más complejidad inicial y más esfuerzo para
establecer bien las jerarquías.

La tarea de la sesión pide expresamente: "**aplicar a la interfaz gráfica de usuario de un
módulo** que utilicen Proxy y Bridge" — coherente con que [[pc3-entregable]] los ubique en
la capa Vista.

## Qué aporta al proyecto

- **Proxy encaja por partida doble**, y con los dos tipos que la sesión detalla:
  - **Protection Proxy** para el dashboard de administradora ([[requisitos]] RF-11): la
    trabajadora entra sin login, la administradora no debería.
  - **Virtual Proxy** para las fotos ([[requisitos]] RF-10): son pesadas y el dashboard
    mostrará muchas; cargarlas solo al abrirlas es exactamente la carga diferida.
- **Bridge encaja de forma media.** El uso razonable es separar el **reporte de cronograma**
  (abstracción) de su **formato de salida** (implementación: pantalla, Excel, PDF), que da
  cobertura a [[requisitos]] RF-13. Es defendible, pero con dos formatos no hay todavía la
  explosión de clases que el patrón viene a evitar.

## Hueco

La sesión no trae código Java de ninguno de los dos, a diferencia de S06–S09. Para el
informe habrá que escribirlo desde cero o buscarlo en las guías de taller, aún sin ingerir.

## Referencias de la sesión

Refactoring Guru (proxy) y reactiveprogramming.io, más tres videos de YouTube.
