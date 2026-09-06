---
titulo: Patrón State
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [comportamiento, gof, nucleo, no-obligatorio]
---

# Patrón State

**Familia:** comportamiento · **Sesión:** [[s12-state-observer]] · **Capa:** Modelo ·
**Requisito:** [[requisitos]] RF-09 y RF-12

**No lo exige [[pc3-entregable]]**, y aun así entra al núcleo: resuelve un problema que
ningún patrón obligatorio cubre.

## Qué es

Permite a un objeto "cambiar su comportamiento cuando cambia su estado interno. El objeto
parecerá cambiar su clase" (S12). Se implementa con una interfaz `State`, clases concretas
por estado, y un `Context` que **delega** las solicitudes al estado actual.

S12 advierte que "puede ser excesivo si solo hay pocos estados".

## Por qué encaja aquí

Dos problemas abiertos que se resuelven con el mismo mecanismo:

1. **Entrada sin salida** (pregunta 5 de [[huecos-abiertos]]): si una trabajadora marca
   entrada y nunca la salida, ¿qué muestra el cronograma? Es un estado, no un error.
2. **Los tres marcadores del Excel.** [[formato-cronograma-excel]] distingue `DESCANSO`,
   `NO TURNO` e `INASISTENCIA`, y ninguno se deduce de las marcaciones: los tres son
   "ausencia de marcaciones" con significados distintos.

Contando todo son **siete estados**, así que la advertencia de S12 sobre pocos estados no
aplica. Al contrario: sin State, esto termina siendo una cadena de `if` sobre banderas — el
código espagueti que [[s14-antipatrones]] señala.

## Los estados

```
SIN_INICIAR ──marcar entrada──► ABIERTA ──marcar salida──► CERRADA
     │                             │
     │                             └──cierre del día sin salida──► INCOMPLETA
     │
     └── sin marcaciones y con turno asignado ──► INASISTENCIA
     └── sin marcaciones y con descanso        ──► DESCANSO
     └── sin marcaciones y sin turno           ──► NO_TURNO
```

## Diseño propuesto

```java
public interface EstadoJornada {
    EstadoJornada registrar(Jornada jornada, Marcacion marcacion);
    String etiquetaCronograma(Jornada jornada);   // lo que va a la celda del Excel
}

public class SinIniciar implements EstadoJornada {
    @Override
    public EstadoJornada registrar(Jornada jornada, Marcacion marcacion) {
        if (marcacion.tipo() != TipoMarcacion.ENTRADA)
            throw new MarcacionInvalida("No hay una entrada previa que cerrar.");
        jornada.agregar(marcacion);
        return new Abierta();
    }

    @Override
    public String etiquetaCronograma(Jornada jornada) { return "NO TURNO"; }
}

public class Abierta implements EstadoJornada {
    @Override
    public EstadoJornada registrar(Jornada jornada, Marcacion marcacion) {
        if (marcacion.tipo() == TipoMarcacion.ENTRADA)
            throw new MarcacionInvalida("Ya hay una entrada sin salida en esta sede.");
        jornada.agregar(marcacion);
        return new Cerrada();
    }

    @Override
    public String etiquetaCronograma(Jornada jornada) { return "SIN SALIDA"; }
}

public class Cerrada implements EstadoJornada {
    @Override
    public EstadoJornada registrar(Jornada jornada, Marcacion marcacion) {
        // Marcación múltiple: se abre un turno nuevo en la misma sede
        if (marcacion.tipo() != TipoMarcacion.ENTRADA)
            throw new MarcacionInvalida("No hay una entrada previa que cerrar.");
        jornada.agregar(marcacion);
        return new Abierta();
    }

    @Override
    public String etiquetaCronograma(Jornada jornada) {
        return jornada.descripcion();     // "10:12AM - 8:46PM"
    }
}

// Descanso, NoTurno e Inasistencia son estados terminales sin marcaciones
```

El `Context` es la propia `Jornada`, que delega:

```java
public class Jornada implements ComponenteCronograma {
    private EstadoJornada estado = new SinIniciar();

    public void registrar(Marcacion marcacion) {
        this.estado = estado.registrar(this, marcacion);
    }

    @Override
    public String descripcionCelda() { return estado.etiquetaCronograma(this); }
}
```

## Cómo se conecta con lo demás

- La validación que en [[patron-factory]] vive en `Marcacion.validar()` **se puede mover
  aquí**. Decidir dónde queda: duplicarla en ambos sitios sería *Shotgun Surgery*.
- `etiquetaCronograma()` es lo que [[patron-composite]] necesita para llenar la celda, y
  cierra el hueco que esa página dejaba abierto.

## Hueco que sigue abierto

Distinguir `DESCANSO` de `NO TURNO` e `INASISTENCIA` **requiere un cronograma planificado**
que hoy no existe en los requisitos. State provee la estructura; falta el dato. Pregunta 3
de [[huecos-abiertos]].

## Enlaces

[[mapa-patron-requisito]] · [[patron-composite]] · [[formato-cronograma-excel]] ·
[[huecos-abiertos]]
