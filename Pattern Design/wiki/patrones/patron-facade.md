---
titulo: Patrón Facade
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S08_s1-Patrones-Estructurales-AF.pptx
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-06
tags: [estructural, gof, nucleo]
---

# Patrón Facade

**Familia:** estructural · **Sesión:** [[s08-adapter-facade]] · **Capa:** Control (fijada
por PC-3) · **Requisito:** [[requisitos]] RF-05 a RF-08

## Qué es

Crea "una interfaz más unificada para un sistema más complejo", simplificando el acceso
mediante **un único punto de entrada**. El curso lo resume así frente a Adapter: **Facade
simplifica, Adapter traduce**.

El caso práctico de S08 es un **hospital** que integra registros médicos, laboratorio y
facturación tras una sola fachada, "sin que los usuarios interactúen directamente con cada
sistema".

## Por qué encaja aquí

Enviar una marcación dispara cinco cosas: validar la trabajadora y la sede, crear la
marcación ([[patron-factory]]), sellar la foto ([[patron-decorator]]), persistir, y notificar
al dashboard ([[patron-observer]] si entra). El controlador web **no debería conocer ese
orden**, y menos con [[requisitos]] RNF-01 exigiendo simplicidad: un solo botón "enviar" del
lado de la usuaria debería ser una sola llamada del lado del código.

## Diseño propuesto

```java
public class RegistrarMarcacionFacade {

    private final CatalogoSedes      catalogo;      // patron-singleton
    private final RepositorioMarcaciones repositorio;
    private final RepositorioFotos   fotos;

    public ResultadoMarcacion registrar(SolicitudMarcacion solicitud) {

        // 1. Resolver sede contra el catálogo cerrado de 7
        Sede sede = catalogo.porNombre(solicitud.nombreSede());

        // 2. Sellar la evidencia — la hora la pone el servidor, no el cliente
        Foto evidencia = new SelloSede(
                             new SelloFechaHora(new FotoBase(solicitud.imagen()),
                                                LocalDateTime.now()),
                             sede);

        // 3. Crear la marcación del tipo correcto
        Marcacion marcacion = MarcacionFactory.crear(
                solicitud.tipo(), solicitud.trabajadora(), sede,
                evidencia, solicitud.notas());

        // 4. Validar contra la jornada abierta en esa sede
        Jornada jornada = repositorio.jornadaDelDia(
                solicitud.trabajadora(), sede, LocalDate.now());
        marcacion.validar(jornada);

        // 5. Persistir imagen y registro
        long idFoto = fotos.guardar(evidencia.render());
        repositorio.guardar(marcacion, idFoto);

        return ResultadoMarcacion.exito(marcacion);
    }
}
```

El controlador de la capa Vista queda reducido a una línea:

```java
return facade.registrar(solicitud);
```

## Cuidados

- **Facade no es una clase-Dios.** Coordina, no implementa: cada paso delega en su
  colaborador. Si empieza a acumular lógica de negocio propia, se convierte en el
  *Big Ball of Mud* de [[s14-antipatrones]].
- El paso 2 usa `LocalDateTime.now()` **del servidor** a propósito. Es la garantía de que el
  sello de RF-06 no se puede falsificar desde el teléfono.
- Con marcación múltiple ([[requisitos]] RF-09), `jornadaDelDia` debe filtrar **por sede**,
  no solo por trabajadora y fecha.

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[patron-factory]] · [[patron-decorator]] ·
[[patron-singleton]]
