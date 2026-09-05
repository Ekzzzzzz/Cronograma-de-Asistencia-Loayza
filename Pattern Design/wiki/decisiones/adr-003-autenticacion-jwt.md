---
tipo: decision
titulo: ADR-003 — Autenticación y sesiones (Spring Security + JWT)
numero: 003
estado_adr: aceptada
tags: [adr, seguridad, jwt, sesiones]
creado: 2026-09-04
actualizado: 2026-09-04
---

# ADR-003 — Autenticación y sesiones (Spring Security + JWT)

## Contexto

Dos perfiles con necesidades opuestas *(decisión del usuario, 2026-09-04)*:

- **Trabajadoras**: poco o nada de manejo informático. Marcan dos veces al día desde su
  celular. **Escribir una contraseña dos veces al día es inaceptable**: si la interfaz
  fricciona, vuelven a WhatsApp.
- **Administradora**: audita la cola de revisión, gestiona el padrón y exporta el Excel,
  desde PC o desde su celular. Aquí la seguridad pesa más que la comodidad: ve datos de
  personal de toda la empresa.

Además el frontend vive en **Vercel** y el backend en otro dominio
([[adr-001-stack-y-arquitectura]]): son orígenes distintos, con las implicaciones de CORS y
de manejo de credenciales que eso trae.

## Decisión

**Spring Security con JWT**, y sesiones de duración distinta según el perfil.

- **Access token** de vida corta, enviado en cada petición.
- **Refresh token** de vida larga, que permite renovar sin volver a pedir credenciales. Es
  lo que hace que la trabajadora **entre y ya esté dentro**.
- **Dos roles**, con exigencias de acceso deliberadamente opuestas:

  | | `TRABAJADORA` | `ADMINISTRADORA` |
  |---|---|---|
  | Puede | Marcar y ver sus propias marcaciones | Padrón, cola de revisión, exportación, auditoría |
  | Acceso | **Nunca ve un login**: sesión abierta desde el enrolamiento | **Login obligatorio en cada sesión** |
  | Segundo factor | No | **Sí, obligatorio** *(decisión del usuario, 2026-09-05)* |

  La asimetría es intencionada: la trabajadora ve sólo lo suyo desde un dispositivo
  vinculado, mientras que la administradora ve datos de personal de toda la empresa —
  incluidas fotos y plantillas biométricas (`CLAUDE.md` §8). Ahí la seguridad pesa más que
  la comodidad.
- **El refresh token se ata al dispositivo** ([[antifraude]], vector 5): usarlo desde otro
  dispositivo obliga a reautenticar y avisa en el panel.
- **Revocación**: la administradora puede cerrar la sesión de un dispositivo desde el panel
  (cese, celular perdido o robado).

## Justificación

- El refresh token de vida larga resuelve el requisito humano —sesión persistente— sin
  dejar un token todopoderoso viajando en cada petición.
- Al ser sin estado, el backend escala y encaja con un frontend desplegado aparte.
- Los roles separan de forma limpia lo que puede ver cada perfil; Spring Security lo hace de
  forma declarativa, sin ensuciar la lógica de negocio.
- La vinculación al dispositivo convierte la sesión persistente —que es cómoda— en una
  señal antifraude, en vez de en un agujero.

## Consecuencias

- **Un JWT no se puede invalidar sin llevar estado.** La revocación exige guardar los
  refresh tokens en base de datos. Se acepta: sin revocación no se puede dar de baja a nadie.
- Hay que definir dónde guarda el token la PWA. `localStorage` es cómodo pero queda expuesto
  a XSS; una cookie `HttpOnly` + `Secure` + `SameSite` es más segura pero complica el
  despliegue en dominios distintos. **Punto a resolver al implementar.**
- El enrolamiento es **presencial** (ver abajo), lo que simplifica el flujo: no hace falta
  entregar credenciales ni gestionar recuperación de contraseña para las trabajadoras.
- Todo va por HTTPS, sin excepción.

## Enrolamiento presencial

*(decisión del usuario, 2026-09-05)*

La trabajadora se da de alta **en persona con la administradora**, en un solo acto:

1. La administradora crea su ficha en el padrón.
2. Le toma la **foto de referencia** — que hace falta igual para el reconocimiento facial, así
   que el enrolamiento no añade un paso, lo aprovecha.
3. Abre la PWA **en el celular de la trabajadora** y deja la sesión iniciada allí mismo.
4. El dispositivo queda **vinculado** en ese momento.

Por qué es la mejor opción de las tres consideradas:

- **La trabajadora nunca ve una contraseña.** No hay nada que olvidar, anotar en un papel ni
  prestar. Es la barrera de entrada más baja posible para usuarias con poco manejo
  informático, que es la restricción que manda ([[adr-002-canal-de-marcacion]]).
- **La vinculación de dispositivo nace correcta**: el sistema sabe desde el primer segundo
  cuál es el celular de cada quien, que es la defensa del vector 5 de [[antifraude]].
- Un enlace de un solo uso por WhatsApp habría sido cómodo a distancia, pero un enlace se
  reenvía: otra persona podría quedarse con la sesión.

**Coste:** exige que las 7 sedes pasen por un enrolamiento presencial. Es trabajo real de
puesta en marcha, y conviene planificarlo sede por sede.

### Enlace personalizado por trabajadora

*(propuesta del usuario, 2026-09-05, aceptada con un matiz)*

Cada trabajadora tiene **su propio enlace**, y la administradora crea con él un acceso
directo en la pantalla de inicio de su celular. Ella toca el ícono y entra directamente a
*su* app. Encaja perfectamente con el enrolamiento presencial y refuerza que nunca vea un
login.

> [!warning] El matiz que hay que respetar: **la URL no puede ser la credencial**
> Si el enlace por sí solo da acceso, entonces *el enlace es la contraseña* — y los enlaces
> se filtran: se reenvían por WhatsApp («mira, esta es la app»), salen en capturas de
> pantalla, quedan en el historial del navegador y se sincronizan entre dispositivos. Con
> usuarias acostumbradas a compartir todo por WhatsApp, es cuestión de tiempo.

**Cómo se resuelve, conservando la experiencia intacta:**

1. La administradora genera un **enlace de activación de un solo uso** y lo abre en el
   celular de la trabajadora, durante el enrolamiento.
2. Al abrirse, el enlace **se canjea** por una sesión ligada a ese dispositivo
   (*refresh token* vinculado) **y se quema**. Si alguien lo abre después, ya no sirve.
3. El acceso directo que queda en la pantalla de inicio apunta a la **URL personal
   permanente** de la trabajadora, que **identifica pero no autentica**: sirve para saludarla
   por su nombre y abrir su app, mientras que quien autoriza es la sesión del dispositivo.
4. Si esa URL se abre en otro celular, no hay sesión: aparece *«Pide a la administradora que
   active este celular»* ([[adr-005-interfaz-de-marcacion]]).

Así la trabajadora obtiene exactamente lo que el usuario pedía —un ícono personal, cero
login— sin que un enlace reenviado se convierta en la llave de la asistencia de otra
persona.

**Riesgo que permanece:** un celular desbloqueado y prestado. Es inherente a cualquier
sesión persistente, y la defensa es el reconocimiento facial ([[antifraude]], vector 4).

## Estado

**Aceptada** *(decisión del usuario, 2026-09-04)*.

## Preguntas abiertas

1. ¿Cuánto debe durar la sesión antes de pedir credenciales otra vez? ¿30 días, 90, nunca
   mientras use el mismo dispositivo?
2. ~~¿Segundo factor para la administradora?~~ → **sí, obligatorio**, junto con inicio de
   sesión en cada sesión *(decisión del usuario, 2026-09-05)*. Falta elegir el método:
   código por aplicación (TOTP), por SMS o llave física.
3. ¿Qué pasa cuando una trabajadora **cambia de celular**? Hace falta un flujo de
   revinculación que no obligue a repetir el enrolamiento completo.

## Enlaces

- [[adr-001-stack-y-arquitectura]] · [[adr-002-canal-de-marcacion]] · [[antifraude]]
- [[decisiones-moc]]
