---
titulo: Patrón Observer
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S12_s1 - Patrones comportamiento_State_Observer_DPA.pptx
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
  - decisiones del usuario (2026-09-06)
actualizado: 2026-09-06
tags: [comportamiento, gof, nucleo, spring]
---

# Patrón Observer

**Familia:** comportamiento · **Sesión:** [[s12-state-observer]] · **Capa:** Vista (fijada
por PC-3) · **Requisito:** [[requisitos]] RF-11 y RF-12

**El único patrón de comportamiento del proyecto.** Sube de la periferia al núcleo en
[[adr-003-login-y-cronograma-simple]], al descartarse [[patron-state]].

## Qué es

Define una dependencia uno-a-muchos entre objetos, de modo que cuando uno cambia de estado
**todos sus dependientes son notificados y actualizados automáticamente** (S12). Los papeles
son `Subject` —que mantiene la lista de observadores y notifica— y `Observer`, que reacciona.

## Por qué encaja aquí

Registrar una marcación dispara **dos efectos que no tienen nada que ver entre sí**:

1. **Recalcular la jornada del día**, para que `jornada_diaria` refleje la nueva hora de
   entrada o salida ([[modelo-datos]]).
2. **Refrescar el dashboard** de la administradora, que ahora es una pantalla real detrás de
   login ([[adr-003-login-y-cronograma-simple]]).

Sin Observer, [[patron-facade]] tendría que conocer a los dos y llamarlos en orden. Esa
fachada empezaría a acumular responsabilidades ajenas al registro, que es exactamente el
*Big Ball of Mud* contra el que su propia página advierte. Y cada efecto nuevo —enviar un
aviso, escribir una auditoría— obligaría a volver a tocarla.

**Con Observer la fachada publica un hecho y se desentiende de quién lo escucha.** Es la
diferencia entre "registrar y además hacer estas tres cosas" y "registrar".

## Diseño propuesto

Versión manual, la que va al informe porque [[pc3-entregable]] pide capturas de código que
muestren el patrón:

```java
public interface ObservadorMarcacion {
    void alRegistrar(Marcacion marcacion);
}

public class PublicadorMarcaciones {                    // Subject

    private final List<ObservadorMarcacion> observadores = new CopyOnWriteArrayList<>();

    public void suscribir(ObservadorMarcacion observador) {
        observadores.add(observador);
    }

    public void notificar(Marcacion marcacion) {
        for (ObservadorMarcacion o : observadores) {
            o.alRegistrar(marcacion);
        }
    }
}

// Observador 1: mantiene al día la tabla resumen
public class RecalculadorJornada implements ObservadorMarcacion {

    private final RepositorioJornadas jornadas;

    @Override
    public void alRegistrar(Marcacion marcacion) {
        Jornada jornada = jornadas.cargar(
                marcacion.trabajadora(), marcacion.sede(), marcacion.fecha());
        jornada.agregar(marcacion);
        jornadas.guardar(jornada);
    }
}

// Observador 2: avisa al dashboard abierto
public class NotificadorDashboard implements ObservadorMarcacion {

    @Override
    public void alRegistrar(Marcacion marcacion) {
        // empuja el cambio a las pestañas abiertas de esa sede
    }
}
```

`CopyOnWriteArrayList` en vez de `ArrayList`: la lista se lee en cada notificación y se
escribe casi nunca, y varias marcaciones pueden llegar a la vez desde sedes distintas.

## Nota sobre Spring Boot

Spring trae el patrón implementado: `ApplicationEventPublisher` publica y `@EventListener`
suscribe, sin escribir el `Subject` a mano.

```java
publicador.publishEvent(new MarcacionRegistrada(marcacion));   // en la fachada

@EventListener
public void alRegistrar(MarcacionRegistrada evento) { ... }    // en cada observador
```

Mismo criterio que [[patron-singleton]] y [[patron-proxy]] en
[[adr-001-patrones-seleccionados]]: **se escribe la versión manual para el informe**, y se
documenta que el framework aplica el mismo patrón por debajo. Comparar ambas es buen material
para la entrevista.

## Cuidados

- **El recálculo de la jornada debe ocurrir en la misma transacción que la inserción.** Si el
  observador falla después de confirmar la marcación, `jornada_diaria` queda desincronizada.
  Con Spring, `@TransactionalEventListener(phase = BEFORE_COMMIT)` lo resuelve.
- **Un observador lento bloquea el registro** si la notificación es síncrona. El aviso al
  dashboard puede ser asíncrono; el recálculo, no.
- S12 advierte que Observer **dificulta seguir el flujo**: el código deja de leerse en orden.
  Con dos observadores el costo es bajo; con diez, el sistema se vuelve impredecible.
- No convertirlo en el bus de eventos de todo el sistema. Publica un hecho, no cualquier cosa.

## Enlaces

[[mapa-patron-requisito]] · [[adr-003-login-y-cronograma-simple]] · [[patron-facade]] ·
[[modelo-datos]] · [[s12-state-observer]]
