---
tipo: fuente
titulo: S13 — Patrones de comportamiento: Command y Memento
archivo: "Archivos_de_clase/S13_s1 - Patrones comportamiento_Command_Memento.pptx"
sesion: 13
tags: [fuente, comportamiento]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S13 — Patrones de comportamiento: Command y Memento

## Resumen

**Command** (diapositivas 10–13): **encapsula una solicitud como un objeto**, desacoplando
al emisor del receptor de la acción. Habilita operaciones reversibles (deshacer y rehacer),
**colas de comandos**, registro de operaciones y transacciones diferidas (diapositivas 8
y 12).

**Memento** (diapositivas 14–17): captura y externaliza el estado interno de un objeto **sin
violar su encapsulación**, para poder restaurarlo después (diapositiva 14). Esencial para
deshacer y rehacer, puntos de control y control de versiones.

## Ventajas y desventajas que enumera el curso

Command (diapositiva 12): desacopla invocador de ejecutor, facilita deshacer y rehacer,
permite encolar y registrar comandos, y añadir funcionalidad sin tocar el código existente.
En contra: proliferan las clases, consume más memoria (cada comando es un objeto) y una cola
grande introduce latencia.

Memento (diapositiva 16): mantiene la encapsulación y permite restaurar estados anteriores.
En contra: **consume mucha memoria si se crean demasiados mementos**, y gestionar su ciclo
de vida es complejo.

> [!warning] Error en la fuente
> La diapositiva 10, titulada «Patrón Command — Desarrollo», **describe en realidad el
> patrón Memento**: su texto es idéntico al de la diapositiva 14. Es un error de copiado en
> el material del curso. La definición de Command usada en esta wiki se toma de las
> diapositivas 8, 11 y 12.

## Conceptos clave

- [[patron-command]] · [[patron-memento]]

## Código del curso

> [!warning] Hueco en esta fuente
> S13 **no trae código Java**. Los ejemplos están en
> `Archivos_de_clase/S13-Taller-ejemplo.docx`, **aún sin ingerir**.

## Qué aporta al proyecto

Command es la pieza que convierte las correcciones de la administradora en un **registro de
auditoría** con deshacer — un requisito real del dominio, no un adorno. Memento lo
complementa guardando el estado previo. Ver [[patron-command]] y [[patron-memento]].
