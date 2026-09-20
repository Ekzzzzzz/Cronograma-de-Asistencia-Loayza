---
titulo: ADR-008 — Alcance de producción, interfaz y marcación sin señal
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-07)
  - prototipo navegable «Marcación Loayza» (artefacto 8ed44102, revisado 2026-09-07)
actualizado: 2026-09-07
tags: [adr, decision, alcance, interfaz, offline, ux]
---

# ADR-008 — Alcance de producción, interfaz y marcación sin señal

**Fecha:** 2026-09-07 · **Estado:** aceptada

## Contexto

Sesión larga de preguntas y respuestas con el usuario que cierra **doce huecos abiertos de
golpe**, y que además incorpora un **prototipo navegable de la app de marcación** que el
usuario había construido por otra vía y que contradecía al wiki en cinco puntos.

Es la primera vez que el proyecto tiene dos fuentes de diseño vivas a la vez. Reconciliarlas
—decidiendo cuál gana en cada punto y por qué— es la mitad de esta decisión.

## 1. Reconciliación con el prototipo

| Punto | Prototipo | Wiki | Resuelto |
|---|---|---|---|
| Identidad | Reconocimiento facial automático | Foto como evidencia que revisa una persona | **Gana el wiki** |
| Sede | Detectada por GPS | Desplegable de 7 sedes | **Gana el wiki** |
| Tope diario | 6 marcaciones | 12 marcaciones (6 pares) | **Gana el wiki** |
| Sin señal | Se encola y se envía después | No contemplado | **Gana el prototipo, con condiciones** |
| Entrada o salida | Se infiere del turno abierto | Dos botones explícitos (RF-04) | **Gana el prototipo** |

### Por qué no hay reconocimiento facial

**Es la decisión de mayor consecuencia de todo el ADR**, y el motivo principal no es técnico.

Una fotografía que se procesa **para identificar a una persona** deja de ser una foto y pasa a
ser un **dato biométrico**, que la Ley 29733 clasifica como **dato sensible**. Eso exige
consentimiento expreso, informado y por escrito de cada trabajadora, con una finalidad
declarada. El proyecto acaba de decidir (§2 de este mismo ADR) que
el aviso de privacidad lo maneja la empresa en papel; con reconocimiento facial ese aviso
dejaría de ser una cortesía para convertirse en un requisito legal que condiciona el
despliegue.

A eso se suman dos costos: un modelo o un servicio de reconocimiento **no cabe en el nivel
gratuito de Oracle** ([[adr-005-hosting-largo-plazo]]), y cada falso negativo es una podóloga
que no puede marcar.

> **La frase del prototipo se conserva, y es la mejor que produjo:** *«nunca se le dice "no
> eres tú"; el reconocimiento falla por luz o ángulo, y trasladarle ese error a la persona es
> inaceptable.»* Sin reconocimiento facial esa frase deja de ser una mitigación y pasa a ser
> innecesaria — que es la forma más limpia de resolver un problema.

**La foto sigue siendo lo que sostiene el sistema sin login**, y esa tesis también viene del
prototipo: el nombre nunca fue la garantía, la cara sí. La diferencia es que quien compara la
cara es **una persona, cuando sospecha**, no un modelo en cada marcación.

### Por qué el tope del prototipo estaba mal

El prototipo fijaba **6 marcaciones** y rechazaba la séptima. Con los números del propio
usuario —hasta **3 pares**, o sea 6 marcaciones, en un día de doble rotación— ese límite
**bloquea a una trabajadora legítima justo en el caso extremo que dice soportar**. No deja
holgura ninguna.

Se mantiene el tope de [[adr-007-seguridad-endpoint-publico]]: **12 marcaciones por
(trabajadora, sede, día)**, que es el doble de lo que ocurre en la práctica.

## 2. Decisiones de alcance

| Tema | Decisión | Nota |
|---|---|---|
| **Volumen** | 100–150 trabajadoras en las 7 sedes | Confirma la estimación de [[adr-002-arquitectura-produccion]]: no hay que rehacer ningún cálculo |
| **Geolocalización** | **No entra por ahora.** La sede se elige de un desplegable | El usuario: *«no queremos sobrecargar la aplicación»*. Queda como mejora futura, no como pendiente |
| **Administradora** | **Una sola cuenta** que ve las 7 sedes | Cierra la pregunta de si hay encargada por sede |
| **Datos históricos** | El sistema **arranca en blanco** | Los Excel viejos quedan como archivo. No hay tarea de importación |
| **Exportación** | **Solo la semana de lunes a domingo** | Exactamente el formato de [[formato-cronograma-excel]]. Sin vista mensual ni rangos libres |
| **Retención de fotos** | **3 meses** la foto completa, la miniatura se queda | Confirma [[adr-002-arquitectura-produccion]]. Mantiene el almacenamiento en ~12 GB, dentro del nivel gratuito de Oracle |
| **Medianoche** | **No ocurre**: los locales cierran antes | La clave (trabajadora, sede, **fecha**) es segura. No hace falta la regla de «la jornada pertenece al día en que abrió el turno» |
| **Dominio** | **Se compra uno propio** (~40 soles al año) | Con certificado gratuito de Let's Encrypt. Desbloquea el acceso a la cámara, que exige HTTPS |
| **Aviso de privacidad** | Lo maneja **la empresa en papel**, fuera del sistema | Entonces el informe tiene que decir explícitamente que esa parte es responsabilidad de la empresa |
| **Jornada sin salida en el Excel** | `10:12AM -`, con el guion y el hueco a la vista | Cierra la pregunta 16 de [[huecos-abiertos]] |

> **Sobre el aviso en papel.** Es una decisión válida, pero traslada una obligación legal fuera
> del software sin dejar rastro en él. **El informe debe declararlo**, no omitirlo: el sistema
> guarda fotografías de personas identificadas durante tres meses, y quién informa a esas
> personas es una responsabilidad asignada, no inexistente.

## 3. Marcación sin señal: dos horas, no una

El prototipo permitía guardar una marcación sin conexión y enviarla después. Tal cual, **eso
rompe la regla que sostiene todo el proyecto**: el servidor es el dueño del tiempo
([[adr-002-arquitectura-produccion]]). Si la marcación llega dos horas tarde, o el servidor
registra una hora falsa, o acepta la del teléfono sin decirlo.

**Se acepta la cola, con la condición de que el sistema nunca afirme una hora que no puede
probar.**

- La marcación encolada guarda **dos momentos**: el que puso el dispositivo y el de llegada al
  servidor.
- Se marca con **`fuente_hora = DISPOSITIVO`**. Una marcación normal lleva `SERVIDOR`.
- El dashboard y la exportación la muestran **distinta**: *«10:02 según el teléfono · recibida
  12:47»*. La administradora ve el desfase y decide.
- **Tope de antigüedad: 2 horas.** Una marcación encolada que llega más tarde **se rechaza** y
  queda en `intento_rechazado` con motivo `COLA_VENCIDA`.

> **El tope de 2 horas es la parte que hace segura a la cola.** Sin él bastaría con atrasar el
> reloj del teléfono para fabricar historia laboral. Con él, la ventana de manipulación queda
> acotada a un corte de señal puntual, que es el único caso que la cola pretende cubrir. El
> usuario eligió 2 horas sobre 12 a propósito: prefiere perder alguna marcación de una caída
> larga —que se arregla con la corrección manual que ya existe— antes que ampliar esa ventana.

**Las reglas de negocio se evalúan contra el día que declara la marcación**, no contra el
momento de llegada: el cooldown y el tope de 12 cuentan sobre la hora declarada. Si no, una
cola de tres marcaciones legítimas se rechazaría entera por llegar junta.

## 4. Interfaz: páginas del servidor, HTMX en el dashboard

El usuario preguntó si el aspecto moderno del prototipo era alcanzable sin React o Angular.

> **Lo es, y la pregunta llevaba dentro una confusión que conviene dejar por escrito.** El
> prototipo que el usuario admira **no usa ningún framework**: es HTML y CSS con unas 150
> líneas de JavaScript corriente. Lo que lo hace moderno es la tipografía, los radios, las
> sombras y el espaciado — **decisiones de diseño, no de arquitectura**. Thymeleaf genera
> exactamente ese mismo HTML.

| Parte | Cómo se construye |
|---|---|
| **Formulario de marcación** | Una página de Thymeleaf más ~200 líneas de JS propio: los pasos, la cámara, la compresión, el sello de hora y la cola sin señal |
| **Dashboard** | Thymeleaf con **HTMX** para refrescar tablas sin recargar la página |
| **Despliegue** | **Un JAR y un servidor.** Sin build de Node, sin CORS, sin dos canalizaciones |

**El flujo de marcar no toca el servidor entre pantalla y pantalla.** Foto → ¿quién eres? →
confirmar ocurren en el navegador, como en el prototipo, y se envía **una sola vez** al tocar
ENVIAR. Además de ser lo mejor posible con mala señal, es lo que hace natural la cola: si el
envío falla, hay exactamente un envío que reintentar.

Motivos, en orden de peso:

1. **El requisito principal es la velocidad de apertura.** Una SPA descarga entre 200 y 400 KB
   de JavaScript antes de dibujar el primer campo. Una página del servidor muestra el
   formulario en la primera respuesta. Para la usuaria de [[requisitos]] RNF-01 eso no es una
   preferencia técnica.
2. **Un solo despliegue**, que es lo que pidió el usuario para ahorrar costos.
3. **Los patrones quedan en el servidor**, donde [[pc3-entregable]] los evalúa. Con una SPA,
   Proxy, Facade y Observer se reparten entre dos proyectos y el informe se complica.

## 5. Mejoras de experiencia adoptadas del prototipo

Se incorporan sin cambios al diseño de la cara pública:

- **No se pregunta si es entrada o salida: se infiere.** Si hay turno abierto, la siguiente
  marcación lo cierra. **`Turno` ya lo soportaba** ([[marcacion-multiple]]), así que esto no
  toca el modelo — solo quita un paso de la pantalla. Deroga [[requisitos]] RF-04.
- **La trabajadora toca su cara, no un desplegable.** Rejilla de avatares con inicial y
  apellido, **filtrada por la sede elegida**: entre 5 y 18 caras en vez de 140.
- **Eso además cierra una fuga de datos.** [[adr-007-seguridad-endpoint-publico]] listaba
  «leer el desplegable de trabajadoras» como ataque; mostrar solo la sede seleccionada reduce
  lo expuesto de las siete sedes a una.
- **Nombre y apellido**, por el caso de las dos Laura.
- **El cargo no se pregunta**: si el nombre viene del padrón, el cargo ya se conoce. Aparece en
  la confirmación como comprobación, no como campo.
- **Un dato de dominio nuevo:** *no todas tienen celular propio*. Marcan desde el de una
  compañera o el del mostrador. Refuerza la decisión de no tener login y conviene que esté
  escrito.

## 6. El contador de la pantalla cuenta pares, no el tope

Corrección del usuario sobre la maqueta: la confirmación no dice «Marcación 1 de 12 de hoy»
sino **«Marcación 1 de 2 de hoy»**, luego «2 de 2», luego «3 de 4». El denominador es el **par
en curso**, no el máximo.

> **Lo pidió como claridad para la trabajadora y resuelve además un problema de seguridad que
> nadie había visto.** Mostrar el 12 en el formulario público es **anunciar el tope de la
> defensa**: le dice a cualquiera con el enlace cuántas peticiones aguanta el sistema antes de
> rechazar. Con el contador por pares, el 12 aparece **solo en el mensaje de rechazo**, cuando
> ya se alcanzó y ocultarlo no serviría de nada.

El tope duro de [[adr-007-seguridad-endpoint-publico]] **no cambia**: 12 marcaciones por
(trabajadora, sede, día), y la número 13 se rechaza. Lo que cambia es que el endpoint deja de
publicarlo. Ver [[marcacion-multiple]].

## Consecuencias

- **[[modelo-datos]] cambia**: `marcacion` gana `fuente_hora ENUM('SERVIDOR','DISPOSITIVO')` y
  `momento_recibido DATETIME NULL`; `intento_rechazado.motivo` gana `COLA_VENCIDA`.
- **[[requisitos]] cambia**: RF-04 queda derogado, RF-02 pasa a rejilla por sede, RF-10 corrige
  la nota vieja que aún dice «las fotos van al disco» —van al almacén de objetos desde
  [[adr-004-despliegue-azure]]—, y aparecen RF-22 (cola sin señal) y RNF-08 (aspecto y pila).
- **[[huecos-abiertos]] se vacía casi entero**: se cierran las preguntas 4, 5, 6, 10, 12, 16 y
  17.
- **Aparece un entregable nuevo:** el prototipo deja de ser un boceto y pasa a ser **la
  especificación de la pantalla de marcación**.
- **Aparece una tarea con fecha:** comprar el dominio, porque sin HTTPS no hay cámara y sin
  cámara no hay sistema.
- **No cambia ningún patrón.** Ni el reconocimiento facial descartado, ni HTMX, ni la cola
  añaden o quitan patrones: siguen siendo siete. La cola vive dentro de la misma fachada de
  registro y el `Turno` ya sabía inferir entrada o salida.

## Enlaces

[[adr-007-seguridad-endpoint-publico]] · [[adr-005-hosting-largo-plazo]] ·
[[adr-002-arquitectura-produccion]] · [[marcacion-multiple]] · [[modelo-datos]] ·
[[requisitos]] · [[huecos-abiertos]]
