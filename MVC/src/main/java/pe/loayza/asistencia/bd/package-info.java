/**
 * <h2>Capa 4 de 4 — BASE DE DATOS</h2>
 *
 * La frontera con MySQL. Cada clase de aqui hace una cosa: llamar a
 * procedimientos almacenados y convertir lo que devuelven en objetos.
 *
 * <p><b>Aqui no hay ningun patron asignado, y es a proposito.</b> PC-3 reparte
 * los patrones entre Vista, Control y Modelo, y deja esta capa como scripts y
 * procedimientos. Meter uno aqui solo para que no quede vacia seria
 * <i>Golden Hammer</i>, que es justo lo que el capitulo de antipatrones
 * penaliza.
 *
 * <h3>Las tres herramientas</h3>
 * <ul>
 *   <li><b>{@code SimpleJdbcCall}</b> — llama a un procedimiento leyendo su
 *       firma del catalogo de MySQL. Uno por procedimiento, creado una vez.</li>
 *   <li><b>{@code RowMapper}</b> — una fila, un objeto. A mano cuando el objeto
 *       valida o tiene enums ({@code MarcacionMapper}); automatico con
 *       {@code DataClassRowMapper} para las filas planas de los listados.</li>
 *   <li><b>{@code @Transactional}</b> — solo en
 *       {@link pe.loayza.asistencia.bd.MarcacionesBD#registrar}, que es la unica
 *       operacion que escribe dos cosas que tienen que ir juntas.</li>
 * </ul>
 *
 * <h3>Los 20 procedimientos, por clase</h3>
 * <pre>
 *   MarcacionesBD     sp_contexto_marcacion, sp_registrar_marcacion,
 *                     sp_actualizar_jornada, sp_marcaciones_del_dia,
 *                     sp_historial_trabajadora
 *   CronogramaBD      sp_cronograma_semanal, sp_cronograma_diario,
 *                     sp_dia_sede, sp_totales_horas
 *   IncidenciasBD     sp_registrar_incidencia, sp_incidencias,
 *                     sp_marcar_incidencia_revisada
 *   TrabajadorasBD    sp_registrar_provisional, sp_provisionales, sp_padron
 *   AdministradoresBD sp_autenticar_admin, sp_marcar_acceso_admin
 *   FotosBD           sp_registrar_foto, sp_purgar_fotos
 *   SedesBD           sp_sedes
 * </pre>
 *
 * <h3>El circuito de una marcacion (ADR-013)</h3>
 * {@code sp_registrar_marcacion} inserta y devuelve el dia entero; el
 * <b>modelo</b> empareja los turnos ({@code Jornada.de}); y
 * {@code sp_actualizar_jornada} reescribe la celda. Todo en una transaccion
 * {@code READ COMMITTED}, con un candado por persona dentro del procedimiento
 * y un punto de guardado alrededor de la celda: <b>la celda puede perderse, la
 * marcacion nunca</b>.
 *
 * <h3>Dos reglas que no se pueden romper</h3>
 * <ul>
 *   <li><b>Nada de logica de negocio aqui.</b> Si una clase de este paquete
 *       empieza a decidir si un turno abre o cierra, esa decision se escapo
 *       del {@link pe.loayza.asistencia.modelo} y hay que devolverla a su
 *       sitio. Aqui se traduce y se guarda; no se decide.</li>
 *   <li><b>Ningun dato de entrada puede hacer fallar una marcacion.</b> MySQL
 *       esta en modo estricto y un texto mas largo que su columna daria error:
 *       por eso {@code Recorte} corta la nota, nunca la marcacion.</li>
 * </ul>
 *
 * <p>El SQL vive fuera de este paquete, en la carpeta {@code SQL/} de la raiz
 * del repositorio, numerado por orden de ejecucion. Estas clases solo lo
 * invocan.
 */
package pe.loayza.asistencia.bd;
