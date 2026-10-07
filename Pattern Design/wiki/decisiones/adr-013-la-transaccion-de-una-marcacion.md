---
titulo: ADR-013 — La transacción de una marcación
tipo: decision
estado: estable
fuentes:
  - MVC/src/test/java/pe/loayza/asistencia/bd/CircuitoDeMarcacionTest.java
  - MVC/src/test/java/pe/loayza/asistencia/bd/CruceModeloContraBaseTest.java
  - instrucciones del usuario (2026-10-06)
actualizado: 2026-10-06
tags: [decision, base-de-datos, transacciones, concurrencia, bd]
---

# ADR-013 — La transacción de una marcación

## Contexto

El usuario pidió cerrar el circuito `sp_registrar_marcacion` → `Jornada` →
`sp_actualizar_jornada` **dentro de una transacción** (2026-10-06). Es lo que ya prometía la
`## Tensiones` de [[modelo-datos]]: las dos llamadas en la misma transacción de Spring.

Al escribirlo aparecieron **dos fallos que un `@Transactional` a secas no resuelve**, y los dos
rompen la regla que más manda en este sistema —**no se rechaza ninguna marcación**,
[[adr-012-sin-cargos-y-sin-rechazos]]—. No se razonaron solamente: **se escribió primero la
versión ingenua y una prueba contra MySQL que la demostrara rota**, y solo después se arregló.

| Fallo | Medido con la versión ingenua |
|---|---|
| **A. Todo o nada.** Si la segunda mitad falla, se deshace también la marcación | La marcación 512 del día **se perdió**: `Out of range value for column 'p_turnos'` |
| **B. Carrera.** Dos envíos simultáneos de la misma persona leen el día a la vez y el último pisa la celda | **5 de 5 rondas** de 10 envíos simultáneos dejaron la celda mal; una decía **7 minutos donde había 5** |

El fallo B es justamente el **doble toque** del botón, que [[adr-012-sin-cargos-y-sin-rechazos]]
reconoce como caso real. Y no solo pierde horas: también puede **inventarlas**.

## Decisión

```
BEGIN  (READ COMMITTED)
  sp_registrar_marcacion   → SELECT … FOR UPDATE sobre la trabajadora, INSERT, y el día entero
  SAVEPOINT
    Jornada.de(día)         → el modelo empareja (regla 4: solo en Java)
    sp_actualizar_jornada   → reescribe la celda
  si falla: ROLLBACK TO SAVEPOINT   (se pierde la celda, nunca la marcación)
COMMIT
```

1. **Un candado por persona.** `sp_registrar_marcacion` empieza con
   `SELECT id … FROM trabajadora WHERE id = p_trabajadora FOR UPDATE`. El segundo envío de la
   misma persona **espera** unos milisegundos en vez de leer a la vez. Se bloquea la fila de
   `trabajadora` y no la celda porque esa fila **siempre existe**: bloquear una celda que aún no
   existe crea un *gap lock*, y dos *gap locks* no se excluyen entre sí.
2. **`READ COMMITTED`** en vez del `REPEATABLE READ` por defecto, para que el envío que esperó
   lea con seguridad lo que confirmó el anterior.
3. **Un punto de guardado** (`PROPAGATION_NESTED`, un *savepoint* de JDBC) alrededor de la
   segunda mitad. Si calcular o guardar la celda falla, se vuelve al punto de guardado y **la
   marcación se confirma igual**. Si falla incluso volver al punto de guardado, el error **sube**:
   decir «guardada» de una marcación que quizá no lo está sería peor que cualquier error.

## Justificación

- **La celda es derivable; la marcación no.** La celda se recalcula **entera** desde el día
  completo en cada marcación, así que si una vez no se guarda, la arregla la siguiente
  marcación de ese día. La marcación es el hecho, y no hay de dónde reconstruirla.
- **El candado no rechaza: espera.** Y solo espera la misma persona; las otras 98 no se
  enteran. Como todo camino que escribe una celda pasa antes por el candado de la persona, el
  orden de bloqueo es siempre el mismo y **no puede haber interbloqueos** entre dos marcaciones.
- **Va en el procedimiento, no en Java**, porque el SQL vive en `SQL/`
  ([[adr-011-jpa-o-sql-a-mano]]) y porque así nadie puede olvidarse de pedirlo.

## Consecuencias

**Se gana:** con los arreglos, las mismas pruebas pasan sin tocar una línea. **25 de 25
rondas** de 10 envíos simultáneos con la celda exacta, y la marcación 512 guardada aunque su
celda no.

**Cuesta:** dos sentencias más por marcación (`SAVEPOINT` y `RELEASE SAVEPOINT`), y el segundo
envío de una misma persona espera a que confirme el primero. Las dos cosas son de milisegundos.

**Se aplica la misma regla a los textos.** MySQL está en modo estricto: un `VARCHAR(500)` que
recibe 501 caracteres **da error y deshace la llamada**. Una nota larga habría hecho perder la
marcación entera. `Recorte` corta **la nota, nunca la marcación**, contando caracteres reales y
no unidades UTF-16 (un emoji son dos en Java y uno en MySQL), y termina en `…` para que el
corte se vea. Lo mismo para el motivo, el detalle de una incidencia y el nombre de una
provisional.

**Y las fracciones de segundo se cortan en Java.** `DATETIME` no guarda fracciones y MySQL
**redondea**: las `9:00:59.9` se guardarían como `9:01:00`, una hora posterior a la real.

## Lo que encontró la primera prueba contra datos reales

Antes de escribir una sola línea del circuito, `CruceModeloContraBaseTest` recalculó en Java
**las 1 178 celdas** que el semillero había calculado en SQL. **Cinco no coincidían, y en las
cinco tenía razón Java:** los «casos raros» de `03_datos_prueba.sql` insertaban marcaciones sin
rehacer su celda —el mismo fallo que los conteos MAÑANA/TARDE congelados del Excel del
cliente—. Corregido el semillero, coinciden **1 180 de 1 180**, carácter por carácter.

## Lo que este ADR no resuelve

- **`jornada_diaria.turnos` es `TINYINT UNSIGNED`: 255 como máximo.** Sin tope de marcaciones
  es un tope escondido. Gracias al punto de guardado ya no cuesta una marcación, pero a partir
  de ahí la celda se congela. Hueco 30 de [[huecos-abiertos]].
- **No hay tipo de incidencia para «celda no recalculada».** Hoy queda en el log como `ERROR`,
  que nadie mira. Hueco 31.

## Enlaces

[[modelo-datos]] · [[adr-011-jpa-o-sql-a-mano]] · [[adr-012-sin-cargos-y-sin-rechazos]] ·
[[cuatro-capas]] · [[huecos-abiertos]] · [[marcacion-multiple]]
