---
titulo: S13 — Command y Memento
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S13_s1 - Patrones comportamiento_Command_Memento.pptx
actualizado: 2026-09-05
tags: [comportamiento, command, memento, unidad-4]
---

# S13 — Command y Memento

Unidad 4, "Patrones para interacción y responsabilidades". 29 diapositivas. Conceptual, sin
código Java.

## Contenido

**Command.** Encapsula una solicitud como un objeto, separando **el emisor del receptor** de
una acción. Es fundamental para operaciones reversibles y colas de comandos.

- Ventajas: desacopla invocador de ejecutor; facilita **undo/redo**; permite **encolar y
  registrar** comandos; se añaden comandos nuevos sin tocar el código existente.
- Desventajas: muchas clases de comando; **"sobrecarga si se abusa para operaciones
  simples"**; más memoria; difícil de depurar si se acumulan.

**Memento.** Captura y externaliza el estado interno de un objeto **sin violar su
encapsulación**, para restaurarlo después. Esencial en deshacer/rehacer de editores, juegos
y control de versiones.

- Ventajas: mantiene la encapsulación; permite restaurar estados anteriores.
- Desventajas: consume mucha memoria si se crean demasiados mementos; administración
  compleja; ineficiente si el estado es grande.

> Aviso: la diapositiva 10, titulada "Patrón Command", describe en realidad el Memento. Es
> un error del material de origen, no de esta lectura.

## Qué aporta al proyecto

- **Command tiene encaje medio.** Encapsular "marcar entrada" y "marcar salida" como
  comandos permitiría **deshacer una marcación equivocada**, algo verosímil con usuarias no
  técnicas ([[requisitos]] RNF-01). Pero se solapa con Factory y Facade, que ya cubren la
  creación y la orquestación; el valor real que añade es el undo y la bitácora de comandos.
- **Memento tiene encaje débil.** Sus casos son editores, juegos y control de versiones. Una
  marcación es un registro pequeño e inmutable: no hay estado interno rico que capturar y
  restaurar. Y no lo exige [[pc3-entregable]].

> **Inferencia:** si se implementa el deshacer, Command basta. Memento solo aportaría si
> hubiera que restaurar el estado completo de una jornada, y no es el caso. Ver
> [[mapa-patron-requisito]].

## Referencias de la sesión

Refactoring Guru (patrones de comportamiento) y reactiveprogramming.io, más cuatro videos.
