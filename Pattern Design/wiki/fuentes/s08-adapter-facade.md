---
titulo: S08 — Adapter y Facade
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S08_s1-Patrones-Estructurales-AF.pptx
actualizado: 2026-09-05
tags: [estructural, adapter, facade, unidad-3]
---

# S08 — Adapter y Facade

Unidad 3, patrones estructurales. 34 diapositivas.

## Contenido

Los patrones estructurales resuelven problemas estableciendo **relaciones estructurales**
entre entidades; el mecanismo principal es la **agregación de objetos**.

**Adapter.** Resuelve problemas de **"desajuste"**: interfaces que no coinciden. `Target` es
la interfaz que el cliente usa, `Adaptee` la incompatible, y `Adapter` implementa `Target`
usando una instancia de `Adaptee`. El ejemplo es una empresa con un **sistema antiguo de
inventario** cuyo formato de salida no sirve a la aplicación nueva: `InventoryAdapter`
traduce `OldInventorySystem.getItemDetails()` al formato que espera `InventoryService`, **sin
modificar el sistema existente**.

**Facade.** Crea "una interfaz más unificada para un sistema más complejo", simplificando el
acceso mediante **un único punto de entrada**. Dos ejemplos: una `LavadoraFacade` que expone
lavar, enjuagar y centrifugar ocultando tres subsistemas; y —en el caso práctico— un
**hospital** que integra registros médicos electrónicos, laboratorio y facturación tras una
sola fachada, "sin que los usuarios interactúen directamente con cada sistema".

Diferencia que la sesión subraya: **Facade simplifica**, Adapter **traduce**.

## Qué aporta al proyecto

- **Facade encaja fuerte.** El caso práctico del hospital es casi nuestro problema: una
  fachada `RegistrarMarcacion` que esconda validación, sellado de la foto, persistencia y
  notas detrás de una sola llamada ([[requisitos]] RF-05 a RF-08).
- **Adapter encaja mejor de lo que parecía.** No hay sistema legado de software, pero **sí
  hay un formato legado**: el Excel que la empresa llena a mano, con sus irregularidades
  (`10.02AM`, `9:57AM - 8PM`, espacios sobrantes) documentadas en
  [[formato-cronograma-excel]]. Adaptar ese formato al modelo nuevo es el mismo problema que
  `OldInventorySystem`. Y Adapter **no es obligatorio** para [[pc3-entregable]], así que es
  una ganancia opcional.

## Referencias de la sesión

- Hu, C. (2023). *An Introduction to Software Design*. Springer Nature.
- bin Uzayr, S. (2023). *Software Design Patterns: The Ultimate Guide*. CRC Press.
