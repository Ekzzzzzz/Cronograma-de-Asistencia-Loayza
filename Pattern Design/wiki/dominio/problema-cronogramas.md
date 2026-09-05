---
tipo: dominio
titulo: Problema — cronogramas diarios de Podología Loayza
tags: [dominio, problema, requisitos]
creado: 2026-09-04
actualizado: 2026-09-04
estado: borrador
fuentes: []
---

# Problema — cronogramas diarios de Podología Loayza

Enunciado del problema real que el programa debe resolver. Todo lo de esta página proviene
del usuario, no de una fuente del curso *(aporte del usuario, 2026-09-04)*.

> [!important] El proceso descrito abajo es el ACTUAL, el que se va a reemplazar
> Desde [[adr-002-canal-de-marcacion]], el sistema **no leerá WhatsApp**: las trabajadoras
> marcarán desde una PWA propia. Esta sección se conserva porque explica de dónde viene el
> requisito antifraude ([[antifraude]]) y qué costumbres hay que respetar en la migración.

## Situación actual (proceso manual)

- Podología Loayza tiene **7 sedes**, y un **grupo de WhatsApp por sede** — ver [[sedes]]
  para direcciones y coordenadas. *(El enunciado inicial decía 8; el usuario corrigió a 7
  el 2026-09-04.)*
- Las trabajadoras son **podólogas** o **cajeras**.
- Cada trabajadora marca asistencia enviando una **foto de sí misma** al grupo de la sede
  donde trabajó ese día. La foto se toma con una app que **imprime la hora exacta y la
  ubicación** sobre la imagen.
- Al enviarla, **adjunta su nombre** en el mensaje.
- **Dos fotos por día por trabajadora**: una de **ingreso** y otra de **salida**.
- Las fotos **siempre muestran el rostro**.
- A veces **una sola foto agrupa a varias personas**, con todos sus nombres adjuntos.
- El grupo al que se envía la foto **determina la sede**: una foto en «Sede Los Olivos»
  significa que esa trabajadora trabajó ese día en Los Olivos.
- Hoy alguien consolida todo esto **a mano** para armar el cronograma diario.

## Lo que debe hacer el programa

1. Tomar las imágenes de los grupos con sus mensajes adjuntos.
2. **Reconocer a las trabajadoras que aparecen en cada imagen**, incluidas las fotos
   grupales.
3. Deducir sede (por el grupo), momento (hora impresa) y tipo de marcación
   (ingreso o salida).
4. Emparejar ingreso con salida de la misma trabajadora, mismo día, misma sede.
5. Generar el **cronograma diario por sede**.

## Entidades del dominio

Páginas por crear, una por entidad: `sede`, `trabajadora` (con rol podóloga / cajera),
`marcacion` (ingreso / salida, con hora, ubicación e imagen), `jornada` (par
ingreso + salida), `cronograma-diario`, `grupo-whatsapp`.

## Reglas conocidas

- La sede sale del grupo de origen, no de la foto.
- Se esperan exactamente 2 marcaciones por trabajadora y día.
- La ubicación GPS impresa sirve para **verificar** que la marcación es coherente con la
  sede del grupo *(propuesta del agente: aún no confirmado como requisito)*.

## Preguntas abiertas

Hay que resolverlas antes de fijar la arquitectura.

**Respondidas por [[formato-cronograma-actual]] (2026-09-04):**

- ~~**¿Qué formato debe tener el cronograma de salida?**~~ → Excel, rejilla semanal de
  lunes a domingo, una fila por trabajadora, con conteos MAÑANA/TARDE al pie y una columna
  de observaciones. Detalle completo en [[formato-cronograma-actual]].
- ~~**¿Cuántas trabajadoras hay?**~~ → parcialmente: entre 21 y 25 podólogas en Los Olivos,
  con altas y bajas frecuentes. Falta saberlo de las otras 7 sedes y de las cajeras. Sigue
  sin conocerse si existe un padrón con fotos de referencia.

**Resueltas por los ADR de 2026-09-04:**

- ~~¿Cómo llegan las imágenes?~~ → PWA propia ([[adr-002-canal-de-marcacion]]).
- ~~¿Qué app de marcación usan / cómo se hace el OCR?~~ → **ya no aplica**: la hora la sella
  el servidor y las coordenadas las da el navegador.
- ~~¿Qué se hace cuando falla el reconocimiento?~~ → cola de revisión humana; nunca se
  rechaza ni se acepta en silencio ([[antifraude]], principio 3).
- ~~¿Proceso por lotes o servicio?~~ → servicio reactivo, marcación a marcación.
- ~~¿Existe un padrón?~~ → no; **se construye** al enrolar a las trabajadoras en la nueva
  plataforma *(decisión del usuario, 2026-09-04)*.

**Pendientes:**

1. ~~¿Cuáles son las sedes, con coordenadas y radio?~~ → **resuelto y completo**: son 7,
   todas con dirección, piso y coordenadas en [[sedes]].
2. **¿Rotan de sede?** ¿Una trabajadora puede marcar en dos sedes distintas el mismo día?
   ¿La sede la elige ella al marcar, o se deduce del GPS?
3. **¿Cómo se distingue ingreso de salida?** ¿Basta con «la primera del día es ingreso», o
   hay turnos partidos y jornadas que cruzan la medianoche?
4. **¿De dónde sale la programación prevista?** El Excel actual sólo guarda lo realmente
   marcado; para detectar tardanzas e inasistencias hace falta el turno planificado como
   entrada aparte. ¿Quién y cómo lo carga?
5. **¿Las cajeras llevan cronograma?** No aparecen en el archivo de Los Olivos.
6. **¿Qué pasa si una trabajadora no puede marcar** (sin señal, sin batería, celular roto)?
   Hace falta registro manual por la administradora con su propio rastro de auditoría.
7. **¿Dónde se despliega el backend?** Vercel cubre el frontend, no la base de datos.

## Enlaces

- Formato de salida objetivo: [[formato-cronograma-actual]]
- Estado del diseño: [[sintesis]]
- Decisiones tomadas: [[decisiones-moc]]
- Mapa de dominio: [[dominio-moc]]
