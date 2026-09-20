/**
 * <h2>Capa 4 de 4 — BASE DE DATOS</h2>
 *
 * La frontera con MySQL. Cada clase de aqui hace una cosa: llamar a un
 * procedimiento almacenado y convertir el resultado en objetos del
 * {@link pe.loayza.asistencia.modelo}.
 *
 * <p><b>Aqui no hay ningun patron asignado, y es a proposito.</b> PC-3 reparte
 * los patrones entre Vista, Control y Modelo, y deja esta capa como scripts y
 * procedimientos. Meter uno aqui solo para que no quede vacia seria
 * <i>Golden Hammer</i>, que es justo lo que el capitulo de antipatrones
 * penaliza.
 *
 * <h3>Por que procedimientos almacenados y no un ORM</h3>
 * No es una preferencia: PC-3 los exige como evidencia entregable. Pero
 * ademas hay una razon tecnica real en {@code sp_registrar_marcacion}:
 * insertar la marcacion y recalcular la jornada del dia tienen que ser
 * <b>atomicos</b>, y resolverlo dentro del motor evita un ida y vuelta extra
 * desde Java en el momento de mas carga.
 *
 * <p>El SQL vive fuera de este paquete, en la carpeta {@code SQL/} de la raiz
 * del repositorio, numerado por orden de ejecucion. Estas clases solo lo
 * invocan.
 *
 * <h3>Regla que no se puede romper</h3>
 * <b>Nada de logica de negocio aqui.</b> Si una clase de este paquete empieza
 * a decidir si un turno abre o cierra, esa decision se escapo del
 * {@link pe.loayza.asistencia.modelo} y hay que devolverla a su sitio.
 */
package pe.loayza.asistencia.bd;
