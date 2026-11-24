---
tipo: decision
titulo: ADR-007 — Flujo consolidado de la v1 (producción y exposición)
numero: 007
estado_adr: aceptada
tags: [adr, arquitectura, flujo, pwa, patrones, despliegue]
creado: 2026-11-23
actualizado: 2026-11-23
reemplaza: "partes de [[adr-001-stack-y-arquitectura]], [[adr-002-canal-de-marcacion]], [[adr-003-autenticacion-jwt]], [[adr-005-interfaz-de-marcacion]] y [[adr-006-acceso-sin-sesion]]"
---

# ADR-007 — Flujo consolidado de la v1 (producción y exposición)

## Contexto

Entre el 2026-09-04 y el 2026-09-05 se tomaron seis decisiones que se fueron corrigiendo
entre sí: sesión por dispositivo (ADR-003), enlace personalizado, enlace global (ADR-006),
selector de caras, jornadas múltiples ([[jornadas-multiples]]). Varias páginas quedaron
contradiciéndose.

El 2026-11-23 el usuario fija las condiciones finales *(decisiones del usuario, 2026-11-23)*:

- **Entrega y exposición el 2026-11-24**, con unas 22 h de margen. Exposición de **8
  minutos**: hay que mostrar en vivo cómo el celular marca y cómo la laptop recibe y ordena
  los datos. **Que la demo no falle pesa más que el costo**: se acepta pagar hosting.
- El sistema debe quedar **en producción**, no sólo como entregable.
- **Sin reconocimiento facial.** La foto se sube como evidencia; no se compara con nada.
- El equipo son **dos**: el usuario y el agente.
- Los datos del repositorio son **de prueba**.

Este ADR es la **fuente de verdad del flujo**. Donde un ADR anterior diga otra cosa, manda
éste.

## Decisión

### 1. Flujo de la trabajadora — enlace global, cuatro pasos

```
SEDE  →  NOMBRE  →  FOTO  →  ENVIAR ENTRADA / ENVIAR SALIDA  →  ¡Listo!
```

| Paso | Qué ve | Detalle |
|---|---|---|
| **Sede** | Las sedes activas como botones grandes | El celular **recuerda la última** y la deja preseleccionada |
| **Nombre** | Lista de trabajadoras de esa sede **+ buscador** | Al escribir, la lista **se va reduciendo** (como las sugerencias de Google). Si no está en la sede, el buscador busca también en las demás (rotación). Botón **«+»** para registrarse: nombre y apellido. El celular **recuerda el último nombre** |
| **Foto** | Cámara en vivo | Al abrirse, el servidor emite el **token de captura** (frescura) |
| **Enviar** | Foto en pequeño, sede y nombre, nota opcional plegada | El botón dice **ENVIAR ENTRADA** o **ENVIAR SALIDA**: lo calcula el servidor (¿tiene jornada abierta?), ella no elige nada |

**Desde la segunda vez, el camino normal es: foto → enviar.** Sede y nombre ya vienen puestos.

Por qué una **lista con buscador** y no texto libre *(propuesta del usuario, refinada por el
agente)*: el nombre escrito a mano es lo que produjo las **dos LAURA** y `MARY P.` en
[[formato-cronograma-actual]]. Elegir del padrón da un dato limpio, sin perder la
sensación de «escribo mi nombre».

**El botón «+»** crea a la trabajadora en estado **POR VALIDAR**. Su marcación se registra
igual, pero va a revisión hasta que la administradora confirme que es una persona real del
padrón. Así nadie queda sin marcar el primer día, y nadie puede inflar el padrón con nombres
falsos sin que se vea.

### 2. Sin comparación facial

La foto es **evidencia**, como hoy en WhatsApp, pero con hora sellada por el servidor y
ubicación verificada. La administradora la ve en el panel. Se elimina el motor de
reconocimiento de la v1, y con él toda su complejidad.

**Riesgo asumido, dicho claro:** el vector 4 de [[antifraude]] («que una compañera marque
por ella») queda cubierto por la **evidencia fotográfica y la auditoría humana**, igual que
en el proceso actual — no peor. Lo que cambia es que hora y ubicación ya no se pueden
falsear.

### 3. Dispositivo — QR + llave no exportable, en modo «señal»

Una PWA **no puede leer el IMEI**: el navegador no lo expone. En su lugar:

1. En el panel, la administradora pulsa **«Activar celular»** y aparece un **QR de un solo
   uso** (válido 10 min).
2. El celular lo escanea. La PWA genera un **par de llaves con WebCrypto marcado como no
   exportable**: la llave privada no puede leerse ni copiarse a otro equipo, ni siquiera
   por JavaScript.
3. El servidor guarda la llave pública. En cada marcación, el celular **firma el token de
   captura**; el servidor verifica la firma.

**Modo señal** *(decisión del usuario)*: un celular **no activado puede marcar igual**, pero
la marcación va a revisión. Nadie se queda sin registrar el día del lanzamiento; con el
tiempo, todos los celulares se activan y la cola baja sola.

### 4. Administración — link secreto + usuario y contraseña

- El panel vive en una **ruta secreta** configurable por variable de entorno, que no se
  comparte *(decisión del usuario)*.
- Además pide **usuario y contraseña**. Razón: un link por sí solo es una contraseña que
  queda en el historial, en capturas de pantalla y en los chats — y detrás están las fotos
  de todo el personal.
- **Sólo las administradoras** ven las fotos y los registros de todas las trabajadoras.
- **Las sedes se crean y editan sólo desde el panel.**
- Segundo factor (2FA): **v2**.

**Cambio respecto de [[adr-003-autenticacion-jwt]]: JWT sale, entra sesión de servidor con
cookie `HttpOnly`.** JWT se había elegido porque frontend y backend vivían en dominios
distintos. Con un solo despliegue (punto 5) ese problema desaparece, y la cookie de sesión
de Spring Security es más segura (JavaScript no la puede leer) y trae protección CSRF de
serie. Las trabajadoras no tienen sesión ([[adr-006-acceso-sin-sesion]]).

### 5. Un solo despliegue, cuatro capas

**Cambio respecto de [[adr-001-stack-y-arquitectura]]: se abandona Vercel.** Spring Boot
sirve la PWA, el panel y la API desde el mismo dominio:

- desaparecen CORS y el manejo de credenciales entre dominios;
- un solo servicio que vigilar el día de la exposición;
- el panel se renderiza en Java (Thymeleaf), así que los patrones de la **capa Vista** son
  código Java real, que es lo que pide la rúbrica de PC-3 ([[fuente-pc2-pc3-entregables]]).

| Pieza | Elección |
|---|---|
| Backend | Java 21 + Spring Boot 3, Maven *wrapper* |
| Base de datos | PostgreSQL gestionado en **Neon** |
| Hosting | **Render**, plan de pago (no se suspende por inactividad) |
| Fotos | En la base de datos en la v1, **detrás de la interfaz `AlmacenFotos`**: cambiarlas a S3 después no toca a nadie más ([[antipatrones]], Vendor Lock-In). Se comprimen en el celular (~100 KB) antes de subir |
| Excel | Apache POI, sólo **exportación** |

**Sin conexión: v2.** La v1 exige señal para marcar. El modo diferido de
[[adr-002-canal-de-marcacion]] queda como mejora posterior.

### 6. Patrones por capa

Cada patrón está porque resuelve algo concreto del negocio *(propuesta del agente)*.

| Capa | Patrón | Clase | Qué resuelve en Loayza |
|---|---|---|---|
| **Vista** | [[patron-observer]] | `PublicadorDeMarcaciones` → `PanelEnVivo`, `BitacoraDeAuditoria` | Al registrarse una marcación, el panel de la laptop se actualiza **en vivo** y queda auditada |
| | [[patron-command]] | `AccionAdministrativa`: `Aprobar`, `Rechazar`, `CorregirHora`, `RegistroManual` | Cada intervención de la administradora es un objeto: se registra, se audita y **se deshace** |
| | [[patron-memento]] | `RecuerdoDeMarcacion` | Guarda el estado previo que `deshacer()` de `CorregirHora` restaura |
| | [[patron-proxy]] | `AlmacenFotosProtegido` | Proxy de protección: sólo una administradora obtiene una foto, y cada acceso queda registrado |
| | [[patron-bridge]] | `Reporte` × `FormatoSalida` | Reporte semanal por sede o por trabajadora × Excel o HTML, sin una clase por combinación |
| **Control** | [[patron-facade]] | `ServicioDeMarcacion` | Un único punto de entrada a la tubería: el controlador REST no conoce los pasos |
| | [[patron-factory]] | `FabricaDeFormatos` | Elige el formato de salida por nombre (`xlsx`, `html`) |
| **Modelo** | [[patron-singleton]] | `CalendarioFeriadosPeru` | Los feriados son únicos y globales; se cargan una sola vez |
| | [[patron-prototype]] | `RejillaSemanal.clonar()` | La rejilla base (días, cabeceras) se clona por sede y semana |
| | [[patron-builder]] | `Marcacion.builder()` | La marcación junta datos de varias fuentes (formulario, servidor, verificaciones) |
| | [[patron-state]] | `EstadoMarcacion` | `REGISTRADA`, `EN_REVISION`, `APROBADA`, `RECHAZADA`: las transiciones ilegales son imposibles |
| | [[patron-composite]] | `VerificacionCompuesta` | Las verificaciones antifraude se combinan en un árbol y dan un veredicto |
| | [[patron-decorator]] | `VerificacionConEvidencia` | Envuelve cualquier verificación y guarda su resultado como evidencia ([[antifraude]], principio 4) |
| **Base de datos** | *Stored procedures* | `sp_consolidar_semana`, `sp_resumen_del_dia` | Los conteos MAÑANA/TARDE **se recalculan siempre** en la base: nunca se copian |

**Descartados con argumento**, que también se exponen:

- [[patron-abstract-factory]]: no hay familias de productos que deban ser coherentes.
- [[patron-adapter]]: su caso de uso era el motor de reconocimiento facial, que sale de la
  v1. Vuelve el día que haya motor.

**Principio ISP** (Unidad I): interfaces pequeñas y separadas — `Verificacion`,
`AlmacenFotos`, `FormatoSalida`, `ObservadorDeMarcaciones`, `AccionAdministrativa`. Ninguna
clase implementa métodos que no usa.

**ODS 8 — Trabajo decente y crecimiento económico**: un registro de asistencia justo y
auditable, que no acusa a nadie automáticamente ([[adr-004-llenado-del-cronograma]]).

### 7. Antifraude de la v1

Verificaciones en el servidor, todas como hojas del Composite:

| Verificación | Si falla |
|---|---|
| Token de captura vigente y sin usar (frescura) | Revisión |
| Geocerca sensible a la precisión ([[sedes]]) | Revisión |
| Dispositivo activado (firma válida) | Revisión |
| Trabajadora validada (no «por validar») | Revisión |
| Intervalo mínimo entre marcaciones | Revisión |
| **Límite de 6 marcaciones por día** ([[jornadas-multiples]]) | **Rechazo** con mensaje claro; queda registrado como intento |

La hora la pone **siempre el servidor**. Nada se descarta en silencio
([[antifraude]], principio 3).

## Consecuencias

**Qué reemplaza este ADR:**

| ADR | Queda vigente | Reemplazado |
|---|---|---|
| [[adr-001-stack-y-arquitectura]] | Spring Boot, base de datos como almacén, Excel como exportación, registro manual por la administradora | Vercel → un solo despliegue en Render + Neon |
| [[adr-002-canal-de-marcacion]] | PWA propia, se descarta WhatsApp, hora sellada por el servidor | Sesión abierta y «primera = entrada»; modo sin conexión pasa a v2 |
| [[adr-003-autenticacion-jwt]] | Rol administradora con login obligatorio | JWT → sesión con cookie `HttpOnly`; 2FA → v2 |
| [[adr-005-interfaz-de-marcacion]] | Los 8 principios de interfaz | Pantallas → sede, nombre, foto, enviar |
| [[adr-006-acceso-sin-sesion]] | Enlace global, sin login para trabajadoras | Selector de caras → lista con buscador; el rostro deja de verificarse |

**A favor**
- Flujo de cuatro pasos que, desde la segunda vez, se reduce a dos.
- Un solo despliegue: menos piezas que puedan fallar el día de la exposición.
- Los patrones resuelven problemas reales del negocio, y se pueden señalar en el código.

**En contra**
- Sin reconocimiento facial, la suplantación entre compañeras se detecta sólo revisando las
  fotos. Es el mismo nivel que hoy, no mejor.
- Sin modo sin conexión en la v1.
- Hasta que se activen los celulares, muchas marcaciones irán a revisión (modo señal).

## Plan de contingencia para la exposición

1. **Principal:** la app en Render, con URL `https` fija. Se abre 10 minutos antes.
2. **Respaldo:** la misma app corriendo en la laptop, expuesta con un túnel de Cloudflare.
3. **Último recurso:** el vídeo de la demo, que PC-3 exige de todos modos.
4. Compartir datos desde el celular (*hotspot*) por si el wifi del aula falla.

## Estado

**Aceptada** *(decisiones del usuario, 2026-11-23)*.

## Enlaces

- [[adr-001-stack-y-arquitectura]] · [[adr-002-canal-de-marcacion]] · [[adr-003-autenticacion-jwt]]
- [[adr-004-llenado-del-cronograma]] · [[adr-005-interfaz-de-marcacion]] · [[adr-006-acceso-sin-sesion]]
- [[antifraude]] · [[sedes]] · [[jornadas-multiples]] · [[fuente-pc2-pc3-entregables]] · [[decisiones-moc]]
