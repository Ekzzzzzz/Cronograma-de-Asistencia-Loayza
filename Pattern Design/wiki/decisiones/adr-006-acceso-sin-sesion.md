---
tipo: decision
titulo: ADR-006 — Enlace global sin sesión e identificación por rostro
numero: 006
estado_adr: propuesta
tags: [adr, acceso, identidad, antifraude]
creado: 2026-09-05
actualizado: 2026-09-05
reemplaza: "el enrolamiento con sesión por dispositivo de [[adr-003-autenticacion-jwt]], sólo para el rol TRABAJADORA"
---

# ADR-006 — Enlace global sin sesión e identificación por rostro

## Contexto

El usuario descarta el enlace personalizado y pide **un enlace global, igual para todas**
*(decisión del usuario, 2026-09-05)*. La razón es una restricción del negocio que no se
había considerado en ningún ADR anterior:

> **Hay trabajadoras que no tienen celular.**

Eso invalida por sí solo el enrolamiento con sesión ligada al dispositivo
([[adr-003-autenticacion-jwt]]): no se puede vincular un dispositivo a quien no lo tiene.
Quien no tenga celular marcará desde el de una compañera, y ese caso deja de ser una
excepción para volverse parte del funcionamiento normal.

El usuario propone además que, tras la foto, aparezcan **dos campos: «Nombre» y «Función»**,
precisamente porque puede marcarse desde cualquier celular.

## El replanteamiento que exige

Sin sesión, el sistema ya no *sabe* quién marca: recibe una **afirmación**. Conviene decirlo
sin rodeos y ver qué se pierde y qué no.

> [!important] El nombre nunca fue la garantía. La cara sí.
> En el proceso actual de WhatsApp el nombre **también** lo escribe la trabajadora a mano, y
> cualquiera podría escribir el de otra. Lo que sostiene el sistema hoy es que **la foto
> muestra el rostro**.
>
> El enlace global no empeora la situación actual: la iguala, y le añade dos cosas que hoy no
> existen — **hora sellada por el servidor** y **ubicación verificada**.

De ahí el principio que ordena toda esta decisión:

**El nombre es una afirmación que acelera la comparación. El rostro es la prueba.**

## Decisión

**Enlace global, sin login, con identificación declarada y verificada contra el padrón.**

### 1. El nombre se elige, no se escribe

Esta es la única corrección a la propuesta del usuario, y va en la dirección que él mismo
persigue *(propuesta del agente)*:

| Escribir el nombre | Elegirlo del padrón |
|---|---|
| Aparece el teclado | Sin teclado |
| `MARY P.`, `mary`, `Mari` — todo distinto | Un registro exacto, siempre |
| Las **dos LAURA** vuelven | Se distinguen por apellido y foto |
| No se puede comparar con el padrón | Enlaza directo con su ficha y su rostro de referencia |

Tras la foto aparece **«¿Quién eres?»** con las trabajadoras de esa sede en cuadrícula, **con
su foto**. Ella **toca su propia cara**. Es más rápido que escribir, no necesita saber
teclear, y produce un dato limpio.

La lista es corta porque se filtra por la sede detectada: entre 5 y 18 personas
([[sedes]]). Queda un buscador para el caso raro y una salida «no estoy en la lista».

### 2. La «Función» no se pregunta: se muestra

Si el nombre viene del padrón, **el rol ya se conoce**. Pedirlo otra vez es pedirle un dato
que el sistema tiene, y abre la puerta a que se equivoque.

En la pantalla de confirmación aparece como comprobación: *«Daniela Ríos · Podóloga»*. Ella
lo ve, no lo escribe.

### 3. El rostro pasa a ser la verificación principal

Antes era una defensa secundaria detrás de la sesión. Ahora es **la** defensa: el sistema
compara la foto recién tomada con la de referencia de la persona declarada.

- **Coincide** → marcación verificada.
- **No coincide o hay poca confianza** → se registra igual y va a la cola de revisión.
  **Nunca se le dice «no eres tú»** ([[adr-005-interfaz-de-marcacion]]).

Esto sube la exigencia sobre el motor de reconocimiento: ya no es un lujo, es el eje. Y
refuerza que entre por [[patron-adapter]], para poder cambiarlo si el elegido no rinde.

### 4. El dispositivo baja de garantía a señal

Ya no hay vinculación. Pero el navegador sigue dejando una huella estable que sirve como
**señal de confianza**, no como permiso:

- un dispositivo que suele marcar para la misma persona suma confianza;
- un dispositivo desde el que marcan ocho personas distintas en un día no se bloquea —
  puede ser el celular compartido de la sede— pero **es un dato para la cola**.

### 5. Propuesta adicional: un celular por sede

*(propuesta del agente, sin decidir)*

Para las trabajadoras sin celular, lo más limpio no es que usen el de una compañera sino que
la sede tenga **su propio celular o tableta registrada**, en el mostrador. Un dispositivo
conocido, en una ubicación conocida, disponible para todas. Es más controlable que un
teléfono personal prestado y no cuesta casi nada.

No sustituye al enlace global; lo complementa.

## Qué se pierde, dicho claro

| | Con sesión (ADR-003) | Con enlace global (este ADR) |
|---|---|---|
| Suplantar a otra | Muy difícil: hay que tener su celular desbloqueado | **Posible**: declarar su nombre y esperar que el rostro pase |
| Identidad | Garantizada por la sesión | **Afirmada** y verificada por el rostro |
| Teclado | Nunca | Nunca (con selector) |
| Sin celular propio | **No funciona** | **Funciona** |
| Frente al proceso actual | Mucho mejor | **Mejor**: hora y ubicación ya no son falsificables |

La suplantación queda dependiendo por completo de que el reconocimiento facial acierte. Es
un riesgo real y hay que asumirlo con los ojos abiertos: **el sistema deja de garantizar la
identidad y pasa a verificarla con margen de error**, con la cola de revisión como red.

## Consecuencias

- **[[adr-003-autenticacion-jwt]] queda vigente sólo para la administradora**, que conserva
  login obligatorio y segundo factor. El rol `TRABAJADORA` deja de tener sesión.
- Desaparece el enrolamiento presencial como requisito para marcar. Sigue haciendo falta
  **dar de alta a cada trabajadora con su foto de referencia**, que ahora es imprescindible:
  sin ella no hay nada contra qué comparar.
- La puesta en marcha se simplifica muchísimo: se reparte un enlace y ya.
- El vector 5 de [[antifraude]] («prestar el celular») deja de aplicar, y el **vector 4**
  («que una compañera marque por ella») se vuelve el riesgo central del sistema.

## Estado

**Propuesta.** Pendiente de aceptar, y de decidir sobre el celular por sede.

## Enlaces

- [[adr-005-interfaz-de-marcacion]] · [[adr-003-autenticacion-jwt]] · [[antifraude]]
- [[jornadas-multiples]] · [[sedes]] · [[decisiones-moc]]
