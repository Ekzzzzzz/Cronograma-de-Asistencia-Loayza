/**
 * <h2>Capa 2 de 4 — CONTROL</h2>
 *
 * Orquesta los casos de uso completos. El caso central es
 * <i>registrar una marcacion</i>, que por dentro son cinco pasos en orden:
 * sellar la hora del servidor, comprimir y guardar la foto, emparejar con el
 * turno abierto, escribir la fila y publicar el evento.
 *
 * <p><b>Esta capa decide el orden, no las reglas.</b> Las reglas de cuando un
 * turno abre o cierra viven en {@link pe.loayza.asistencia.modelo}; aqui solo
 * se decide a quien se llama y en que secuencia, y que pasa si un paso falla.
 *
 * <h3>Patrones que viven aqui</h3>
 * <ul>
 *   <li><b>Facade</b> — {@code RegistrarMarcacion}. Una sola puerta para los
 *       cinco pasos: quien la llama no conoce ni el orden ni los participantes.
 *       Es lo que impide que la capa Vista tenga que saber como funciona el
 *       sistema por dentro.</li>
 *   <li><b>Factory</b> — decide si lo que llega es una entrada o una salida,
 *       mirando si esa trabajadora tiene un turno abierto en esa sede.</li>
 * </ul>
 *
 * <h3>Regla que no se puede romper</h3>
 * Desde el 2026-09-20, <b>ninguna marcacion se rechaza por su secuencia</b>.
 * Doble entrada, doble salida o varias seguidas en la misma sede se aceptan y
 * se guardan todas. La fabrica <i>sugiere</i> si abre o cierra; no valida.
 * Ver {@code wiki/decisiones/adr-010-sin-rechazos-oracle-y-nombre-libre.md}.
 */
package pe.loayza.asistencia.control;
