---
titulo: Patrón Proxy
tipo: patron
estado: borrador
fuentes:
  - Archivos_de_clase/S11_s1 - Patrones estrucuturales_Proxy_Bridge (2)_EIHIXN.pptx
  - Archivos_de_clase/S15_s1s2 - PC-3-DPA.pdf
actualizado: 2026-09-06
tags: [estructural, gof, nucleo, spring]
---

# Patrón Proxy

**Familia:** estructural · **Sesión:** [[s11-proxy-bridge]] · **Capa:** Vista (fijada por
PC-3) · **Requisito:** [[requisitos]] RF-10 y RF-11

## Qué es

"Proporciona un sustituto o marcador de posición para otro objeto **para controlar el
acceso** a él". S11 distingue cuatro tipos; dos nos sirven:

| Tipo | Para qué |
|---|---|
| **Virtual Proxy** | Retrasa la creación y carga del objeto hasta que sea necesario |
| **Protection Proxy** | Controla el acceso, con permisos distintos por usuario |

Frente a [[patron-decorator]], que es estructuralmente igual: en Decorator al cliente le
interesa *lo que se agrega*; en Proxy, *el objeto agregado*.

## Por qué encaja aquí

Encaja por partida doble, con dos necesidades **realmente distintas** — no es el mismo
patrón estirado dos veces:

1. **Protection Proxy para el dashboard.** [[requisitos]] RF-01 dice que la trabajadora entra
   por un enlace general **sin login**. El dashboard de administradora (RF-11) no puede tener
   esa misma puerta abierta: expone fotos y horarios de todo el personal.
2. **Virtual Proxy para las fotos.** RF-10 guarda una foto por marcación. Con 7 sedes y
   varias marcaciones diarias por trabajadora, un mes son miles de imágenes. El dashboard
   debe listar las marcaciones **sin cargar las imágenes**, y traer cada una solo al abrirla.

## Diseño propuesto

```java
public interface RepositorioFotos {
    BufferedImage obtener(long idFoto);
}

// Implementación real: va al disco o a la base de datos
public class RepositorioFotosReal implements RepositorioFotos {
    @Override
    public BufferedImage obtener(long idFoto) { /* lectura costosa */ }
}

// Virtual Proxy: no toca el almacenamiento hasta que alguien pide de verdad la imagen
public class RepositorioFotosProxy implements RepositorioFotos {

    private final RepositorioFotosReal real;
    private final Map<Long, BufferedImage> cache = new ConcurrentHashMap<>();

    @Override
    public BufferedImage obtener(long idFoto) {
        return cache.computeIfAbsent(idFoto, real::obtener);
    }
}
```

Y el de protección sobre el dashboard:

```java
public class DashboardProtectionProxy implements ServicioDashboard {

    private final ServicioDashboard real;
    private final SesionAdministradora sesion;

    @Override
    public List<MarcacionResumen> marcacionesDe(Sede sede, LocalDate dia) {
        if (!sesion.esAdministradora())
            throw new AccesoDenegado("Solo la administradora puede ver el dashboard.");
        return real.marcacionesDe(sede, dia);
    }
}
```

## Nota sobre Spring Boot

**Spring usa Proxy internamente**: `@Transactional`, `@Cacheable` y Spring Security funcionan
creando proxies dinámicos alrededor de los beans. Sería perfectamente válido resolver el
control de acceso con `@PreAuthorize` y la caché con `@Cacheable`.

Para [[pc3-entregable]] conviene **escribir los proxies a mano**, porque el informe pide
capturas de código que demuestren el patrón y una anotación no lo muestra. Pero mencionar en
el informe que el framework aplica el mismo patrón por debajo es un buen punto para la
entrevista.

## Cuidados

- El caché del Virtual Proxy **crece sin límite** tal como está escrito. Para producción hay
  que acotarlo por tamaño o tiempo.
- S11 advierte que Proxy añade indirección, complica la depuración y puede introducir
  latencia. Con dos proxies bien delimitados el costo es asumible.
- **El Protection Proxy no sustituye a la autenticación real.** Es la puerta, no la
  cerradura: hace falta decidir cómo se autentica la administradora — pregunta 2 de
  [[huecos-abiertos]].

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[patron-decorator]] · [[huecos-abiertos]]
