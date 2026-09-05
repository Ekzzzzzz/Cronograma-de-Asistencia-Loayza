---
tipo: dominio
titulo: Antifraude — por qué existe y cómo se sostiene
tags: [dominio, antifraude, seguridad, requisitos]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Antifraude — por qué existe y cómo se sostiene

## El origen del requisito

Antes, la asistencia se anotaba en un **cronograma de papel** y cualquiera podía escribir la
hora que quisiera. Por eso se pasó a las fotos con hora y ubicación impresas por una app:
**el objetivo del sistema nunca fue registrar asistencia, fue registrar asistencia que no se
pueda falsear** *(aporte del usuario, 2026-09-04)*.

Cualquier decisión de diseño que facilite el fraude rompe el propósito del proyecto, por
muy elegante que sea. Esta página es la referencia obligada para todo ADR de la tubería.

## La verdad incómoda del cliente

> [!warning] El cliente no es confiable, y no hay forma de que lo sea
> Una PWA **no puede garantizar** que la foto venga de la cámara en vivo ni que el GPS sea
> real. `<input capture>` es una sugerencia que el navegador puede ignorar; en muchos
> navegadores la usuaria puede elegir de la galería igual. La geolocalización del navegador
> se falsea con apps de *mock location* o con las herramientas de desarrollo. Una cámara
> virtual puede alimentar `getUserMedia` con un vídeo grabado.
>
> **Conclusión de diseño: ninguna garantía antifraude puede depender del navegador.** Todas
> las verificaciones que importan ocurren en el servidor. El cliente aporta comodidad, no
> seguridad.

Esto no debilita la propuesta: el sistema actual **también** es falsificable (se puede
fotografiar la pantalla de otro teléfono) y aun así funciona, porque el rostro y la hora
hacen que mentir sea trabajoso y detectable. La meta realista no es la imposibilidad
matemática del fraude, sino que **hacer trampa cueste más que trabajar**, y que cuando
ocurra quede rastro.

## Vectores de fraude y su defensa

| # | Vector | Defensa (servidor) |
|---|---|---|
| 1 | Subir una foto vieja de la galería | **Frescura**: al abrir la captura el servidor emite un *token de captura* con vida corta (p. ej. 90 s) y un identificador único; la foto sólo se acepta si llega con ese token vigente y sin usar. Una foto de ayer no tiene token válido. |
| 2 | Manipular la hora del teléfono | **La hora la sella el servidor**, siempre. La hora del dispositivo se guarda sólo como dato informativo y, si difiere mucho, se marca la anomalía. |
| 3 | Falsear el GPS | **Geocerca** por sede (radio 200 m, ver [[sedes]]) sensible a la precisión declarada por el navegador + coherencia: distancia y tiempo respecto de su marcación anterior. Fuera de la cerca no se rechaza sin más: se registra y se manda a revisión humana. |
| 4 | Que una compañera marque por ella | **Reconocimiento facial contra el padrón.** Es la defensa central y la razón de que la foto siga siendo obligatoria. |
| 5 | Prestar el celular o la cuenta | **Vinculación de dispositivo**: la sesión se ata a un identificador de dispositivo; marcar desde uno nuevo exige reautenticación y queda avisado en el panel. |
| 6 | Fotografiar la pantalla de otro teléfono | Difícil de detectar automáticamente. Mitigación práctica: el rostro debe coincidir **y** las señales (cerca, frescura, dispositivo) alinearse; las anomalías combinadas van a revisión. |
| 7 | Marcar entrada y salida seguidas para simular jornada | **Regla de duración mínima** entre ingreso y salida, y contraste con el turno programado. |

## Principios que se derivan

1. **El servidor es la única autoridad de la hora.** Nunca el cliente.
2. **Ninguna verificación se ejecuta en el navegador.** El cliente captura y envía; el
   servidor decide.
3. **Rechazar es peor que revisar.** Una marcación sospechosa **nunca** se descarta ni se
   acepta en silencio: se registra con sus señales y va a la cola de revisión. Rechazar de
   plano deja a una trabajadora real sin su asistencia registrada, que es un daño peor que
   una revisión de más.
4. **Toda marcación conserva su evidencia**: foto, coordenadas, hora de servidor, hora de
   dispositivo, resultado de cada verificación. Sin evidencia no hay auditoría posible, y
   sin auditoría el sistema no sirve para un reclamo laboral.
5. **Las verificaciones son independientes entre sí y se pueden añadir o quitar.** Hoy son
   siete; mañana serán otras. Ninguna debe estar cableada dentro de otra.

## Forma que esto sugiere para el código

Cada verificación es una regla independiente que recibe la marcación y devuelve un
resultado con su nivel de confianza; el conjunto se compone y produce un veredicto:
**aceptada**, **a revisión** o **rechazada**.

Esa forma —una cadena de comprobaciones intercambiables y componibles— es terreno directo
de varios patrones del curso. Candidatos evidentes: `chain-of-responsibility`, `strategy`,
`composite`, `decorator`. **Sin decidir** hasta ingerir S08–S13 y abrir el ADR
correspondiente (`CLAUDE.md` §5.3).

## Preguntas abiertas

1. ~~¿Coordenadas y radio de cada sede?~~ → **resuelto** en [[sedes]]: **las 7 sedes** con
   coordenadas, radio propuesto de **200 m** configurable por sede. Las dos sedes más cercanas están a 3,3 km,
   así que ninguna geocerca razonable puede confundirlas.
2. ~~¿Qué margen de tolerancia?~~ → **resuelto**: regla sensible a `coords.accuracy`, con
   cuatro veredictos y **ninguno de rechazo automático** ([[sedes]]). Hacía falta porque
   5 de las 7 sedes están en pisos altos (Lince en el 12, Miraflores en el 6), donde el
   GPS es malo.
3. ¿Quién revisa la cola y con qué frecuencia? La administradora, ¿a diario?
4. ~~¿Qué pasa si una trabajadora no logra marcar?~~ → **resuelto**: la administradora la
   registra a mano, marcada como **registro manual** y no como marcación verificada, con
   rastro de auditoría ([[adr-001-stack-y-arquitectura]]). Se descartó marcar desde el
   celular de una compañera, que choca con el vector 5.
5. ¿Se le avisa a la trabajadora cuando su marcación queda en revisión?

## Enlaces

- [[adr-002-canal-de-marcacion]] · [[adr-003-autenticacion-jwt]]
- [[problema-cronogramas]] · [[sintesis]] · [[dominio-moc]]
