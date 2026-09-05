---
tipo: moc
titulo: Síntesis — estado del diseño
creado: 2026-09-04
actualizado: 2026-09-04
estado: borrador
---

# Síntesis — estado del diseño

Página viva. Responde en todo momento a: **«¿cómo se resuelve hoy el problema, y con qué
patrones?»** Se reescribe con cada decisión aceptada; no es un histórico (para eso está
[[log]]).

## Estado actual

**Fase: arquitectura y dominio decididos, patrones pendientes.** Tres ADR **aceptados**
([[adr-001-stack-y-arquitectura]], [[adr-002-canal-de-marcacion]],
[[adr-003-autenticacion-jwt]]) y uno propuesto ([[adr-004-llenado-del-cronograma]]). No hay
código todavía.

**Lo que el sistema puede prometer, medido:** llena solo el **81,7 %** de la rejilla
semanal. El resto son ~26 decisiones de dos opciones por semana y sede. No detecta
tardanzas, porque no existe programación de turnos contra la que comparar.

**El giro decisivo (2026-09-04): se abandona WhatsApp.** El sistema deja de leer un canal
ajeno y pasa a tener el suyo — una PWA desde la que las trabajadoras marcan con la cámara.
El problema deja de ser *interpretar evidencia ajena* y pasa a ser *generar evidencia
propia*, y con eso desaparecen el OCR, la clasificación de imágenes, el emparejamiento por
nombre escrito a mano y el riesgo de que bloqueen el número del negocio.

Las decisiones sobre **qué patrón va en cada etapa** siguen bloqueadas hasta ingerir
S06–S13: sin catálogo de patrones no hay con qué comparar.

Lo que ya está fijado: **el formato de salida**. El programa debe reproducir la rejilla
semanal del Excel que se usa hoy — ver [[formato-cronograma-actual]]. No hay que diseñar
el entregable, hay que llenarlo automáticamente y sin los errores del proceso manual.

Además, el análisis del Excel reveló que el problema es **más grande que «hacer un
cronograma»**: el archivo dejó de ser una programación prevista y hoy es un *registro de
asistencia real* transcrito a ojo desde las fotos. Ahí está el trabajo manual que se
automatiza.

## Recorrido del problema

Cadena que el programa tendrá que cubrir, de la foto al cronograma
(descomposición preliminar, propuesta del agente — cada eslabón necesitará su ADR):

```
1. Recepción      POST desde la PWA: foto + comentario + coordenadas + token de captura
2. Validación     Cadena de verificaciones antifraude → veredicto y confianza  [[antifraude]]
3. Identificación Reconocimiento facial contra el padrón: ¿es quien dice ser?
4. Tipificación   ¿ingreso o salida?
5. Registro       Persistir la marcación con toda su evidencia
6. Consolidación  Recalcular la jornada y el cronograma de la semana
7. Salida         Rejilla semanal por sede, formato [[formato-cronograma-actual]]
```

Cualquier etapa que quede bajo el umbral de confianza manda el caso a la **cola de revisión
humana** en vez de adivinar ([[antifraude]], principio 3).

Los eslabones 1 y 7 ya tienen destino conocido; los del medio esperan al catálogo de
patrones. La etapa 2 es la más rica en oportunidades de diseño.

Cada eslabón es un punto donde un patrón de diseño puede entrar. Ninguno está decidido
todavía.

## Patrones candidatos por etapa

Los 15 patrones del curso ya están evaluados contra el problema ([[patrones-moc]]).
Asignación **preliminar** de cada etapa de la tubería, sin ADR todavía
*(propuesta del agente)*:

| Etapa | Patrones candidatos |
|---|---|
| Toda la tubería | [[patron-facade]] — `ServicioDeMarcacion`, punto único de entrada |
| 1. Recepción | [[patron-builder]] — armar `Marcacion` por etapas |
| 2. Validación antifraude | [[patron-composite]] + [[patron-decorator]] + [[patron-factory]] |
| 3. Identificación | [[patron-adapter]] (motor externo) + [[patron-decorator]] (caché, auditoría) |
| 4. Tipificación y ciclo de vida | [[patron-state]] — 6 estados con transiciones reales |
| 5. Registro | [[patron-observer]] — publicar `MarcacionRegistrada` |
| 6. Consolidación | [[patron-composite]] — agregar conteos por el árbol |
| 7. Exportación | [[patron-factory]] — elegir exportador por formato |
| Cola de revisión | [[patron-command]] + [[patron-memento]] — auditoría con deshacer |
| Acceso a fotos y biometría | [[patron-proxy]] — Protection + Virtual |

**Descartados a propósito**: [[patron-singleton]] (Spring ya lo da) y
[[patron-abstract-factory]] (no hay familias que deban ser coherentes). Las dos decisiones
se defienden en [[patrones-moc]].

Quedan **8 ADR por escribir**, uno por decisión ([[decisiones-moc]]).

## Riesgos abiertos

- **Migrar a las trabajadoras al canal nuevo.** Pasó a ser el riesgo mayor, y es humano, no
  técnico: usuarias con poco manejo informático cambiando la costumbre de mandar un
  WhatsApp. Si la PWA no resulta más fácil que el proceso actual, fracasa.
- **El cliente no es confiable.** Ninguna garantía antifraude puede depender del navegador;
  todo se verifica en el servidor. Ver [[antifraude]].
- **Privacidad y datos biométricos.** Rostros y ubicaciones de personas reales; el diseño
  debe asumir procesamiento local (`CLAUDE.md` §8).
- **Fiabilidad del reconocimiento facial.** Iluminación variable y mascarillas degradan la
  identificación; la cola de revisión manual es obligatoria, no opcional.
- **Sin señal no hay marcación.** Necesita modo sin conexión con envío diferido.
- **Despliegue del backend.** Vercel sirve para el frontend, no para un backend con base de
  datos. Sin decidir.
