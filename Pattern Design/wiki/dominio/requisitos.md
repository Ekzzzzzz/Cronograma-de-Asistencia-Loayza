---
titulo: Requisitos del sistema
tipo: dominio
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
actualizado: 2026-09-05
tags: [requisitos, dominio]
---

# Requisitos del sistema

Fuente única: el brief del usuario del 2026-09-05. Todo lo que no esté aquí y no tenga
fuente es inferencia y debe marcarse como tal.

## Requisitos funcionales

### Cara trabajadora

- **RF-01 — Acceso por enlace general.** La trabajadora entra a un único enlace público.
  No se menciona login ni contraseña.
- **RF-02 — Identificación por nombre.** Primer campo: escribir el nombre de la
  trabajadora.
- **RF-03 — Selección de sede.** Segundo campo: menú desplegable con las 7 sedes
  (ver [[sedes]]).
- **RF-04 — Tipo de marcación.** Dos botones explícitos: **entrada** o **salida**.
- **RF-05 — Foto obligatoria como evidencia.** Después de elegir el tipo de marcación, la
  trabajadora toma una foto.
- **RF-06 — Sello de fecha y hora en la foto.** La fecha y la hora deben **salir en la
  imagen**, no solo guardarse como metadato.
- **RF-07 — Notas opcionales.** Un recuadro de texto libre, no obligatorio, después de la
  foto.
- **RF-08 — Envío.** Botón de enviar debajo del recuadro de notas.
- **RF-09 — Marcación múltiple.** La trabajadora puede marcar varias veces el mismo día, en
  sedes distintas, y todas las entradas y salidas quedan registradas. Ver
  [[marcacion-multiple]].

### Cara administradora

- **RF-10 — Persistencia.** Las fotos y los datos de marcación se guardan en base de datos.
- **RF-11 — Dashboard por sede.** Visualización intuitiva, organizada **una pestaña por
  sede**, según la sede que eligió la trabajadora.
- **RF-12 — Tabla estilo cronograma.** El sistema organiza los datos en una tabla por sede
  con el formato de [[formato-cronograma-excel]].
- **RF-13 — Exportación.** Esa tabla debe poder exportarse a Excel.

## Requisitos no funcionales

- **RNF-01 — Simplicidad por encima de todo.** Usuarias mayores de 30 años sin manejo de
  tecnología. Pocos pasos, botones grandes, texto claro, sin jerga.
- **RNF-02 — Web y móvil.** El flujo exige cámara: en la práctica se usará desde el
  teléfono.

  > **Inferencia:** el brief no dice "móvil" explícitamente, pero "tomar una foto" desde un
  > enlace web implica cámara del teléfono. Confirmar con el usuario.

- **RNF-03 — Stack.** Spring Boot (indicado por el usuario).
- **RNF-04 — Patrones de diseño.** El sistema debe aplicar patrones del curso de forma
  justificada. Ver [[mapa-patron-requisito]].

## Requisitos del curso (PC-3)

Añadidos tras ingerir [[pc3-entregable]]. **No son negociables**: son criterios de
evaluación, y pesan sobre el diseño tanto como los del cliente.

- **RC-01 — Arquitectura de cuatro capas.** Vista, Control, Modelo y Base de datos. Ver
  [[cuatro-capas]].
- **RC-02 — Diez patrones obligatorios.** Singleton, Prototype, Factory, Facade, Decorator,
  Composite, Proxy, Bridge, Observer y Command, repartidos por capa.
- **RC-03 — Base de datos con scripts y stored procedures.** Descarta un diseño solo-ORM.
- **RC-04 — Repositorio GitHub con carpetas `MVC` y `SQL`.**
- **RC-05 — Principios ISP** documentados con capturas de código y ejecución.
- **RC-06 — Evaluación de antipatrones** con planificación y evidencias.
- **RC-07 — El proyecto debe responder a un ODS** declarado en la introducción del informe.
- **RC-08 — Demo en video mp4** del software funcionando con datos de prueba, por módulo.

## Tensiones

- **RNF-03 vs. RC-03/RC-04.** El usuario pidió Spring Boot; el curso pide estructura `MVC` +
  `SQL` con stored procedures. Son compatibles, pero obligan a escribir SQL a mano en vez de
  delegar todo a JPA.
- **RC-07 no tiene equivalente en el brief.** El cliente quiere control de asistencia; el
  curso exige encuadrarlo en un Objetivo de Desarrollo Sostenible. Falta decidir cuál — ver
  [[huecos-abiertos]].

## Huecos y decisiones pendientes

Estos puntos no están definidos en el brief y hacen falta antes de modelar. Se detallan en
[[huecos-abiertos]]:

- ¿El nombre se escribe libre o se elige de una lista de trabajadoras registradas? Escribir
  libre produce duplicados ("DANIELA" / "Daniela" / "Dani") que rompen el agrupado del
  cronograma.
- ¿Quién y cómo accede al dashboard de administradora? RF-01 dice que la trabajadora no
  tiene login; del lado administrador no se dice nada.
- ¿Se valida que la trabajadora esté físicamente en la sede? Existe `tools/pluscode.py` con
  las coordenadas de las 7 sedes, lo que sugiere que se consideró geolocalización, pero el
  brief no la pide.
- ¿Qué pasa con una entrada sin salida al cierre del día?
- ¿Dónde se guardan las imágenes: en la base de datos o en disco con la ruta en base?
