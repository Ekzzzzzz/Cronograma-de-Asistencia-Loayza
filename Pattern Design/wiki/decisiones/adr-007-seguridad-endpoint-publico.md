---
titulo: ADR-007 — Seguridad del endpoint público y jornada laboral
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-06)
actualizado: 2026-09-06
tags: [adr, decision, seguridad, abuso, dominio]
---

# ADR-007 — Seguridad del endpoint público y jornada laboral

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

El usuario precisó dos cosas que cambian cómo hay que leer una decisión anterior:

1. **El tope de 6 pares no es un límite de formato: es una medida de seguridad.** En la
   práctica no se ven más de **2 rotaciones, o sea 3 pares**. Los seis existen por si alguien
   intenta marcar muchas veces para tumbar el sistema.
2. **Hay que distinguir jornada completa de media jornada**, tanto en podólogas como en
   cajeras. No hay más cargos que esos dos.

El primer punto abre un frente que el wiki no había tocado en siete decisiones: **el
formulario de marcación es un endpoint público, sin autenticación, que acepta subida de
archivos** ([[requisitos]] RF-01). Es la superficie de ataque más expuesta del sistema y no
tenía ni una línea escrita.

## Qué se puede hacer contra este sistema

Enumerarlo antes de defenderlo, porque cada defensa tiene que corresponder a un ataque real:

| Ataque | Consecuencia |
|---|---|
| **Marcar cientos de veces** por una trabajadora | El cronograma se llena de basura y la jornada de esa persona deja de ser legible |
| **Subir archivos enormes** | Se agota el disco y se quema CPU en procesar imágenes que nadie pidió |
| **Subir una imagen "bomba"** —100 KB comprimidos que se expanden a miles de megapíxeles— | **Tumba la JVM al instante.** Con un heap de 384 MB, decodificar una imagen de 30 000 × 30 000 px es muerte inmediata |
| **Inundar de peticiones** | El servidor de 1 GB de RAM deja de responder para todas las sedes |
| **Leer el desplegable de trabajadoras** | Cualquiera con el enlace obtiene la lista del personal de las 7 sedes |

> **La imagen bomba es la más peligrosa y la menos obvia.** No hace falta enviar un archivo
> grande: basta un JPEG pequeño con dimensiones enormes declaradas. Es el ataque que un
> sistema con subida de fotos y poca RAM tiene que contemplar sí o sí.

## Decisiones

### 1. El tope de 6 pares se endurece: el séptimo se rechaza

**Esto corrige [[adr-006-dashboard-y-turnos-multiples]]**, que decía que un séptimo par "se
registra igual y marca la jornada para revisión". Eso tenía sentido cuando el límite era de
formato; **como defensa no sirve de nada**, porque aceptar por encima del tope es exactamente
lo que el atacante quiere.

- **Máximo 12 marcaciones por (trabajadora, sede, día).** La número 13 se rechaza.
- **Pero el intento se registra**, en una tabla de intentos rechazados, y la administradora lo
  ve en el dashboard.
- Si el caso resultara legítimo, **ya existe la vía de arreglarlo**: la corrección manual de
  [[adr-006-dashboard-y-turnos-multiples]], que inserta con `origen = ADMINISTRADORA` y motivo.

Así se cumplen las dos cosas a la vez: **el límite es duro** —que es lo que pide una defensa— y
**ningún dato real se pierde** —que era la preocupación original—. La diferencia es que ahora
recuperar el dato pasa por una persona, no por el endpoint público.

> **El tope real del negocio son 3 pares, no 6.** El usuario lo confirma: no se ven más de dos
> rotaciones. Los seis dan el doble de margen antes de estorbar a nadie, que es la holgura
> correcta para un límite de seguridad: **generoso para el uso legítimo, acotado para el
> abuso.**

### 2. Cuatro barreras, ordenadas de la más barata a la más cara

El principio que las ordena: **rechazar lo antes posible**. Descartar una petición en Nginx no
cuesta nada; descartarla después de decodificar una imagen cuesta cientos de milisegundos de
CPU que en un servidor de 1 vCPU son un lujo.

| Barrera | Qué detiene | Costo de rechazar |
|---|---|---|
| **1. Nginx** | Cuerpos de más de 2 MB · exceso de peticiones por IP | Cero: no llega a la JVM |
| **2. ¿Es una imagen?** | Archivos que no lo son · imágenes bomba de más de 5000 × 5000 px | Solo se lee la cabecera, no se decodifica |
| **3. Reglas del negocio** | Menos de 2 minutos desde la última marcación · más de 12 al día | Una consulta indexada |
| **4. Procesar** | *(ya no es barrera: aquí se sella, se genera miniatura y se guarda)* | ~300 ms de CPU |

Detalles que importan al implementar:

- **2 MB de límite de subida** es generoso: el navegador ya comprime a ~200 KB
  ([[adr-002-arquitectura-produccion]]). Va configurado **en Nginx y en Spring**, no en uno
  solo.
- **La validación de imagen lee los bytes mágicos**, no el `Content-Type` ni la extensión, que
  el cliente controla y puede mentir.
- **Las dimensiones se leen de la cabecera antes de decodificar.** Es la línea que separa un
  rechazo limpio de una JVM muerta.
- **El cooldown de 2 minutos** por trabajadora y sede: nadie entra y sale legítimamente en
  menos. Se resuelve en la **misma consulta** que cuenta las marcaciones del día, así que no
  cuesta un viaje extra.
- **El pool de procesamiento de imágenes va acotado** (medida 6 de
  [[adr-002-arquitectura-produccion]]): un pico encola en lugar de saturar.

### 3. El enlace lleva un segmento aleatorio

En vez de `/marcar`, algo como `/m/7f3a9c2e4b81`.

> **No es seguridad, y hay que decirlo así en el informe.** Quien tenga el enlace entra igual.
> Lo que hace es **filtrar el ruido automatizado**: los escaneadores que prueban rutas comunes
> no lo encuentran. Es barato y quita la mayor parte del tráfico basura, pero **no sustituye a
> ninguna de las cuatro barreras**.

### 4. Sin CAPTCHA — y se documenta por qué

Sería la defensa obvia contra automatización, y **se descarta**.

[[requisitos]] RNF-01 es explícito: usuarias mayores de 30 años con poca familiaridad
tecnológica, para quienes cualquier fricción se traduce en marcaciones no registradas. Un
CAPTCHA delante del formulario **rompería el requisito principal del cliente** para defender
algo que las cuatro barreras ya cubren.

Es una decisión de compromiso consciente: se acepta algo más de exposición a cambio de que el
sistema se use.

### 5. Jornada: completa o media, como dato y no como regla

Aparece `trabajadora.jornada` con valores **`FULL_TIME`** y **`PART_TIME`**, independiente del
cargo: hay podólogas y cajeras de ambos tipos.

**Se muestra, no se valida.** El dashboard y el cronograma lo exhiben junto al cargo, y los
totales de horas lo usan para dar contexto — 45 horas semanales significan cosas muy distintas
según la jornada contratada.

> **Deliberadamente no se implementa ninguna regla del tipo "media jornada no puede pasar de N
> horas".** Nadie la pidió, y rechazaría horas extra legítimas. El sistema **registra lo que
> ocurrió**; interpretarlo es criterio de la administradora, igual que con `DESCANSO` e
> `INASISTENCIA` en [[adr-003-login-y-cronograma-simple]]. Es la misma disciplina de siempre:
> no inventar reglas de negocio que el cliente no enunció.

### 6. No hay más cargos

Confirmado por el usuario: solo **podóloga** y **cajera**. El `ENUM` de
[[adr-006-dashboard-y-turnos-multiples]] se queda, y la tabla de catálogo sigue descartada.

## Lo que esto le hace al diseño de patrones

**Refuerza [[patron-proxy]], y no añade ningún patrón nuevo.**

Las barreras 1 y 3 son control de acceso delante del caso de uso real: un *Protection Proxy*.
[[patron-proxy]] ya lo aplicaba al dashboard; ahora tiene un **segundo uso, en el lado público**,
lo que refuerza su justificación en vez de estirarla.

> **La tentación era llamarlo patrón nuevo.** Un filtro de limitación de tasa se parece a
> Decorator, a Chain of Responsibility y a Proxy a la vez. Pero Chain of Responsibility ni
> siquiera está entre los quince del curso, y bautizar como patrón nuevo lo que es un segundo
> uso del mismo sería inflar la cuenta — el **Golden Hammer** que
> [[adr-001-patrones-seleccionados]] se comprometió a evitar. Se documenta como lo que es.

## Consecuencias

- **[[requisitos]] gana RNF-07** (límites de abuso en el endpoint público) y **RF-21**
  (jornada completa o media).
- **[[modelo-datos]] cambia**: `jornada` en `trabajadora`, y una tabla nueva
  `intento_rechazado` con IP, momento, trabajadora y motivo.
- **Aparece configuración de Nginx** como parte del entregable, no solo como despliegue: los
  límites viven ahí.
- **El dashboard gana una señal**: los intentos rechazados tienen que verse, o la barrera
  protege en silencio y nadie se entera de que alguien lo intentó.
- **Material nuevo para el informe.** PC-3 no pide un capítulo de seguridad, pero un endpoint
  público con defensas razonadas es contenido sólido para la entrevista, y el descarte del
  CAPTCHA es un ejemplo limpio de decisión de compromiso documentada.

## Enlaces

[[adr-006-dashboard-y-turnos-multiples]] · [[adr-002-arquitectura-produccion]] ·
[[patron-proxy]] · [[requisitos]] · [[modelo-datos]] · [[marcacion-multiple]]
