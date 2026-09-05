---
tipo: dominio
titulo: Sedes — direcciones, coordenadas y geocerca
tags: [dominio, sedes, geocerca, antifraude]
creado: 2026-09-04
actualizado: 2026-09-05
estado: activo
---

# Sedes — direcciones, coordenadas y geocerca

Datos aportados por el usuario los días 2026-09-04 y 2026-09-05. Coordenadas derivadas de
los Plus Codes por decodificación directa *(cálculo del agente, verificable con
`python tools/pluscode.py`)*.

> [!note] Corrección registrada
> El enunciado inicial hablaba de 8 grupos de WhatsApp. **Son 7 sedes**
> *(aporte del usuario, 2026-09-04)*. Propagado a [[problema-cronogramas]], [[antifraude]],
> [[adr-002-canal-de-marcacion]] y `CLAUDE.md` §8.

## Las 7 sedes

Razón social en todas: **Podología y Estética Loayza**.

| Sede | Dirección | Piso | Plus Code | Latitud | Longitud |
|---|---|:--:|---|---:|---:|
| **Los Olivos** | Av. Carlos Izaguirre 525 | 1 | `2W5M+M4` | **-11.990812** | **-77.067188** |
| **La Molina** | Av. La Molina 744, consultorio 202 *(frente a Farmacia Universal)* | 2 | `W2JR+RG` | **-12.067937** | **-76.958687** |
| **San Borja** | Av. Aviación 3550, consultorio 202 | 2 | `VXRX+RG` | **-12.107938** | **-77.001188** |
| **Lince** | Av. Arenales 1912 | **12** | `WX87+CM` | **-12.083937** | **-77.035812** |
| **San Miguel** | Av. La Marina 2889, consultorio 202 *(frente a la UPC)* | 2 | `WWF4+47` | **-12.077187** | **-77.094313** |
| **Surco** | Av. Los Próceres 502, Santiago de Surco *(frente al ICPNA)* | 1 | `R2X6+FC` | **-12.151312** | **-76.988937** |
| **Miraflores** | Av. José Larco 345, Galería Multicentro, consultorio 604 *(frente al Parque Kennedy)* | **6** | `VXHC+JF` | **-12.120938** | **-77.028813** |

**Las 7 resueltas.** Ninguna queda pendiente.

## Cómo se obtuvieron las coordenadas

Un Plus Code (Open Location Code) **no es una referencia opaca**: codifica la posición en el
propio texto y se decodifica con un algoritmo público, sin consultar ningún servicio.

Los códigos cortos que diste (`2W5M+M4`) omiten los 4 primeros caracteres, que se recuperan
a partir de una referencia cercana — aquí, el centro de Lima. Todas las sedes comparten el
mismo prefijo recuperado: `57V4`. El código completo de Miraflores, por ejemplo, es
`57V4VXHC+JF`.

El decodificador está en `tools/pluscode.py`. Se validó contra un Plus Code conocido (la
sede de Google en Zúrich) antes de usarlo, con acierto dentro de la celda. Ejecutarlo
reimprime la tabla y las distancias.

**Precisión: la celda de un código de 10 dígitos mide unos 14 m.** La coordenada apunta al
edificio, que es exactamente lo que necesita una geocerca.

## Dotación por sede

*(aporte del usuario, 2026-09-05)*

| Rol | Por sede |
|---|---|
| **Cajeras** | 2 a 3, como máximo |
| **Podólogas** | de 3 hasta 15, entre *full time* y *part time* |

**Ambos roles van en el mismo cronograma de asistencias.** El Excel actual sólo lista
podólogas ([[formato-cronograma-actual]]); el sistema las unifica en una sola rejilla por
sede, distinguiéndolas por rol.

Dos consecuencias de diseño *(propuesta del agente)*:

- **La rejilla es muy desigual entre sedes.** Una sede puede tener 5 filas y otra 18. El
  exportador no puede asumir un alto fijo, y el panel debe seguir siendo legible en ambos
  extremos.
- **La distinción *full time* / *part time* ya está en los datos actuales**: los turnos
  `10AM-8PM` (completo), `3PM-8PM` (tarde) y `10AM - 3PM` (medio) del primer bloque del
  Excel, y las abreviaturas `TC` y `MT` de las notas. Es información del padrón, no de la
  marcación.

## Distancias entre sedes

|  | Olivos | Molina | S. Borja | Lince | S. Miguel | Surco | Mirafl. |
|---|---:|---:|---:|---:|---:|---:|---:|
| **Los Olivos** | — | 14 587 | 14 870 | 10 903 | 10 047 | 19 771 | 15 059 |
| **La Molina** | 14 587 | — | 6 414 | 8 573 | 14 783 | 9 837 | 9 637 |
| **San Borja** | 14 870 | 6 414 | — | 4 615 | 10 687 | 5 004 | **3 333** |
| **Lince** | 10 903 | 8 573 | 4 615 | — | 6 405 | 9 061 | 4 184 |
| **San Miguel** | 10 047 | 14 783 | 10 687 | 6 405 | — | 14 113 | 8 624 |
| **Surco** | 19 771 | 9 837 | 5 004 | 9 061 | 14 113 | — | 5 495 |
| **Miraflores** | 15 059 | 9 637 | **3 333** | 4 184 | 8 624 | 5 495 | — |

En metros. Las tres parejas más cercanas: **San Borja–Miraflores 3,3 km**, Lince–Miraflores
4,2 km, San Borja–Lince 4,6 km. La más lejana, Los Olivos–Surco, a 19,8 km.

## Consecuencia principal: la geocerca puede ser generosa

Es el hallazgo más útil de este análisis *(propuesta del agente)*.

Con **3,3 km** de separación mínima, ninguna geocerca razonable puede confundir dos sedes.
El radio podría llegar a ~1,6 km antes de que dos cercas se tocaran. Ese margen hace falta,
porque:

> [!important] Cinco de las siete sedes están en pisos altos
> Sólo Los Olivos y Surco están a pie de calle. Las otras cinco están en el piso 2 o más
> arriba, con dos casos extremos: **Miraflores en el piso 6 de una galería** y **Lince en el
> piso 12**.
>
> El GPS necesita vista al cielo. En interiores la señal se atenúa y el navegador cae a
> posicionamiento por Wi-Fi y antenas de telefonía, que en zonas densas de Lima suele dar
> entre 20 y 150 m de error — a veces más, en una galería con estructura metálica.
>
> Una geocerca estrecha —50 m, digamos— **rechazaría marcaciones legítimas todos los días**
> en Miraflores y en Lince. Y rechazar es peor que revisar ([[antifraude]], principio 3).

**Radio propuesto: 200 m para todas las sedes.** Amplio para absorber el error de
posicionamiento en interiores, y a un orden de magnitud de distancia de la ambigüedad entre
sedes.

Como todas están en avenidas principales de Lima —Izaguirre, La Molina, Aviación, Arenales,
La Marina, Los Próceres, Larco—, la cobertura de Wi-Fi y antenas es densa, lo que juega a
favor del posicionamiento cuando el GPS no alcanza.

## Regla de geocerca sensible a la precisión

Un radio fijo no basta: el navegador informa además de la **precisión** de la lectura
(`coords.accuracy`, en metros). Comprobar «¿está a menos de 200 m?» no significa nada si la
propia lectura declara 500 m de error.

Regla propuesta *(propuesta del agente, pendiente de ADR)*:

| Situación | Veredicto |
|---|---|
| `distancia + precisión ≤ radio` | **Dentro** con seguridad |
| `distancia − precisión ≤ radio` pero no lo anterior | **Plausible**: se acepta, se anota la duda |
| `distancia − precisión > radio` | **Fuera**: no se rechaza, va a revisión |
| `precisión > 500 m` o sin señal | **Indeterminado**: va a revisión |

Ninguna de las cuatro filas rechaza automáticamente. La geocerca es **una señal más** dentro
de la cadena de verificaciones, no un portero.

**Ajuste por sede, no global:** el radio es configuración por sede, no una constante. Si
Miraflores o Lince resultan problemáticas en la práctica, se sube el suyo sin tocar las
demás. Conviene medir la precisión real que reportan los dispositivos durante las primeras
semanas y calibrar con esos datos en vez de adivinar.

## Verificación pendiente

Las coordenadas se derivaron de los Plus Codes que diste, así que apuntan a donde apunten
esos códigos. Dos contrastes rápidos dan confianza:

- **San Miguel** (`-12.077187, -77.094313`) cae sobre Av. La Marina a la altura de la UPC,
  como dice la dirección.
- **Miraflores** (`-12.120938, -77.028813`) cae sobre Av. Larco junto al Parque Kennedy.

Aun así, **conviene comprobar una en el mapa** antes de fijarlas como configuración del
sistema: pegar las coordenadas en Google Maps y confirmar el edificio.

## Enlaces

- [[antifraude]] — la geocerca es una de las 7 verificaciones
- [[problema-cronogramas]] · [[formato-cronograma-actual]] · [[dominio-moc]]
