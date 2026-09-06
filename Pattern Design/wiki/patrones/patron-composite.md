---
titulo: Patrón Composite
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S09_s1-Patrones-Estructurales-DC.pptx
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-06
tags: [estructural, gof, nucleo]
---

# Patrón Composite

**Familia:** estructural · **Sesión:** [[s09-decorator-composite]] · **Capa:** sin asignar
por PC-3; se propone **Modelo** · **Requisito:** [[requisitos]] RF-12

## Qué es

Trata de manera uniforme objetos individuales y composiciones, en relaciones **parte-todo**.
S09 lo describe como "una generalización del patrón [[patron-decorator]] con una colección
agregada de instancias de Component": Decorator ofrece composición *vertical* simple;
Composite, una jerarquía completa.

El ejemplo del curso es clínico —`Treatment`, `SingleTreatment`, `CompositeTreatment`— y
sirve casi sin adaptación.

## Por qué encaja aquí

[[formato-cronograma-excel]] demuestra que **la celda de jornada es una agregación, no un
dato**: `10:12AM - 8:46PM` es el resultado de emparejar dos marcaciones. Y con
[[requisitos]] RF-09 (marcación múltiple) una jornada puede tener más de un par.

La jerarquía del cronograma es literalmente parte-todo:

```
SemanaSede            (compuesto — una pestaña del dashboard, una tabla de Excel)
└── DiaSede           (compuesto — una columna: LUNES 25)
    └── Jornada       (compuesto — una celda: 10:12AM - 8:46PM)
        └── Marcacion (hoja — una entrada o una salida con su foto)
```

Que el cliente pueda pedir `horasTrabajadas()` a cualquier nivel sin saber si habla con una
hoja o con un compuesto es exactamente lo que el patrón resuelve.

## Diseño propuesto

```java
public interface ComponenteCronograma {
    Duration horasTrabajadas();
    String  descripcion();   // lo que termina en la celda del Excel
}

// Hoja
public class Marcacion implements ComponenteCronograma {
    private final TipoMarcacion tipo;      // ENTRADA | SALIDA
    private final LocalDateTime momento;

    @Override public Duration horasTrabajadas() { return Duration.ZERO; }
    @Override public String descripcion() {
        return momento.format(DateTimeFormatter.ofPattern("h:mma"));
    }
}

// Compuesto
public class Jornada implements ComponenteCronograma {
    private final List<ComponenteCronograma> marcaciones = new ArrayList<>();

    public void agregar(ComponenteCronograma c) { marcaciones.add(c); }

    @Override
    public Duration horasTrabajadas() {
        return marcaciones.stream()
                .map(ComponenteCronograma::horasTrabajadas)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public String descripcion() {   // "10:12AM - 8:46PM"
        return primeraEntrada() + " - " + ultimaSalida();
    }
}

// SemanaSede y DiaSede repiten la misma estructura de agregación
```

`horasTrabajadas()` se propaga hacia arriba igual que `cost()` recorre la lista en el
`CompositeTreatment` del curso.

## Cuidados

- **`Marcacion` devuelve `Duration.ZERO`**: una marcación suelta no tiene duración, la
  duración nace del par. Es el compromiso normal de Composite — la hoja implementa
  operaciones que no le corresponden del todo.
- **El emparejamiento entrada–salida no es trivial** con marcación múltiple entre sedes. Ver
  el hueco en [[formato-cronograma-excel]].
- Los estados `DESCANSO`, `NO TURNO` e `INASISTENCIA` **no** salen de esta jerarquía: son
  ausencia de marcaciones. Los resuelve [[patron-state]].

## Enlaces

[[mapa-patron-requisito]] · [[formato-cronograma-excel]] · [[patron-state]] ·
[[patron-decorator]]
