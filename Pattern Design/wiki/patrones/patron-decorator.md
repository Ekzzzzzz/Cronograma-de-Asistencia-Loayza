---
titulo: Patrón Decorator
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S09_s1-Patrones-Estructurales-DC.pptx
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-06
tags: [estructural, gof, nucleo]
---

# Patrón Decorator

**Familia:** estructural · **Sesión:** [[s09-decorator-composite]] · **Capa:** sin asignar
por PC-3; se propone **Vista** · **Requisito:** [[requisitos]] RF-06

El encaje más fuerte del proyecto entero.

## Qué es

"Permite agregar funcionalidades adicionales a un objeto de manera dinámica sin modificar su
estructura" (S09, diapositiva 3). `Decorator` y el componente básico derivan ambos de una
misma interfaz `Component`, y el decorador **agrega una instancia** del componente: su
operación queda "decorada" con la del original.

La distinción que da el curso: Decorator es **estructuralmente como Proxy**, pero con
intención opuesta — en Decorator al cliente le interesa *lo que se agrega*; en
[[patron-proxy]], *el objeto agregado*.

## Por qué encaja aquí

[[requisitos]] RF-06 pide que **la fecha y la hora salgan en la foto**, no solo guardadas
como metadato. Eso es literalmente envolver un objeto añadiéndole responsabilidades sin
cambiar su interfaz. Y los sellos son **acumulables e independientes**: fecha y hora, sede,
nombre de la trabajadora. Cada uno es un decorador que se puede encadenar o quitar.

La cadena `BasicTransport → InsuranceDecorator → TrackingDecorator` del ejemplo de S09 se
traduce uno a uno.

## Diseño propuesto

```java
public interface Foto {
    BufferedImage render();
    String descripcion();
}

// Componente básico: la imagen tal como salió de la cámara
public class FotoBase implements Foto {
    private final BufferedImage original;

    public FotoBase(BufferedImage original) { this.original = original; }

    @Override public BufferedImage render()   { return original; }
    @Override public String descripcion()     { return "Foto sin sellos"; }
}

// Decorador abstracto
public abstract class SelloDecorator implements Foto {
    protected final Foto fotoDecorada;

    protected SelloDecorator(Foto fotoDecorada) { this.fotoDecorada = fotoDecorada; }

    @Override public BufferedImage render()   { return fotoDecorada.render(); }
    @Override public String descripcion()     { return fotoDecorada.descripcion(); }
}

// Decorador concreto: el que exige RF-06
public class SelloFechaHora extends SelloDecorator {
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private final LocalDateTime momento;

    public SelloFechaHora(Foto fotoDecorada, LocalDateTime momento) {
        super(fotoDecorada);
        this.momento = momento;
    }

    @Override
    public BufferedImage render() {
        BufferedImage imagen = fotoDecorada.render();
        Graphics2D g = imagen.createGraphics();
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.setColor(Color.WHITE);
        g.drawString(momento.format(FORMATO), 24, imagen.getHeight() - 24);
        g.dispose();
        return imagen;
    }

    @Override
    public String descripcion() {
        return fotoDecorada.descripcion() + ", con fecha y hora";
    }
}

// Decorador concreto: deja constancia de la sede elegida
public class SelloSede extends SelloDecorator { /* análogo */ }
```

Uso desde [[patron-facade]]:

```java
Foto evidencia = new SelloSede(
                     new SelloFechaHora(new FotoBase(capturada), LocalDateTime.now()),
                     sede);
```

## Cuidados

- **El sello debe aplicarse en el servidor, no en el navegador.** Si se dibuja en el cliente,
  la trabajadora podría manipular la hora. El decorador vive en la capa que persiste.
- `render()` como está escrito **modifica la imagen recibida**. Conviene copiarla antes de
  dibujar, o el decorador de más adentro queda alterado.
- No abusar: tres sellos son suficientes. Una cadena de ocho decoradores sería
  Overengineering ([[s14-antipatrones]]).

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[patron-facade]] · [[patron-composite]]
