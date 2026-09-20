---
titulo: Patrón State
tipo: patron
estado: obsoleto
fuentes:
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [comportamiento, gof, descartado, antipatrones]
---

# Patrón State

**Familia:** comportamiento · **Sesión:** [[s12-state-observer]] · **Capa:** Modelo ·
**Requisito:** [[requisitos]] RF-09 y RF-12

> ## ⚠️ Patrón descartado (2026-09-06)
>
> **Esta página se conserva como evidencia del descarte, no como diseño a implementar.**
>
> State entró al núcleo con una justificación concreta: resolvía la entrada sin salida **y**
> los tres marcadores del Excel, y esta misma página argumentaba que "contando todo son siete
> estados, así que la advertencia de S12 sobre pocos estados no aplica".
>
> El 2026-09-06 el usuario fijó que `DESCANSO`, `NO TURNO`, `INASISTENCIA` y las tardanzas
> **se manejan a mano fuera del sistema**, y que la celda del cronograma solo lleva horas. Eso
> borró cinco de los siete estados. Quedan dos —turno abierto y turno cerrado— y
> [[s12-state-observer]] advierte que State "puede ser excesivo si solo hay pocos estados".
>
> Por el criterio del proyecto —**¿el código queda más limpio con el patrón que sin él?**—
> la respuesta pasó a ser no: un `salida == null` dentro de `Turno` es más claro que dos
> clases de estado con transiciones. Conservarlo por inercia habría sido **Golden Hammer**
> ([[s14-antipatrones]]).
>
> **Su lugar en el núcleo lo ocupa [[patron-observer]]**, que mantiene la familia de
> comportamiento que exige [[pc3-entregable]] con una justificación más sólida.
>
> Razón completa en [[adr-003-login-y-cronograma-simple]]. El diseño vigente del turno está
> en [[marcacion-multiple]].
>
> **Por qué vale la pena leerla igual:** es el único patrón del proyecto que se descartó
> *después* de haberse aceptado, porque un cambio de requisitos le quitó el sustento. Los
> otros siete se descartaron en el análisis inicial. Eso lo convierte en el mejor material
> para el capítulo de antipatrones con evidencias que pide PC-3 (RC-06), y en una buena
> respuesta para la entrevista.

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

## Qué pasó con el hueco que esta página iba a cerrar

Todo lo de arriba está **superado**. Se conserva tal como se escribió porque el valor de esta
página es documental.

Distinguir `DESCANSO` de `NO TURNO` e `INASISTENCIA` requería un cronograma planificado que el
sistema no tenía. Durante unas horas del 2026-09-06 la solución fue una tabla
`turno_planificado` en [[modelo-datos]] y una pantalla para llenarla.

**Esa solución nunca se construyó.** El usuario cerró el hueco por la vía más barata: esos
casos se manejan **a mano, fuera del sistema**. Con ellos desaparecieron la tabla, la pantalla
y los cinco estados que sostenían este patrón.

Es la lección que conviene contar en el informe: **el hueco no se cerró con más diseño, sino
con menos alcance**, y el patrón que se había aceptado para cubrirlo dejó de tener trabajo.

## Enlaces

[[adr-003-login-y-cronograma-simple]] · [[patron-observer]] · [[marcacion-multiple]] ·
[[mapa-patron-requisito]] · [[s14-antipatrones]]
