---
titulo: Sedes
tipo: dominio
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
  - tools/pluscode.py
actualizado: 2026-09-05
tags: [dominio, sedes]
---

# Sedes

La empresa opera **7 sedes** en Lima. La lista y los Plus Codes vienen de la constante
`SEDES` en `tools/pluscode.py`; el brief del usuario confirma el número (7) pero no los
nombres, así que los nombres se toman del script.

| Sede | Plus Code | Código completo | Latitud | Longitud |
|---|---|---|---|---|
| Los Olivos | `2W5M+M4` | `57V42W5M+M4` | -11.990812 | -77.067188 |
| La Molina | `W2JR+RG` | `57V4W2JR+RG` | -12.067937 | -76.958687 |
| San Borja | `VXRX+RG` | `57V4VXRX+RG` | -12.107938 | -77.001188 |
| Lince | `WX87+CM` | `57V4WX87+CM` | -12.083937 | -77.035812 |
| San Miguel | `WWF4+47` | `57V4WWF4+47` | -12.077187 | -77.094313 |
| Surco | `R2X6+FC` | `57V4R2X6+FC` | -12.151312 | -76.988937 |
| Miraflores | `VXHC+JF` | `57V4VXHC+JF` | -12.120938 | -77.028813 |

Coordenadas obtenidas ejecutando `python tools/pluscode.py` (2026-09-05). Los Plus Codes son
cortos y se resuelven contra una referencia en el centro de Lima (`REF_LAT = -12.05`,
`REF_LON = -77.04`). Cada celda tiene ~14 m de lado, así que la precisión es de nivel
edificio. El script se autovalida decodificando un código conocido de Zúrich con un error de
~3 m.

> **Hueco:** faltan las direcciones postales reales de las sedes. Las coordenadas están,
> pero nadie ha confirmado que apunten a la puerta correcta de cada local.

## Separación entre sedes

Las dos sedes más cercanas son **San Borja y Miraflores, a 3 333 m**. La más lejana de todas
es Los Olivos, en el norte, a 19,8 km de Surco.

> **Inferencia:** con 3,3 km de separación mínima y ~14 m de precisión, identificar en qué
> sede está una trabajadora por GPS sería inequívoco: no hay ambigüedad posible entre dos
> sedes. Esto hace **técnicamente viable** la validación por ubicación de la pregunta 4 de
> [[huecos-abiertos]], aunque el brief no la pida.

## Por qué importan

- **[[requisitos]] RF-03**: la lista alimenta el desplegable de selección de sede. Es una
  lista cerrada de 7 elementos, no texto libre.
- **[[requisitos]] RF-11**: el dashboard tiene exactamente una pestaña por sede.
- **[[requisitos]] RF-12/RF-13**: se genera una tabla de cronograma por sede. El ejemplo
  disponible en `docs/Cronograma_Ejemplo.xlsx` es el de Los Olivos.

## Tensiones

Ninguna registrada todavía.

> **Inferencia:** que exista un script con las coordenadas de las 7 sedes sugiere que se
> evaluó validar la ubicación de la marcación por GPS. El brief no lo pide. Anotado como
> decisión abierta en [[huecos-abiertos]]; no se implementa hasta que el usuario lo pida.
