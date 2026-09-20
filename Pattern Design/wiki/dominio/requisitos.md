---
titulo: Requisitos del sistema
tipo: dominio
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
actualizado: 2026-09-06
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

  > **Matizado dos veces.** El 2026-09-06 por [[adr-002-arquitectura-produccion]]: el nombre
  > **se elige de una lista cerrada**, no se escribe. El 2026-09-07 por
  > **Matizado por tercera vez el 2026-09-20** por [[adr-010-sin-rechazos-oracle-y-nombre-libre]]: la rejilla sigue
  > siendo el camino principal, pero debajo aparece **«No estoy en la lista»**, que abre un
  > campo de texto. Ese texto **no crea una trabajadora del padrón**: crea una
  > **provisional**, que la administradora confirma o fusiona. Así nadie se queda sin marcar y
  > el padrón no se ensucia solo.
  >
  > [[adr-008-alcance-interfaz-y-cola-sin-senal]]: esa lista es una **rejilla de caras
  > filtrada por la sede elegida** — entre 5 y 18 en vez de 140 — con nombre y apellido. Sin
  > teclado, y de paso expone una sede en lugar de las siete. Texto libre produciría duplicados que rompen el
  > agrupado del cronograma ([[formato-cronograma-excel]]). Añade al alcance una pantalla de
  > registro de trabajadoras.
- **RF-03 — Selección de sede.** Segundo campo: menú desplegable con las 7 sedes
  (ver [[sedes]]).
- **RF-04 — ~~Tipo de marcación.~~** ~~Dos botones explícitos: **entrada** o **salida**.~~

  > **Derogado el 2026-09-07** por [[adr-008-alcance-interfaz-y-cola-sin-senal]]: **no se
  > pregunta, se infiere**. Si hay un turno abierto, la siguiente marcación lo cierra; si
  > no, abre uno. `Turno` ya lo sabía hacer ([[marcacion-multiple]]), así que esto no toca
  > el modelo — solo quita un paso de la pantalla.
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

  > **Matizado el 2026-09-06** por [[adr-002-arquitectura-produccion]]: las fotos **no van en
  > la base**, que solo guarda la ruta. Guardarlas como `BLOB` degradaría el rendimiento a
  > este volumen.
  >
  > **Corregido el 2026-09-07:** aquel ADR decía «al disco del servidor», pero
  > [[adr-004-despliegue-azure]] y [[adr-005-hosting-largo-plazo]] las movieron al
  > **almacén de objetos** de la nube. La nota se había quedado atrás.

- **RF-14 — Login de administrador.** *(nuevo, 2026-09-06)* El acceso al dashboard, a la
  exportación y al registro de trabajadoras exige usuario y contraseña. Ver
  [[adr-003-login-y-cronograma-simple]]. **No afecta a RF-01**: la trabajadora sigue entrando
  por un enlace público sin credenciales.

- **RF-15 — El cronograma solo escribe horas.** *(nuevo, 2026-09-06)* La celda generada
  lleva pares `hora de ingreso - hora de salida`, o queda vacía. `DESCANSO`, `NO TURNO`,
  `INASISTENCIA` y las tardanzas **se manejan a mano** fuera del sistema.

  > **Corregido el 2026-09-06** por [[adr-006-dashboard-y-turnos-multiples]]: la celda lleva
  > **hasta seis pares, uno por línea**, no un solo rango colapsado.

- **RF-16 — Vista diaria del dashboard.** *(nuevo)* Una sede y un día: cada trabajadora en una
  fila con **todos sus turnos** y sus horas sumadas.
- **RF-17 — Corrección de marcaciones olvidadas.** *(nuevo)* La administradora puede añadir una
  salida que nadie marcó. **No edita nada**: inserta una marcación con `origen =
  ADMINISTRADORA`, sin foto, con motivo obligatorio, y queda distinguida a la vista.
- **RF-18 — Historial por trabajadora.** *(nuevo)* Sus marcaciones en un rango de fechas,
  **cruzando las 7 sedes**. Es la única vista que no filtra por una sola sede.
- **RF-19 — Totales de horas.** *(nuevo)* Suma por trabajadora y por sede en un rango. Es el
  dato que el Excel llenado a mano nunca pudo dar.
> **RF-20 matizado el 2026-09-20** por [[adr-010-sin-rechazos-oracle-y-nombre-libre]]: el cargo **sale de la
> plantilla del cronograma, de la exportación y de las vistas del dashboard**. El campo se
> conserva en la ficha de la trabajadora, y admite nulo.

- **RF-20 — Cargo de la trabajadora.** *(nuevo)* Cada trabajadora es **podóloga o cajera** —
  confirmado que no hay más cargos— y el cargo aparece como columna del cronograma.
- **RF-21 — Jornada completa o media.** *(nuevo, 2026-09-06)* Independiente del cargo: hay
  podólogas y cajeras de ambos tipos. **Se muestra, no se valida.** Ver
  [[adr-007-seguridad-endpoint-publico]].
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

- **RNF-05 — HTTPS obligatorio.** *(nuevo, 2026-09-06)* Sin certificado válido el navegador
  móvil **no concede acceso a la cámara**, y RF-05 deja de funcionar. Es un requisito
  funcional disfrazado de infraestructura. Ver [[adr-002-arquitectura-produccion]].

  > **Resuelto sin costo** por [[adr-004-despliegue-azure]]: etiqueta DNS de Azure
  > (`<nombre>.<region>.cloudapp.azure.com`) más certificado Let's Encrypt. No hace falta
  > comprar dominio.

- **RNF-06 — Subida rápida en red móvil.** *(nuevo, 2026-09-06)* La foto se comprime en el
  navegador antes de enviarse. Una imagen de cámara sin comprimir son 15–30 segundos de espera
  en 4G, y con RNF-01 esa fricción se traduce en marcaciones no registradas.

- **RF-22 — Ninguna marcación se rechaza por su secuencia.** *(nuevo, 2026-09-20)* Doble
  entrada, doble salida y varias marcaciones seguidas en la misma sede **se aceptan y se
  guardan**. `Turno` sigue infiriendo si abre o cierra, pero **sugiere, no bloquea**. Ver
  [[adr-010-sin-rechazos-oracle-y-nombre-libre]].
- **RF-23 — Registro por nombre libre.** *(nuevo, 2026-09-20)* Si la trabajadora no está en la
  rejilla, puede escribir su nombre. Queda **provisional** hasta que la administradora la
  confirme o la fusione.
- **RF-24 — Exportación diaria.** *(nuevo, 2026-09-20)* Además de la semana de lunes a domingo,
  la administradora puede descargar **la hoja de un solo día**.
- **RF-25 — La advertencia de hora no se oculta.** *(nuevo, 2026-09-20)* Cuando la hora la puso
  el dispositivo y no el servidor, el aviso es **permanente**: aparece en todas las vistas y
  **viaja al Excel** como asterisco junto a la hora, con leyenda al pie.
- **RNF-09 — Ráfaga de más de 80 personas.** *(nuevo, 2026-09-20)* El sistema no se degrada
  cuando ochenta trabajadoras marcan en la misma franja. La medida que importa es **acotar
  cuántas imágenes se decodifican a la vez**; el resto es dimensionado.
- **RNF-07 — Resistencia al abuso del endpoint público.** *(nuevo, 2026-09-06)* El formulario
  de marcación no tiene autenticación (RF-01) y acepta subida de archivos, así que necesita
  barreras: límite de tamaño, validación real de imagen contra bombas de descompresión,
  cooldown por trabajadora y **tope duro de 12 marcaciones** por día y sede. Ordenadas de la
  más barata a la más cara de evaluar. Ver [[adr-007-seguridad-endpoint-publico]].

  > **Sin CAPTCHA, y es deliberado.** Rompería RNF-01, que es el requisito principal del
  > cliente. Se acepta algo más de exposición a cambio de que el sistema se use.

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

  > ✅ **Resuelto el 2026-09-06: ODS 8, Trabajo decente y crecimiento económico.** Un registro
  > con evidencia fotográfica y hora puesta por el servidor hace auditable la jornada y protege
  > las horas efectivamente trabajadas. Meta 8.8 como referencia. Ver
  > [[adr-005-hosting-largo-plazo]].
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
