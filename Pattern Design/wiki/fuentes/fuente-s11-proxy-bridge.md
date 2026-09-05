---
tipo: fuente
titulo: S11 — Patrones estructurales: Proxy y Bridge
archivo: "Archivos_de_clase/S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx"
sesion: 11
tags: [fuente, estructural]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# S11 — Patrones estructurales: Proxy y Bridge

## Resumen

**Proxy** (diapositivas 10–17): **sustituto o marcador de posición** de otro objeto para
**controlar el acceso** a él (diapositiva 10). Cómo aplicarlo: identificar el objeto que
necesita control de acceso, crear una clase Proxy **con la misma interfaz** que el objeto
real, y delegar en él añadiendo la funcionalidad extra (diapositiva 14).

**Cuatro tipos de proxy** (diapositiva 15), útil para elegir con precisión:

| Tipo | Para qué |
|---|---|
| **Virtual Proxy** | Retrasa la creación y la carga del objeto hasta que sea necesario |
| **Protection Proxy** | Controla el acceso, con permisos distintos por usuario |
| **Remote Proxy** | Proxy local de un objeto en otro espacio de direcciones |
| **Smart Proxy** | Funcionalidad extra: seguimiento de referencias, contabilidad de recursos |

**Bridge** (diapositivas 18–21): **separa la abstracción de su implementación** para que
ambas varíen de forma independiente. Evita la **explosión de clases** cuando hay múltiples
variantes de una implementación (diapositiva 19).

## Conceptos clave

- [[patron-proxy]] · [[patron-bridge]] · [[mvc]]

## Código del curso

> [!warning] Hueco en esta fuente
> Las diapositivas de S11 **no traen código Java**, sólo definiciones, ventajas y
> desventajas. Los ejemplos están en el archivo aparte
> `Archivos_de_clase/S11-Ejemplo-MVC-Proxy-Bridge.docx`, **aún sin ingerir**.

## Qué aporta al proyecto

El **Protection Proxy** es la respuesta directa a la restricción de privacidad de
`CLAUDE.md` §8: controlar quién accede a las fotos y a las plantillas biométricas. El
**Virtual Proxy** evita cargar imágenes pesadas hasta que la administradora las pide. Ver
[[patron-proxy]].
