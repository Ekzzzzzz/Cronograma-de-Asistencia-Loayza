/**
 * <h2>Capa 3 de 4 — MODELO</h2>
 *
 * El negocio de verdad: {@code Sede}, {@code Trabajadora}, {@code Marcacion},
 * {@code Turno} y {@code Jornada}, mas el almacen de fotos. Aqui es donde se
 * sabe que una jornada puede tener varios turnos, que un turno sin salida sigue
 * abierto, y que las horas se suman turno a turno y nunca restando los extremos.
 *
 * <p><b>Esta capa no sabe que existe la web.</b> No importa nada de Spring MVC
 * ni de Thymeleaf. Si se pudiera borrar la capa Vista entera, todo lo de aqui
 * seguiria teniendo sentido.
 *
 * <h3>Patrones que viven aqui</h3>
 * <ul>
 *   <li><b>Singleton</b> — el catalogo de las siete sedes. Son fijas: se cargan
 *       una vez al arrancar y todas las pantallas leen esa misma copia.</li>
 *   <li><b>Composite</b> — la jerarquia semana → dia → jornada → turno.
 *       Preguntar "cuantas horas" se responde igual en cualquier nivel.</li>
 *   <li><b>Bridge</b> — separa <i>que</i> se guarda de <i>donde</i> se guarda:
 *       el dominio pide "guarda esta foto" y la implementacion decide si acaba
 *       en Oracle Object Storage o en el disco local de pruebas.
 *       <i>Por confirmar al escribir la clase.</i></li>
 *   <li><b>Adapter</b> — traduce el SDK de la nube al vocabulario del dominio,
 *       para que los nombres de Oracle no se cuelen dentro del negocio.
 *       <i>Por confirmar al escribir la clase.</i></li>
 * </ul>
 *
 * <h3>Dos reglas que se repiten en todo el modelo</h3>
 * <ul>
 *   <li><b>Una marcacion es inmutable.</b> Corregir es insertar otra fila con
 *       {@code origen = ADMINISTRADORA} y un motivo obligatorio, nunca editar.</li>
 *   <li><b>El servidor es el dueno de la hora.</b> Si la puso el dispositivo,
 *       queda marcado como tal y se ve siempre.</li>
 * </ul>
 */
package pe.loayza.asistencia.modelo;
