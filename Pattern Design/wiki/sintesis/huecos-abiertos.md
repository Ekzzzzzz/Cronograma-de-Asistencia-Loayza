---
titulo: Huecos abiertos
tipo: sintesis
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-10-06
tags: [pendientes, decisiones, bloqueos]
---

# Huecos abiertos

Registro vivo de lo que falta saber, decidir o desbloquear. Se revisa en cada lint. Cuando
un hueco se cierra, se mueve a una página de [[decisiones]] y aquí se marca como resuelto.

## Bloqueos del entorno

**Actualizado 2026-09-05:** el toolchain quedó completo. Ya no hay bloqueos de entorno.

| Bloqueo | Estado | Nota |
|---|---|---|
| No hay Python 3 | ✅ **resuelto** | Python 3.14.7. Los tres scripts de `tools/` ya corren. |
| PDF ilegibles | ✅ **resuelto** | `python tools/extraer_pdf.py` extrae ~105 000 caracteres de PC-2 y PC-3. |
| No hay JDK | ✅ **resuelto** | Java y javac 26.0.2.1. |
| No hay Maven | ✅ **resuelto** | Maven 3.9.16 instalado a nivel de usuario, verificado con `mvn -v`. |
| `JAVA_HOME` sin definir | ✅ **resuelto** | Apunta a `C:\Program Files\Java\jdk-26.0.2.1`. |
| JDK 26 vs Spring Boot | ✅ **descartado** | Spring Initializr ofrece Java 26 con Boot 4.1.1. No hace falta otro JDK. |
| `CASO DE EJEMPLO 2.pdf` | ⚠️ **parcial** | Solo 510 caracteres extraíbles: es un PDF de imágenes. Necesitaría OCR. |

Un aviso que no bloquea pero muerde después: **`extraer_pdf.py` pierde tildes y ligaturas**
en algunos PDF (`presentacin`, `cient!co`). No citar literalmente de esos archivos sin
corregir el texto.

Todas las fuentes de `Archivos_de_clase/` son legibles salvo la excepción anotada, así que la
ingesta puede avanzar con el material completo.

## Preguntas para el usuario

1. ✅ **Resuelta (2026-09-06) — Identidad de la trabajadora.** Se elige de una **lista
   desplegable cerrada**; la administradora registra a las podólogas. Ver
   [[adr-002-arquitectura-produccion]]. Matiza [[requisitos]] RF-02 y añade al alcance la
   pantalla de registro de trabajadoras.
2. ✅ **Resuelta (2026-09-06) — Acceso de la administradora.** **Login obligatorio**:
   Spring Security con contraseñas BCrypt sobre la tabla `administrador`. Protege dashboard,
   exportación y alta de trabajadoras. Ver [[adr-003-login-y-cronograma-simple]].
3. ✅ **Resuelta (2026-09-06) — Descanso / No turno / Inasistencia.** **Se manejan a mano**,
   fuera del sistema. El sistema solo escribe horas. Se resolvió por reducción de alcance, no
   por diseño: desaparecieron la tabla `turno_planificado`, la pantalla de planificación y el
   patrón [[patron-state]].
4. ✅ **Resuelta (2026-09-07) — no entra por ahora.** La sede se elige de un desplegable de
   siete. El usuario: *«no queremos sobrecargar la aplicación»*. `tools/pluscode.py` queda
   como material para una mejora futura, **no como pendiente**. Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]].
5. ✅ **Resuelta (2026-09-07) — cerrada del todo.** El turno queda abierto
   (`ultima_salida IS NULL`), sin estado especial ni etiqueta escrita por el sistema
   ([[adr-003-login-y-cronograma-simple]]). Las dos partes que faltaban ya están: **la celda
   muestra `10:12AM -`** (pregunta 16) y **la administradora sí puede corregirla**
   (pregunta 15).
6. ✅ **Resuelta (2026-09-07), ampliada el 2026-09-20 — semanal y diaria.** La semana de lunes
   a domingo sigue siendo el formato de [[formato-cronograma-excel]], y ahora se añade **la hoja
   de un solo día** ([[adr-010-sin-rechazos-oracle-y-nombre-libre]]). Sigue sin haber vista mensual ni rangos
   libres: romperían las siete columnas fijas que la empresa reconoce.

## Preguntas nuevas tras ingerir PC-3

Ver [[pc3-entregable]]. Estas condicionan el informe, no solo el código:

7. ✅ **Resuelta (2026-09-06) — ODS 8, Trabajo decente y crecimiento económico**, con la
   meta 8.8 como referencia. Era el último hueco que bloqueaba la introducción del informe.
   Ver [[adr-005-hosting-largo-plazo]].
8. ⚠️ **Parcial — tamaño del equipo.** El usuario confirmó que **son dos personas**,
   programando a la par. Lo que **no** está confirmado es que el docente lo autorice: PC-3
   nombra los archivos `Apellidos1-2-3` y pide lecciones "por cada estudiante", lo que apunta
   a grupos de tres. **Hay que preguntarlo en clase.**
9. ✅ **Resuelta (2026-09-06) — Motor de base de datos: MySQL 8.** A este volumen MySQL y
   PostgreSQL rinden igual; se elige por cercanía a la sintaxis del curso y facilidad de
   hosting. Ver [[adr-002-arquitectura-produccion]] y [[modelo-datos]].
10. ✅ **Resuelta (2026-09-07) — renderizado en servidor.** Thymeleaf, con **HTMX** en el
    dashboard y JavaScript propio solo en el formulario de marcación —pasos, cámara,
    compresión, sello de hora y cola sin señal—. Un JAR y un servidor. Los patrones de la
    capa Vista se quedan **en el servidor**, que es donde [[pc3-entregable]] los evalúa.
    Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]].
11. **¿Dónde se ubican Decorator y Composite?** PC-3 los exige por familia pero no les
    asigna capa. Propuesta en [[mapa-patron-requisito]].
12. ✅ **Resuelta (2026-09-07) — arranca en blanco.** Los Excel viejos quedan como archivo y
    no hay tarea de importación. **Consecuencia directa: [[patron-adapter]] pierde su primera
    justificación** y ahora depende por completo de que `AlmacenFotosBlob` traduzca de verdad
    el SDK de la nube (pregunta 21). Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]].

## Preguntas nuevas tras ADR-002

Ver [[adr-002-arquitectura-produccion]]. Salen de tratar el proyecto como producción real:

13. ✅ **Resuelta (2026-09-06) — planificación de turnos.** No existe: esos casos se
    manejan a mano. La tabla `turno_planificado` y su pantalla se eliminaron del alcance.
14. ✅ **Resuelta (2026-09-06), y luego corregida el mismo día.** La celda lleva **un par por
    línea, hasta seis** ([[adr-006-dashboard-y-turnos-multiples]]). La respuesta anterior
    —colapsar a primera entrada – última salida— **era una mala lectura** de la indicación del
    usuario y se revirtió.
15. ✅ **Resuelta (2026-09-06) — sí puede, insertando.** La administradora añade la salida
    olvidada como **marcación nueva** con `origen = ADMINISTRADORA`, sin foto y con motivo
    obligatorio. Nada se edita ni se borra: la inmutabilidad se conserva y se gana
    trazabilidad. Ver [[adr-006-dashboard-y-turnos-multiples]].
16. ✅ **Resuelta (2026-09-07) — `10:12AM -`**, con el guion y el hueco a la vista, tal como
    lo proponía [[marcacion-multiple]]. Dejarla vacía la haría indistinguible de un día sin
    marcaciones y la administradora perdería la señal. Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]].
17. ✅ **Resuelta (2026-09-07) — se compra uno propio**, unos 40 soles al año, con
    certificado gratuito de Let's Encrypt. **Es una tarea con fecha, no una preferencia:**
    sin HTTPS válido el navegador móvil no abre la cámara y RF-05 deja de funcionar. Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]].
18. ✅ **Resuelta (2026-09-06) — respaldo.** `mysqldump` nocturno comprimido a un contenedor
    Blob en nivel frío; las fotos ya viven en Blob, que replica tres copias. Azure Backup se
    descartó por costo. Ver [[adr-004-despliegue-azure]].

## Preguntas nuevas tras ADR-004

Ver [[adr-004-despliegue-azure]]. El crédito de estudiante es de **100 USD y 12 meses**:

19. **¿La suscripción *Azure for Students* incluye los servicios gratuitos de 12 meses** —750 h
    mensuales de VM B1s, discos, 5 GB de Blob— **o solo entrega el crédito?** Sin ellos el
    crédito rinde unos 7 meses; con ellos, más de 18. **Cambia el resultado por un factor de
    dos** y se comprueba en el portal, no aquí.
20. ⚠️ **Parcial (2026-09-07) — depende de la dueña, que aún no ha visto el proyecto.** La
    cuenta de estudiante no lleva tarjeta: al agotarse, los recursos se detienen. La
    estrategia acordada es **presentarle el sistema ya funcionando sobre Oracle Always Free**,
    de modo que el costo mensual el día de la propuesta sea **cero** y la conversación no sea
    «¿financias esto?» sino «¿lo quieres?». **Riesgo anotado:** si se despliega en una cuenta
    personal de estudiante, los datos de asistencia de la empresa quedan en una cuenta a
    nombre del alumno. La cuenta de Oracle debe crearse con un correo **transferible** desde
    el primer despliegue.
21. **¿`AlmacenFotosBlob` es de verdad un Adapter?** Lo es si traduce el SDK de la nube a la
    interfaz del dominio; no lo es si acaba siendo una delegación de tres líneas. **Se decide
    escribiéndolo**, no antes. De ello depende que el proyecto tenga 7 u 8 patrones.

## Preguntas nuevas tras ADR-005

22. 🔴 **LA MÁS URGENTE (subida el 2026-09-20) — ¿Oracle dará capacidad para una instancia
    ARM?** Con [[adr-010-sin-rechazos-oracle-y-nombre-libre]] la plataforma **ya no es Azure sino Oracle Always
    Free**, así que esto dejó de ser un riesgo futuro: **es el riesgo principal del proyecto y
    bloquea la semana 1**. Los errores de *out of capacity* son frecuentes y a veces hay que
    reintentar durante días. Si no la da, la salida es Hetzner por ~5,50 USD al mes.
23. ✅ **Resuelta (2026-09-20) — no hay migración: se empieza en Oracle.** El usuario decidió
    trabajar directamente sobre la versión gratuita. Hay que verificar `aarch64` **antes de
    escribir nada**, no después. Ver [[adr-010-sin-rechazos-oracle-y-nombre-libre]].

## Preguntas nuevas tras ADR-008

Ver [[adr-008-alcance-interfaz-y-cola-sin-senal]]. Salen de reconciliar el prototipo con el wiki:

24. **¿Existen fotos de referencia de las trabajadoras?** La rejilla de caras las necesita. El
    prototipo dibuja iniciales sobre un color generado a partir del nombre, que funciona y no
    cuesta nada — pero una foto real es mucho más rápida de reconocer para quien va a tocarla.
    Si no existen, hay que decidir si se toman al dar de alta a cada trabajadora.
25. **¿Se prueba el prototipo con una podóloga real antes de desplegar?** Lo pide el propio
    prototipo en su pie. Es la única forma de saber si «toca tu cara» se entiende sin que nadie
    lo explique, y una tarde de prueba ahorra semanas de rediseño.
26. **¿Con qué correo se crea la cuenta de la nube?** Ver la pregunta 20: si es el correo
    universitario de un alumno, migrar después es rehacer el despliegue entero.

## Preguntas nuevas tras ADR-010

27. 🔴 **¿El tope de 12 marcaciones también cae?** El usuario dijo el 2026-09-06 que *«es
    importante un límite»* y el 2026-09-20 que *«no debe invalidar nada»*. Las dos no pueden ser
    verdad a la vez. La propuesta de [[adr-010-sin-rechazos-oracle-y-nombre-libre]] es conservar el tope
    **solo como defensa del endpoint público**, nunca como validación de la jornada, y bajar el
    cooldown de 2 minutos a 30 segundos. **Falta confirmarlo.** Si cae del todo, el formulario
    público se queda sin ningún límite superior.
28. **¿El campo `cargo` se sigue usando en algún sitio?** Sale de la plantilla, de la
    exportación y de las vistas. Si tampoco aparece en el alta de trabajadoras, es una columna
    que nadie lee y conviene quitarla del esquema en vez de dejarla muerta.
29. **¿Qué pasa con una marcación provisional que nadie confirma?** Se guarda y espera, pero sin
    un plazo acaba siendo una bandeja que crece sola. Conviene acordar cada cuánto se revisa.

> **Resueltos en [[adr-012-sin-cargos-y-sin-rechazos]]:** el 27 (sí, el tope de 12 cae del
> todo, junto con el cooldown y la cola vencida) y el 28 (`cargo` no se usaba en ningún sitio
> y se eliminó). Anotado aquí el 2026-10-06; ADR-012 los cerraba pero esta página no se
> había actualizado.

## Preguntas nuevas tras ADR-013 (capa `bd`)

30. **`jornada_diaria.turnos` es `TINYINT UNSIGNED`, máximo 255.** Sin tope de marcaciones es
    un tope escondido. Desde [[adr-013-la-transaccion-de-una-marcacion]] ya no cuesta ninguna
    marcación, pero a partir de ahí la celda se congela. Pasarlo a `SMALLINT` es un cambio de
    una palabra en `01_esquema.sql` y en `sp_actualizar_jornada`. **Falta decidirlo.**
31. **No hay incidencia para «celda no recalculada».** Si falla la segunda mitad del circuito,
    hoy queda un `ERROR` en el log, que nadie mira. ¿Se añade `CELDA_NO_RECALCULADA` al `ENUM` de
    `incidencia` y a `TipoIncidencia`?
32. ✅ **Resuelto el 2026-10-06.** Faltaban dos procedimientos: listar las sedes (sin él, el
    Singleton `CatalogoSedes` no tenía de dónde cargarse) y listar el padrón para «¿Quién
    eres?». Añadidos `sp_sedes` y `sp_padron`; el padrón **no devuelve el documento**, porque
    alimenta un formulario público.
33. ✅ **Resuelto el 2026-10-06 en [[adr-014-marcar-sin-esperar]].** La foto chocaba con la
    inmutabilidad de la marcación. Ahora vive en `foto_marcacion`, llega en un envío aparte
    **después** de la marcación, y ponerla es insertar en otra tabla, no editar. La regla 2 manda guardar la
    marcación **antes** de procesar la foto, pero `foto_ruta` se escribe en el `INSERT`, la
    marcación no se edita nunca, y no hay procedimiento que ponga la ruta después. Opción
    natural: generar la clave de la foto (un UUID) antes de insertar y guardar el archivo
    después; pero si el archivo falla, la fila apuntaría a una foto que no existe. Es una
    decisión de la capa `control`.
34. **`sp_purgar_fotos` marca pero nadie borra.** Pone `foto_purgada = TRUE`, y ningún
    procedimiento devuelve qué archivos hay que borrar del almacén. Sin eso, la retención de 90
    días es solo una bandera.
35. **Una marcación de una trabajadora o sede que no existe sí falla**, por las claves foráneas
    de `marcacion`. No es un rechazo por contenido sino por identidad, y la capa `control` tiene
    que garantizar antes que la persona existe (o crearla como provisional).
36. **¿Cuántas trabajadoras usan iPhone?** En iPhone una marcación pendiente no se envía en
    segundo plano, ni con la página instalada ([[adr-014-marcar-sin-esperar]]): llega cuando
    vuelve a abrirse la página. Saber cuántas son dice cuánto importa.
37. **¿Qué se hace con una foto cuya clave no existe?** `sp_registrar_foto` devuelve vacío y no
    rompe nada, pero la capa `control` tiene que decidir: guardar el archivo y reintentar
    asociarlo, o descartarlo con una incidencia. No debería pasar —el celular manda la foto
    después del ✓✓—, y por eso mismo conviene que deje rastro si pasa.

## Huecos de contenido del wiki

**Actualizado 2026-09-06.** Los huecos anteriores de esta sección quedaron cerrados: S06–S14
y PC-3 están ingeridas, `wiki/patrones/` tiene ocho páginas y [[modelo-datos]] ya existe.

Lo que falta:

- **S01–S04 y las guías de taller siguen sin ingerir.** Son el material de principios de
  diseño y del **ISP** que exige [[pc3-entregable]] (RC-05), y hoy no hay ninguna página que
  lo cubra. `factory-decorator.docx` además trae código Java muy cercano al dominio.
- **No hay página de ISP**, pese a ser un criterio del informe. El caso está identificado: la
  interfaz `ComponenteCronograma` obligaba a `Marcacion` a devolver `Duration.ZERO`, y el
  nivel `Turno` de [[marcacion-multiple]] lo corrige. Falta escribirla.
- **No hay página de endpoints ni de la interfaz de usuario.**
- **No se ha escrito una sola línea de código.** No existe el proyecto Maven, ni el esquema
  SQL, ni los stored procedures que define [[modelo-datos]].
