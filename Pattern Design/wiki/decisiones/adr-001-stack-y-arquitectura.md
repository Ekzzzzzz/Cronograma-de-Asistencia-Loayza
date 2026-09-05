---
tipo: decision
titulo: ADR-001 — Stack y arquitectura general
numero: 001
estado_adr: aceptada
tags: [adr, arquitectura, stack]
creado: 2026-09-04
actualizado: 2026-09-05
---

# ADR-001 — Stack y arquitectura general

## Contexto

El programa debe resolver [[problema-cronogramas]] y ser **usable de verdad** en Podología
Loayza, no sólo entregable de curso *(aporte del usuario, 2026-09-04)*.

Restricción académica: el profesor exige lenguaje orientado a objetos donde se puedan
aplicar patrones de diseño; **Spring Boot es aceptable** *(aporte del usuario)*.

Con [[adr-002-canal-de-marcacion]], el sistema deja de leer WhatsApp y pasa a tener **canal
propio**: las trabajadoras marcan desde una PWA.

## Decisión

**Frontend PWA en Vercel + backend Spring Boot protegido con JWT + base de datos.**

### Backend — Spring Boot

- Java + Spring Boot, Maven vía *wrapper* (`./mvnw`): resuelve que en este equipo no hay
  Maven ni Gradle instalados.
- API REST consumida por la PWA y por el panel de administración.
- **Spring Security + JWT** — ver [[adr-003-autenticacion-jwt]].
- **Base de datos como almacén.** El Excel es sólo formato de **exportación**, nunca el
  almacén: usar la hoja de cálculo como base de datos es la causa raíz de todos los fallos
  documentados en [[formato-cronograma-actual]].
- Paquete raíz: `pe.loayza.cronograma`.

### Frontend — dos aplicaciones, un despliegue

Ambas en Vercel, ambas *responsive*, con necesidades muy distintas:

| | App de marcación | Panel de administración |
|---|---|---|
| Usuaria | Trabajadora, poco manejo informático | Administradora |
| Dispositivo | Celular, siempre | Celular **y** PC, indistinto |
| Diseño | *Mobile-first* extremo: un botón grande, cero decisiones | Denso en información, tablas, filtros |
| Función | Cámara → comentario opcional → enviar | Cola de revisión, padrón, cronograma, exportar Excel |

La app de marcación se instala como **PWA** en la pantalla de inicio: se abre como si fuera
una app, sin pasar por el navegador, y funciona con conexión intermitente.

### La tubería

```
1. Recepción      POST desde la PWA: foto + comentario + coordenadas + token de captura
2. Validación     Cadena de verificaciones antifraude → veredicto y confianza  [[antifraude]]
3. Identificación Reconocimiento facial contra el padrón: ¿es quien dice ser?
4. Tipificación   ¿ingreso o salida? — por las marcaciones previas del día, y se le muestra
5. Registro       Persistir la marcación con toda su evidencia
6. Consolidación  Recalcular la jornada y el cronograma de la semana
```

Toda marcación con veredicto dudoso va a la **cola de revisión** en vez de aceptarse o
rechazarse en silencio ([[antifraude]], principio 3).

## Justificación

- Spring Boot da inyección de dependencias, que es el andamio natural para intercambiar
  implementaciones —verificaciones antifraude, reconocedor facial, exportadores— sin tocar a
  quien las usa. Los patrones del curso **se aplican de verdad**, no quedan decorativos.
- Separar la app de marcación del panel evita el error clásico de una sola interfaz que no
  sirve bien a ninguno de los dos perfiles.
- La base de datos elimina de raíz tres de los fallos del proceso manual: vocabulario
  inconsistente, conteos desactualizados y celdas mal tecleadas.

## Consecuencias

**A favor**
- Automatización de punta a punta, sin intervención manual ni riesgo legal.
- El Excel se genera siempre correcto y recalculado, en el formato que ya conocen.
- Queda espacio explícito para la revisión humana, imprescindible con reconocimiento facial.

**En contra**
- Superficie considerable: backend, dos frontends, base de datos, despliegue. Es un proyecto
  de verdad, no un ejercicio.
- **Datos biométricos en una base de datos.** Obliga a tomarse en serio `CLAUDE.md` §8:
  guardar plantillas faciales en vez de fotos crudas donde se pueda, cifrado en reposo, y
  acceso restringido por rol.
- El backend necesita hospedaje con almacenamiento persistente (Vercel sirve para el
  frontend, no para esto). **El despliegue queda deliberadamente sin decidir**
  *(decisión del usuario, 2026-09-05)*, lo que impone una restricción de diseño: **no
  atarse a ningún proveedor**. Almacenamiento de fotos detrás de una interfaz propia,
  persistencia con JPA y PostgreSQL, motor de reconocimiento facial por
  [[patron-adapter]]. Es exactamente la defensa contra el antipatrón **Vendor Lock-In**
  ([[antipatrones]]).

### Quiénes usan el sistema

**Podólogas y cajeras marcan igual** *(decisión del usuario, 2026-09-05)*: mismo flujo,
mismo padrón, sólo cambia el rol. Queda abierto si comparten cronograma o se exportan por
separado — el Excel actual sólo tiene podólogas ([[formato-cronograma-actual]]).

### Contingencia: cuando no se puede marcar

Sin batería, sin señal o con el celular roto, **la administradora registra la marcación a
mano** desde el panel *(decisión del usuario, 2026-09-05)*. Queda marcada como **registro
manual, no como marcación verificada**, y con rastro de auditoría: quién la registró, cuándo
y por qué.

Se descartó permitir marcar desde el celular de una compañera: choca con la vinculación de
dispositivo y abre la puerta a que se marquen entre ellas ([[antifraude]], vector 5).

Esta vía es exactamente el caso de uso de [[patron-command]]: una intervención humana sobre
el registro de asistencia que debe poder deshacerse y quedar auditada.

## Estado

**Aceptada** *(decisión del usuario, 2026-09-04)*.

S06–S14 ya están ingeridos ([[patrones-moc]]). Los ADR que decidan **qué patrón** va en cada
etapa son el siguiente paso, empezando por la cadena de verificaciones de [[antifraude]].

## Enlaces

- [[adr-002-canal-de-marcacion]] · [[adr-003-autenticacion-jwt]] · [[antifraude]]
- [[problema-cronogramas]] · [[formato-cronograma-actual]] · [[sintesis]] · [[decisiones-moc]]
