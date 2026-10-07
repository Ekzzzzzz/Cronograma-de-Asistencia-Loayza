package pe.loayza.asistencia.bd;

import pe.loayza.asistencia.modelo.TipoMarcacion;

/**
 * Lo que se sabe de hoy <b>antes</b> de que la trabajadora pulse el boton. Lo
 * devuelve {@code sp_contexto_marcacion}, que antes se llamaba
 * {@code sp_validar_marcacion} y ya no valida nada (ADR-012).
 *
 * <p><b>Sirve para ayudar, nunca para rechazar.</b> Con
 * {@code ultimoTipo} la pantalla preselecciona el boton contrario y le ahorra
 * un toque; si ella pulsa el otro, se guarda el otro. Que boton sugerir lo
 * decide {@link TipoMarcacion#siguienteSugerido()}, en el modelo, no aqui.
 *
 * @param marcacionesHoy cuantas lleva hoy en esa sede.
 * @param ultimoTipo     la ultima que hizo hoy en esa sede, o nulo si aun no
 *                       marco.
 */
public record ContextoMarcacion(int marcacionesHoy, TipoMarcacion ultimoTipo) {
}
