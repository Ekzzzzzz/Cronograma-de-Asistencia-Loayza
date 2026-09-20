/**
 * <h2>Capa 1 de 4 — VISTA</h2>
 *
 * Todo lo que el usuario ve y toca. Aqui viven los controladores de Spring MVC
 * que devuelven plantillas de Thymeleaf, el endpoint publico de marcacion y los
 * filtros que lo protegen.
 *
 * <p><b>Esta capa no contiene reglas de negocio.</b> Recibe la peticion, se la
 * entrega a la capa {@link pe.loayza.asistencia.control} y devuelve lo que
 * aquella responda. Si alguna vez hay que decidir algo aqui sobre turnos,
 * horas o sedes, esa decision esta en el paquete equivocado.
 *
 * <h3>Patrones que viven aqui</h3>
 * <ul>
 *   <li><b>Proxy</b> — el portero. Exige sesion antes de dejar entrar al
 *       dashboard, y difiere la carga de las fotos grandes hasta que alguien
 *       las abre de verdad.</li>
 *   <li><b>Observer</b> — refresca la vista cuando se registra una marcacion,
 *       sin que quien la registro sepa que existe un dashboard.</li>
 *   <li><b>Decorator</b> — envuelve la foto para estamparle encima la fecha,
 *       la hora y la sede.</li>
 *   <li><b>Command</b> — la cola sin senal: una marcacion capturada como
 *       objeto, guardada en el telefono y ejecutada al volver la conexion.
 *       <i>Por confirmar al escribir la clase.</i></li>
 * </ul>
 *
 * <p>El nombre del paquete es {@code vista} y no {@code controller} a
 * proposito: quien abre este repositorio tiene que <b>ver</b> la arquitectura
 * de cuatro capas, no traducirla. Ver {@code wiki/arquitectura/estructura-repositorio.md}.
 */
package pe.loayza.asistencia.vista;
