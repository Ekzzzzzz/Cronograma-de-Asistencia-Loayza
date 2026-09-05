---
tipo: decision
titulo: ADR-005 — Interfaz de la app de marcación
numero: 005
estado_adr: propuesta
tags: [adr, pwa, interfaz, usabilidad]
creado: 2026-09-05
actualizado: 2026-09-05
---

# ADR-005 — Interfaz de la app de marcación

## Contexto

La restricción que manda sobre todas las demás: **las usuarias tienen más de treinta años y
no manejan más que redes sociales** *(aporte del usuario, 2026-09-05)*. Si la interfaz
fricciona, vuelven a WhatsApp y el proyecto fracasa por más correcto que sea el código.

El usuario propone un flujo sin cuentas: abrir el enlace, elegir sede, escribir nombre y
nota, tomar foto y enviar. Su razonamiento —«registrarse complicaría todo»— es correcto.
La conclusión que se extrae de él es la contraria a la esperada.

## El malentendido que hay que deshacer

«Que se registren» y «que tengan sesión» no son lo mismo.

Con el enrolamiento presencial de [[adr-003-autenticacion-jwt]], **la trabajadora nunca se
registra, nunca ve un login y nunca escribe una contraseña**. La administradora la da de
alta una vez, aprovechando la foto de referencia que hace falta igual. A partir de ahí, la
app se abre y ya está dentro.

Comparación de lo que ocurre **cada día**:

| Flujo sin cuentas (propuesto) | Flujo con sesión (decidido) |
|---|---|
| Abrir enlace | Abrir ícono |
| Elegir entre 7 sedes | *(preseleccionada; se puede cambiar)* |
| **Escribir su nombre** | *(el sistema ya sabe quién es)* |
| Escribir nota | *(opcional, plegada)* |
| Tomar foto | Tomar foto |
| Enviar | Enviar |
| **~15 interacciones, con teclado** | **3 toques, sin teclado** |

**La sesión no es una carga: es lo que elimina el trabajo.**

### Por qué escribir el nombre es inaceptable

Dos razones independientes, cada una suficiente:

1. **Rompe la identificación.** Es exactamente lo que produjo las **dos LAURA** y las
   abreviaturas tipo **`MARY P.`** en el Excel actual ([[formato-cronograma-actual]]).
   Volver a nombres escritos a mano reintroduce el problema que el canal propio había
   eliminado ([[adr-002-canal-de-marcacion]]).
2. **Rompe el antifraude.** Sin sesión, cualquiera con el enlace marca poniendo el nombre de
   otra. Se cae el vector 4 de [[antifraude]] —«que una compañera marque por ella»—, que es
   la razón por la que existen las fotos.

> [!warning] Revisado el 2026-09-05 por [[adr-006-acceso-sin-sesion]]
> El usuario descarta el enlace personalizado y pide **un enlace global**, porque hay
> trabajadoras **sin celular propio**. Con eso desaparece la sesión, y con ella el saludo
> «Hola, Daniela» de la pantalla 1 y la inferencia de entrada/salida por orden.
>
> El flujo pasa a ser: **Inicio → Foto → ¿Quién eres? → Confirmar → Listo**. Los ocho
> principios de interfaz y la tabla de casos límite siguen vigentes tal cual.
>
> La comparación de abajo se conserva porque explica **por qué no se escribe el nombre** —
> argumento que sigue en pie y que en el flujo nuevo se resuelve con un **selector de
> caras** en vez de con la sesión.

## Decisión

**Cuatro pantallas, un botón por pantalla, cero teclado en el camino normal.**

### Pantalla 1 — Inicio

- Saludo con su nombre: *«Hola, Daniela»*. Confirma de un vistazo que la app sabe quién es.
- Estado de hoy en una frase grande: *«Aún no has marcado hoy»* o *«Entraste a las 10:02»*.
- **Un solo botón enorme**, que cambia de texto según corresponda:
  **MARCAR ENTRADA** o **MARCAR SALIDA**.
- Debajo, en pequeño: *«Sede Miraflores · cambiar»*. La sede está resuelta; cambiarla es
  posible pero no obligatorio.

### Pantalla 2 — Foto

- Cámara en vivo a pantalla completa, con una guía ovalada para el rostro.
- Instrucción corta: *«Mira a la cámara»*.
- Botón de disparo grande y centrado, donde ya está el pulgar.

### Pantalla 3 — Confirmar

La pantalla más importante, y la que el usuario intuía como «formulario»:

- La foto tomada, en pequeño.
- **Qué se va a registrar, en letra grande**: *«ENTRADA · 10:02 a. m.»* y *«Sede Miraflores»*.
- *«Agregar una nota (opcional)»* — **plegado**. Sólo aparece el teclado si ella lo toca.
- Botón **ENVIAR** y, secundario, *«Repetir foto»*.

Aquí se resuelve lo que el usuario pedía como formulario, pero al revés: en vez de pedirle
datos, **el sistema le muestra lo que ya sabe y ella confirma**. Si algo está mal, se ve en
el acto en lugar de descubrirse a fin de mes.

### Pantalla 4 — Listo

- Marca de verificación grande y verde.
- *«¡Listo! Entrada registrada a las 10:02 a. m.»*
- Una línea amable de cierre.

**El camino normal son 3 toques: MARCAR → disparar → ENVIAR.** Sin teclado.

## Principios de interfaz para estas usuarias

*(propuesta del agente)*

1. **Una acción por pantalla.** Nunca dos botones que compitan.
2. **Objetivos táctiles grandes**: mínimo 56 px de alto; el botón principal, mucho más.
3. **Texto grande**: 18 px de cuerpo, 24 px o más para lo que importa. Muchas usarán lentes.
4. **Sin jerga y sin iconos sin etiqueta.** Nada de menú hamburguesa, pestañas ni engranajes.
   Las palabras son las que ellas usan: *entrada*, *salida*, *sede*.
5. **Alto contraste.** Se usa en clínicas con luz fuerte y en pantallas con brillo bajo.
6. **Confirmar antes de enviar, nunca después.** Ningún error debe ser irreversible.
7. **El teclado es el último recurso.** Sólo aparece si ella pide escribir una nota.
8. **Decir siempre qué pasó.** Nada de pantallas ambiguas: o se registró, o no, y se dice.

## Casos que la interfaz debe cubrir

| Situación | Qué muestra |
|---|---|
| Ya marcó entrada | El botón dice **MARCAR SALIDA** |
| Ya marcó las dos | *«Ya registraste tu jornada de hoy»*, sin botón |
| Sin señal | Se guarda y se envía sola: *«Guardado. Se enviará cuando haya señal»* |
| GPS lejos de la sede | **No se bloquea.** Se envía y va a revisión ([[antifraude]], principio 3) |
| Celular nuevo | Mensaje claro: *«Pide a la administradora que active este celular»* |
| Rostro no reconocido | Se envía igual y va a revisión. **Nunca se le dice «no eres tú»** |

Esa última fila importa: acusar a alguien en su propia pantalla, con un reconocimiento
facial que puede fallar por la luz, sería trasladar el error del sistema a la persona. El
caso se resuelve en la cola de la administradora, no en la cara de la trabajadora.

## Consecuencias

- El desarrollo del frontend es **poco código pero mucho cuidado**: cuatro pantallas simples
  cuestan más de afinar que diez complejas.
- Hay que **probarlo con una podóloga real** antes de desplegar en las 7 sedes. Es la única
  forma de saber si es intuitivo; nadie que haya construido el sistema puede juzgarlo.
- El panel de administración es una aplicación distinta, con criterios opuestos: denso,
  con tablas y filtros ([[adr-001-stack-y-arquitectura]]).

## Prototipo navegable

**https://claude.ai/code/artifact/8ed44102-37c0-4620-8019-d4b56f991b78**

Implementa las cuatro pantallas con estado real: al marcar entrada, el botón cambia solo a
MARCAR SALIDA, y tras la salida muestra la jornada completa. La cámara y la foto están
simuladas; las horas son las del dispositivo.

Incluye un modo a pantalla completa para enseñárselo a una podóloga desde un celular sin las
anotaciones de alrededor.

## Estado

**Propuesta.** El prototipo existe para validar el flujo antes de escribir código.

## Enlaces

- [[adr-002-canal-de-marcacion]] · [[adr-003-autenticacion-jwt]] · [[antifraude]]
- [[formato-cronograma-actual]] · [[decisiones-moc]]
