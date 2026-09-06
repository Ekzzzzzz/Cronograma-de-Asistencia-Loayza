---
titulo: Patrón Singleton
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx
  - Archivos_de_clase/S14_s1 - Antipatrones Concepto, Propósito.pptx
actualizado: 2026-09-06
tags: [creacional, gof, nucleo, spring]
---

# Patrón Singleton

**Familia:** creacional · **Sesión:** [[s06-singleton-prototype]] · **Capa:** Modelo (fijada
por PC-3) · **Requisito:** [[requisitos]] RF-03 y RC-03

El patrón con más letra chica del proyecto. Encaja, pero hay que aplicarlo con cuidado.

## Qué es

"Crea una clase que tenga una única instancia y proporcione un punto de acceso global"
(S06). Dos variedades: **instanciación temprana** (en la carga) y **perezosa** (solo cuando
se necesita). El ejemplo canónico del curso es `DBConnection`: sin Singleton se abren
conexiones redundantes.

El propio curso reconoce tres desventajas: es complicado en **entornos multihilo**, **viola
la responsabilidad única** y **dificulta las pruebas unitarias** por el estado global.

## Por qué encaja aquí

Dos usos legítimos:

1. **Catálogo de sedes.** [[requisitos]] RF-03 exige un desplegable con una lista **cerrada
   de 7 elementos** ([[sedes]]). Es un dato fijo, compartido y de solo lectura: cargarlo una
   vez y consultarlo desde toda la aplicación es el caso de libro.
2. **Conexión a base de datos.** RC-03 exige base de datos con stored procedures, y el
   ejemplo `DBConnection` del curso es literalmente esto.

## Aviso importante con Spring Boot

**Spring ya gestiona los beans como singletons por defecto.** Un `@Service` o `@Component`
tiene una sola instancia por contenedor, sin escribir `getInstance()`. Escribir el Singleton
clásico *encima* de Spring para las mismas clases sería redundante — y redundar en Singletons
es justo la **"Singletonitis"** que [[s14-antipatrones]] señala.

La salida limpia, y que además da material excelente para el informe:

- **`CatalogoSedes` con Singleton clásico**, escrito a mano. Es autocontenido, no necesita
  inyección de dependencias y demuestra el patrón tal como lo pide [[pc3-entregable]].
- **La conexión a base de datos se deja a Spring** (`DataSource` con pool), y en el informe
  se documenta que **Spring aplica el mismo patrón** por debajo. Comparar la versión manual
  con la del framework es exactamente el tipo de análisis que la entrevista de PC-3
  premia.

## Diseño propuesto

Versión perezosa y segura en multihilo, resolviendo la desventaja que advierte S06 mediante
el *holder idiom* (la JVM garantiza que la clase interna se cargue una sola vez):

```java
public final class CatalogoSedes {

    private final List<Sede> sedes;

    private CatalogoSedes() {                 // constructor privado
        this.sedes = List.of(
            new Sede("Los Olivos",  "2W5M+M4", -11.990812, -77.067188),
            new Sede("La Molina",   "W2JR+RG", -12.067937, -76.958687),
            new Sede("San Borja",   "VXRX+RG", -12.107938, -77.001188),
            new Sede("Lince",       "WX87+CM", -12.083937, -77.035812),
            new Sede("San Miguel",  "WWF4+47", -12.077187, -77.094313),
            new Sede("Surco",       "R2X6+FC", -12.151312, -76.988937),
            new Sede("Miraflores",  "VXHC+JF", -12.120938, -77.028813));
    }

    private static class Holder {
        private static final CatalogoSedes INSTANCIA = new CatalogoSedes();
    }

    public static CatalogoSedes getInstancia() {
        return Holder.INSTANCIA;
    }

    public List<Sede> todas() { return sedes; }

    public Sede porNombre(String nombre) {
        return sedes.stream()
                .filter(s -> s.nombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new SedeDesconocida(nombre));
    }
}
```

`List.of(...)` devuelve una lista inmutable: nadie puede alterar el catálogo desde fuera, lo
que neutraliza buena parte del riesgo de estado global.

> Coordenadas tomadas de [[sedes]], obtenidas ejecutando `tools/pluscode.py`.

## Cuidados

- **Un solo Singleton en todo el sistema.** Si aparece un segundo, revisar si de verdad hace
  falta o si es Singletonitis.
- El estado global dificulta las pruebas (S06). Aquí se mitiga porque el catálogo es
  **inmutable y sin dependencias**: no hay estado que ensuciar entre pruebas.
- No meterle lógica de negocio. Es un catálogo, no un servicio.

## Enlaces

[[mapa-patron-requisito]] · [[sedes]] · [[s14-antipatrones]] · [[patron-facade]]
