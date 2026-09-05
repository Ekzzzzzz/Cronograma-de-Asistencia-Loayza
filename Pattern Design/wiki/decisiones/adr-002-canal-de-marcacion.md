---
tipo: decision
titulo: ADR-002 — Canal de marcación (PWA propia, se descarta WhatsApp)
numero: 002
estado_adr: aceptada
tags: [adr, ingesta, pwa, antifraude]
creado: 2026-09-04
actualizado: 2026-09-04
reemplaza: "versión anterior de este ADR, basada en ingesta desde WhatsApp"
---

# ADR-002 — Canal de marcación (PWA propia, se descarta WhatsApp)

## Contexto

La versión anterior de este ADR proponía leer las imágenes de los grupos de WhatsApp —uno
por cada una de las **7 sedes**, ver [[sedes]]— mediante adaptadores intercambiables. **Se descarta** *(decisión del usuario, 2026-09-04)*:

- El único camino que lee grupos son librerías no oficiales que violan los términos de
  servicio y **exponen el número del negocio a bloqueo**. Inaceptable.
- Se quiere un sistema **100 % automatizado, antifraude y profesional**, no un puente frágil
  que depende de que WhatsApp no cambie nada.

Hay que recordar **por qué existen las fotos**: antes la asistencia se anotaba en un
cronograma de papel y **cualquiera podía mentir sobre su hora de entrada**. La foto con hora
y ubicación impresas por una app de terceros es un mecanismo antifraude, no un capricho
*(aporte del usuario, 2026-09-04)*.

Restricción humana, y es la que manda: **las trabajadoras saben poco o nada de informática;
usan WhatsApp y redes sociales, nada más**. Cualquier solución que exija instalar algo,
aprender una interfaz complicada o seguir instrucciones técnicas fracasa.

## Decisión

**Canal propio: una PWA (Progressive Web App) *mobile-first*, alojada en Vercel, desde la
que cada trabajadora marca su asistencia con la cámara del celular.**

Flujo de la trabajadora, diseñado para que sea más simple que mandar un WhatsApp:

1. Abre el enlace desde su celular (o el ícono, si instaló la PWA en la pantalla de inicio).
2. La sesión ya está abierta — no vuelve a escribir contraseña (ver
   [[adr-003-autenticacion-jwt]]).
3. Ve un solo botón grande: **MARCAR**.
4. Se abre **la cámara del dispositivo**; se toma la foto.
5. La pantalla de confirmación muestra **qué se va a registrar**, ya resuelto por el
   sistema: *«ENTRADA · Sede Miraflores»*. Ella sólo confirma, o corrige la sede si hace
   falta.
6. Puede añadir un **comentario de texto** opcional antes de enviar (equivale al texto que
   hoy adjunta en WhatsApp).
7. Envía. El sistema responde en pantalla si quedó registrada.

### Cómo se resuelven la sede y el tipo

Decisiones del usuario, 2026-09-05:

- **Sede: preseleccionada, ella confirma.** El sistema propone la sede y ella la ve escrita
  antes de enviar. El GPS **valida pero no decide** — con 5 de 7 sedes en pisos altos
  ([[sedes]]), dejar que una lectura mala eligiera la sede sería frágil, y ella no tendría
  cómo corregirlo.
- **Entrada o salida: lo infiere el sistema y lo muestra.** La primera marcación del día es
  entrada, la segunda salida. Se mantiene el flujo de un solo botón, y al mostrarlo antes de
  enviar se evita el error clásico de marcar salida al llegar. Si el sistema se equivoca,
  queda visible en el acto en vez de descubrirse a fin de mes.

Backend Spring Boot recibe la marcación por API ([[adr-001-stack-y-arquitectura]]).

## Justificación

- **Elimina el riesgo de bloqueo** del número del negocio. Ese era el problema.
- **Es más fácil que el proceso actual, no más difícil.** Hoy la trabajadora abre una app de
  marcación, toma la foto, abre WhatsApp, busca el grupo, adjunta la foto y escribe su
  nombre. Con la PWA: abrir y un botón. Esto importa más que cualquier consideración
  técnica.
- **La hora deja de ser un dato que hay que creer.** Hoy viene impresa por una app de
  terceros y se transcribe a ojo. Con canal propio, **la hora la sella el servidor**: no se
  puede falsificar desde el teléfono.
- **No hace falta instalar nada.** Una PWA es una página web; se abre desde un enlace que se
  les puede mandar por WhatsApp. Ellas ya saben abrir enlaces.
- El sistema pasa a ser dueño de la evidencia en vez de intérprete de evidencia ajena.

### Lo que esta decisión elimina de la tubería

Consecuencia importante y no obvia *(propuesta del agente)*:

| Etapa del diseño anterior | Qué pasa ahora |
|---|---|
| **OCR** de la hora y ubicación impresas | **Desaparece.** La hora la pone el servidor y la ubicación la da la API de geolocalización del navegador. |
| **Clasificar** si la imagen es de marcación | **Casi desaparece.** El canal es propio: todo lo que entra pretende ser una marcación. Queda sólo validar que la foto sirva (que tenga un rostro utilizable). |
| Deducir la **sede** por el grupo de WhatsApp | Cambia de fuente: se resuelve por geocerca y por la asignación de la trabajadora. |
| Emparejar por **nombre adjunto** escrito a mano | **Desaparece el problema de los nombres.** La sesión ya dice quién es; el rostro lo confirma. Se acaban las dos LAURA y las `MARY P.`. |

La tubería se reduce y, a la vez, se vuelve más confiable.

## Alternativas descartadas

| Opción | Por qué no |
|---|---|
| Librerías no oficiales de WhatsApp (Baileys, whatsapp-web.js) | Violan los términos de servicio; riesgo de bloqueo del número del negocio. **Motivo del cambio.** |
| WhatsApp Business Cloud API | No cubre lectura de grupos. |
| Exportación `.zip` del chat | No es automatizado; alguien tiene que exportar a mano cada día. |
| App móvil nativa (Android) | Hay que instalarla, publicarla y mantenerla; barrera alta para usuarias no técnicas. La PWA da el 90 % del beneficio con una fracción del costo. |
| Reloj biométrico / huella en cada sede | Antifraude excelente, pero es hardware por sede, con costo e instalación. Fuera del alcance de un proyecto de software. |

## Consecuencias

**A favor**
- Automatización real de punta a punta, sin intervención manual ni riesgo legal.
- Datos limpios desde el origen: hora exacta, identidad conocida, sede validada.
- La empresa deja de depender de una app de terceros para su control de asistencia.

**En contra**
- **Hay que migrar a las trabajadoras**: acompañamiento, enrolamiento y un periodo en
  paralelo con WhatsApp hasta que todas estén dentro. Es el mayor riesgo del proyecto, y es
  humano, no técnico.
- Depende de que tengan datos móviles y un celular con cámara. Necesita **modo sin
  conexión**: si no hay señal, la marcación se guarda en el dispositivo y se envía al
  reconectar, conservando la hora de captura como reclamada y marcándola para revisión.
- La PWA no puede *garantizar* por sí sola que la foto venga de la cámara en vivo. Por eso
  el antifraude se resuelve **en el servidor** — ver [[antifraude]].

## Estado

**Aceptada** *(decisión del usuario, 2026-09-04)*.

## Enlaces

- [[antifraude]] — cómo se sostiene la garantía antifraude sin poder confiar en el cliente
- [[adr-001-stack-y-arquitectura]] · [[adr-003-autenticacion-jwt]]
- [[problema-cronogramas]] · [[sintesis]] · [[decisiones-moc]]
