---
titulo: Patrón Factory
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S07_s1-Patrones-Creacionales-FB_FLDRVK.pptx
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-06
tags: [creacional, gof, nucleo]
---

# Patrón Factory

**Familia:** creacional · **Sesión:** [[s07-factory-abstractfactory-builder]] · **Capa:**
Control (fijada por PC-3) · **Requisito:** [[requisitos]] RF-04

## Qué es

Genera objetos "sin revelar el mecanismo de creación al cliente"; el cliente usa siempre la
misma interfaz estándar. Ventaja que remarca el curso: permite **añadir tipos nuevos sin
alterar el código cliente**.

## Por qué encaja aquí

[[requisitos]] RF-04 pide dos botones: **entrada** o **salida**. Ese es exactamente el
parámetro de tipo del ejemplo `NotificacionFactory` de S07, que devuelve
`NotificacionCorreo` o `NotificacionSMS`. La correspondencia es uno a uno, así que el
ejemplo del curso se puede citar en el informe como fundamento directo.

Y las dos marcaciones **no son iguales**: una salida necesita una entrada previa con la que
emparejarse, una entrada no. Esa diferencia de comportamiento justifica dos clases, no un
booleano.

## Diseño propuesto

```java
public enum TipoMarcacion { ENTRADA, SALIDA }

public interface Marcacion extends ComponenteCronograma {
    void validar(Jornada jornadaActual);
    TipoMarcacion tipo();
}

public class MarcacionEntrada implements Marcacion {
    @Override public TipoMarcacion tipo() { return TipoMarcacion.ENTRADA; }

    @Override
    public void validar(Jornada jornadaActual) {
        if (jornadaActual.estaAbierta())
            throw new MarcacionInvalida("Ya hay una entrada sin salida en esta sede.");
    }
}

public class MarcacionSalida implements Marcacion {
    @Override public TipoMarcacion tipo() { return TipoMarcacion.SALIDA; }

    @Override
    public void validar(Jornada jornadaActual) {
        if (!jornadaActual.estaAbierta())
            throw new MarcacionInvalida("No hay una entrada previa que cerrar.");
    }
}

// La fábrica
public class MarcacionFactory {

    public static Marcacion crear(TipoMarcacion tipo,
                                  Trabajadora trabajadora,
                                  Sede sede,
                                  Foto evidencia,
                                  String notas) {
        return switch (tipo) {
            case ENTRADA -> new MarcacionEntrada(trabajadora, sede, evidencia, notas);
            case SALIDA  -> new MarcacionSalida(trabajadora, sede, evidencia, notas);
        };
    }
}
```

El cliente —[[patron-facade]]— nunca hace `new MarcacionEntrada(...)`: pide a la fábrica y
recibe la interfaz.

## Por qué Factory y no Abstract Factory

S07 lo dice explícitamente: los diseños **empiezan con Factory Method** —menos difícil, más
adaptable— y avanzan hacia Abstract Factory **solo cuando se descubre que hace falta más
flexibilidad**. Aquí no hay familias de productos que varíen juntas: solo hay marcaciones.
Abstract Factory quedó descartado en [[mapa-patron-requisito]].

## Cuidados

- El `switch` sobre el enum es exhaustivo: si mañana aparece un tercer tipo (por ejemplo,
  una pausa de refrigerio) el compilador obliga a tratarlo. Eso es deseable.
- No convertir la fábrica en un cajón de sastre con quince ramas: sería el Golden Hammer de
  [[s14-antipatrones]].

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[patron-facade]] · [[patron-state]]
