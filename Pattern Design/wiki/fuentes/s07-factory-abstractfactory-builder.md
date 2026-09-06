---
titulo: S07 — Factory, Abstract Factory y Builder
tipo: fuente
estado: estable
fuentes:
  - Archivos_de_clase/S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx
actualizado: 2026-09-05
tags: [creacional, factory, abstract-factory, builder, unidad-2]
---

# S07 — Factory, Abstract Factory y Builder

Unidad 2, patrones creacionales. 38 diapositivas.

## Contenido

**Factory.** Genera objetos "sin revelar el mecanismo de creación al cliente"; el cliente
usa siempre la misma interfaz estándar. El ejemplo de la sesión es una
**`NotificacionFactory`** que devuelve `NotificacionCorreo` o `NotificacionSMS` según un
parámetro de tipo:

```java
public class NotificacionFactory {
    public static Notificacion crearNotificacion(String tipo) {
        if (tipo.equalsIgnoreCase("correo")) return new NotificacionCorreo();
        else if (tipo.equalsIgnoreCase("sms")) return new NotificacionSMS();
        else throw new IllegalArgumentException("Tipo de notificación desconocido");
    }
}
```

Ventaja que remarca: permite añadir tipos nuevos **sin alterar el código cliente**.

**Abstract Factory.** Interfaz para crear **familias** de objetos relacionados sin
especificar sus clases concretas (ejemplo: muebles modernos vs. victorianos, cada familia
con silla y sofá). La sesión aclara la progresión típica: los diseños **empiezan con Factory
Method** —menos difícil, más adaptable— y avanzan hacia Abstract Factory, Prototype o
Builder **solo cuando se descubre que hace falta más flexibilidad**.

**Builder.** Construye objetos complejos **paso a paso**; evita constructores con muchos
parámetros. Ejemplo: un `Coche` con marca, modelo, año, color, aire acondicionado y GPS.
Desventaja explícita: "**puede ser excesivo para objetos simples** donde un Factory o
incluso un simple constructor serían suficientes".

## Qué aporta al proyecto

- **Factory es el encaje más limpio y directo del proyecto.** `NotificacionFactory`
  (correo/SMS) es estructuralmente idéntico a lo que necesitamos: crear la marcación según
  sea **entrada o salida** ([[requisitos]] RF-04). El ejemplo del curso se puede citar tal
  cual en el informe.
- **Builder** tiene un encaje medio: construir el reporte de cronograma, que sí tiene muchas
  opciones (sede, rango de fechas, incluir o no fotos, formato de salida).
- **Abstract Factory no tiene familia de productos que justificarlo.** Ver
  [[mapa-patron-requisito]].

## Aviso que la sesión da y conviene tomar en serio

Dos frases sirven de argumento directo para no inflar el diseño: empezar por Factory y
avanzar **solo cuando se necesite más flexibilidad**, y que Builder **puede ser excesivo**
para objetos simples. Es el mismo criterio que [[s14-antipatrones]] llama *Overengineering*.

## Referencias de la sesión

bin Uzayr, S. (2023). *Software Design Patterns: The Ultimate Guide*. CRC Press.
