---
titulo: ADR-011 — ¿@Entity o SQL a mano?
tipo: decision
estado: estable
fuentes:
  - propuesta del usuario (2026-09-20)
  - Archivos_de_clase/PC-3 (Anexo 1, RC-03)
actualizado: 2026-09-20
tags: [decision, base-de-datos, jpa, jdbc, pc3]
---

# ADR-011 — ¿`@Entity` o SQL a mano?

## La propuesta

El usuario, antes de empezar el esquema (2026-09-20):

> «Se pueden crear las tablas con los métodos `@Entity`, `@Table`, etc. de Spring Boot, así
> que no sé cómo quisieras trabajarlo mejor pero creo sería mejor utilizar estos métodos.»

La propuesta es correcta técnicamente y merece respuesta, no un «ya estaba decidido». Con JPA
se anotan las clases, `spring.jpa.hibernate.ddl-auto` crea las tablas solas y desaparecen tanto
el `CREATE TABLE` como el código que traduce filas a objetos. Es menos trabajo, y en la mayoría
de proyectos Spring es lo que se hace.

## La decisión

**El SQL se escribe a mano y es la única fuente de verdad del esquema.** No entra JPA.

## Por qué

### 1. `SQL/` es un entregable calificado, no un detalle de implementación

[[pc3-entregable]] exige la carpeta `SQL/` **por nombre, en el anexo**, y RC-03 pide
procedimientos almacenados como evidencia. Los scripts hay que entregarlos igual.

Eso convierte la pregunta en otra distinta: no es «JPA o SQL», es **«SQL, o SQL *más* JPA»**. Y
la segunda opción tiene el problema de siempre: dos definiciones del mismo esquema que se
separan en cuanto alguien toca una y olvida la otra. El día que no coincidan, el que manda es el
que corrió en la máquina, y el otro —el entregable— se entrega mal sin que nadie lo note.

### 2. Tres cosas del modelo no las expresa JPA

No es una objeción de purista; son columnas que ya existen en [[modelo-datos]]:

- **`marcacion.fecha` es una columna generada y almacenada** (`DATE(momento)`). Es lo que hace
  que filtrar por rango de días use el índice en vez de anularlo. JPA la mapea como
  `@Generated` de solo lectura, pero **no la crea**: el `DDL` de Hibernate la escribiría como
  una columna normal y el índice dejaría de servir, en silencio.
- **`jornada_diaria` tiene clave primaria compuesta y sin `id`** —`(sede_id, fecha,
  trabajadora_id)`— precisamente para que InnoDB la guarde físicamente en el orden de la
  consulta del cronograma. En JPA eso es una `@IdClass` o un `@Embeddable`: más ceremonia para
  describir algo que en SQL son tres palabras.
- **`sp_registrar_marcacion` hace dos cosas en una transacción.** Un ORM que quiere manejar la
  persistencia y un procedimiento que escribe por detrás conviven mal: hay que vaciar la caché
  de primer nivel a mano para que Hibernate no siga creyendo lo que ya no es cierto.

### 3. Los patrones del curso quedan mejor sin ORM

Esto es lo que más pesa para la nota. **`Jornada` → `Turno` → `Marcacion` es el Composite**
([[patron-composite]]), y es un objeto que se **construye** a partir de una lista de
marcaciones, no una fila que se **carga** de una tabla. Si `Jornada` fuera una `@Entity`, sería
a la vez objeto de dominio y fila mapeada, y el emparejamiento tendría que esquivar a Hibernate
en vez de apoyarse en él.

Con JDBC, la capa `modelo` es Java normal: se prueba con un `new` y una lista, sin base de
datos, sin contexto de Spring. Eso es lo que hace que las pruebas por capa de la semana 3 sean
escribibles en una tarde.

### 4. `ddl-auto` no se lleva a producción

Hibernate genera el esquema comparando lo que hay con lo que espera. En desarrollo va bien; en
un servidor con datos reales, es el mecanismo que un día decide que sobra una columna. Este
sistema guarda **evidencia de jornada laboral**, y esa carpeta [[adr-002-arquitectura-produccion]]
ya la trata como algo que no se borra.

## Lo que la propuesta tenía de razón, y que sí se recoge

El fondo de la idea era **no escribir a mano la traducción entre filas y objetos**, y eso no
hace falta sufrirlo:

- `SimpleJdbcCall` invoca los procedimientos **leyendo sus parámetros del catálogo del motor**:
  no hay que escribir la lista de `?`.
- `RowMapper` convierte cada fila en un objeto en una línea por campo.
- `sp_registrar_marcacion` devuelve **el día entero ya ordenado** en la misma llamada. No hay
  N+1 que evitar, porque no hay navegación perezosa que lo cause.

El código de la capa `bd` no va a ser más largo que el de un repositorio JPA. Lo que cambia es
quién manda: manda el SQL, que es lo que se entrega.

## Revisión honesta del costo

**Sin JPA se escribe más código de lectura.** Cada procedimiento necesita su `RowMapper`. Si
este proyecto tuviera treinta tablas y CRUD sobre todas, la decisión sería la contraria.

Con **seis tablas y dieciséis procedimientos**, ese costo es una tarde. La entrega es en menos
de un mes y el criterio que decide es cuál de los dos caminos deja el entregable de `SQL/`
correcto sin trabajo extra. Es este.

## Enlaces

[[modelo-datos]] · [[pc3-entregable]] · [[cuatro-capas]] · [[patron-composite]] ·
[[estructura-repositorio]] · [[adr-002-arquitectura-produccion]]
