---
titulo: Los patrones en cristiano
tipo: sintesis
estado: estable
fuentes:
  - Archivos_de_clase/S06_s1-Patrones-Creacionales-SP_DPA.pptx
  - Archivos_de_clase/S14_s1-Antipatrones_DPA.pptx
  - decisiones del usuario (2026-09-08)
actualizado: 2026-09-08
tags: [sintesis, patrones, informe, entrevista, divulgacion]
---

# Los patrones en cristiano

Explicación de cada patrón **sin jerga**, pensada para quien recién entra al curso. Pedida por
el usuario el 2026-09-08 para la presentación de avance.

[[mapa-patron-requisito]] dice *cuáles* patrones usamos y a qué requisito sirve cada uno;
[[cuatro-capas]] dice *dónde* van. Esta página dice **qué hace cada uno con palabras normales y
qué pasaría si no estuviera** — que es la forma en que hay que contarlo en la entrevista
individual de [[pc3-entregable]].

## Qué es un patrón, para empezar

No es código que se copia y se pega. **Es una forma conocida de resolver un problema que se
repite**, con un nombre acordado para poder hablar de ella. Igual que en cocina «sofreír» no es
una receta sino una técnica que aparece en cien recetas distintas.

De ahí sale la regla que este proyecto sigue desde [[adr-001-patrones-seleccionados]]:
**primero el problema, después el patrón.** Si no se puede nombrar el problema que resuelve, el
patrón sobra — y meterlo igual tiene nombre propio: *Golden Hammer*
([[s14-antipatrones]]).

## Los nueve trabajando a la vez

El viaje de una marcación, de tocar ENVIAR a que el dashboard se refresque. Es la mejor forma
de entenderlos, porque **ninguno trabaja solo**:

| # | Qué pasa | Patrón |
|---|---|---|
| 1 | Si no había señal, esperó guardada como una orden | **Command** *(por confirmar)* |
| 2 | Un portero mira los límites antes de dejarla pasar | **Proxy** |
| 3 | Un solo método se encarga de todos los pasos | **Facade** |
| 4 | Se decide sola si es una entrada o una salida | **Factory** |
| 5 | La sede sale de la lista que ya está en memoria | **Singleton** |
| 6 | La foto se envuelve con la fecha, la hora y la sede | **Decorator** |
| 7 | Se guarda la foto sin saber en qué nube acaba | **Bridge** *(por confirmar)* |
| 8 | …hablando el idioma de esa nube en concreto | **Adapter** *(por confirmar)* |
| 9 | La marcación entra a la base de datos | *ninguno: `sp_registrar_marcacion`* |
| 10 | Se anuncia «marcación registrada» a quien quiera oírlo | **Observer** |
| 11 | Se recalcula la jornada sumando turno a turno | **Composite** |
| 12 | El dashboard se refresca solo | *ninguno: HTMX* |

> **Inferencia:** el paso 3 llama al 4, el 4 produce el objeto que el 6 envuelve, y el 10
> desencadena el 11 sin conocerlo. Esa cooperación es la mitad de la respuesta en la entrevista.

## Creacionales

| Patrón | En cristiano | Dónde entra | Sin él |
|---|---|---|---|
| **Singleton** | Una sola copia de algo, compartida por todo el programa | La lista de las 7 sedes ([[sedes]]), cargada una vez al arrancar | Cada pantalla volvería a pedirle a la base las mismas siete filas que nunca cambian |
| **Factory** | Un único sitio que decide qué objeto crear | Decide si crea una entrada o una salida mirando si hay turno abierto ([[marcacion-multiple]]) | Ese `si tiene turno abierto…` estaría copiado en cada sitio que crea marcaciones |
| **Prototype** | Crear un objeto copiando otro que ya existe | **No entra.** Una marcación son cuatro datos: no hay nada que ahorrar copiando (S06 pptx) | Nada. Meterlo añadiría una clase que no resuelve ningún problema |

## Estructurales

| Patrón | En cristiano | Dónde entra | Sin él |
|---|---|---|---|
| **Facade** | Una puerta única a algo que por dentro tiene muchos pasos | `RegistrarMarcacion`: valida, sella, guarda y avisa ([[patron-facade]]) | El controlador tendría que conocer los cinco pasos, su orden y qué hacer si uno falla |
| **Decorator** | Envolver algo para añadirle una capa sin tocar lo de dentro | La foto se envuelve para estamparle fecha, hora y sede ([[patron-decorator]]) | La clase de la foto tendría que saber de relojes y de sedes |
| **Composite** | Tratar igual a una pieza suelta y a un grupo de piezas | Semana → día → jornada → turno ([[patron-composite]]) | Cuatro formas distintas de sumar horas, y cuatro sitios donde equivocarse |
| **Proxy** | Un portero delante del objeto de verdad | El login del dashboard y las fotos que no se cargan hasta abrirlas ([[patron-proxy]]) | Las ocho pantallas repetirían la comprobación de sesión, y el listado traería 40 fotos que nadie mira |
| **Bridge** | Separar *qué* se hace de *dónde* se hace | El almacén de fotos sobre tres nubes ([[adr-009-estructura-y-dos-patrones-que-vuelven]]) | Cambiar de Azure a Oracle obligaría a tocar todo el código que guarda fotos |
| **Adapter** | Un traductor entre dos formas de hablar | Traduce el SDK de la nube al vocabulario del dominio ([[patron-adapter]]) | El vocabulario de la nube se colaría dentro de las clases del negocio |

## De comportamiento

| Patrón | En cristiano | Dónde entra | Sin él |
|---|---|---|---|
| **Observer** | Avisar de que algo pasó sin saber quién escucha | Al guardar se publica el hecho; reaccionan el recálculo y el dashboard ([[patron-observer]]) | Guardar tendría que llamar a cada interesado por su nombre |
| **Command** | Guardar una orden en una caja para ejecutarla más tarde | La cola sin señal ([[adr-008-alcance-interfaz-y-cola-sin-senal]]) | Sin conexión no se podría marcar, y esa marcación se perdería |
| **State** | Cambiar de comportamiento según el estado | **Entró y luego salió**: quedaron dos estados ([[patron-state]]) | Nada, y ese es el punto: dos estados son un `if`, no una máquina de estados |

## Cómo se responde en la entrevista

La respuesta buena tiene tres partes y **siempre en este orden**: el problema primero, el patrón
después, la consecuencia al final.

> *«El controlador necesitaba hacer cinco cosas en orden y saber qué pasa si una falla. Usamos
> Facade, así que ahora llama a un método. Si mañana añadimos un sexto paso, el controlador ni
> se entera.»*

Quien empieza por la definición del libro suena a que la memorizó. Quien empieza por el problema
suena a que lo vivió — y aquí lo vivió de verdad, porque **cada uno de los nueve salió de un
requisito que el cliente pidió**.

## Enlaces

[[mapa-patron-requisito]] · [[cuatro-capas]] · [[adr-001-patrones-seleccionados]] ·
[[adr-009-estructura-y-dos-patrones-que-vuelven]] · [[s14-antipatrones]] · [[pc3-entregable]]
