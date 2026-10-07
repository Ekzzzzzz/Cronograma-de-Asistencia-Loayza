package pe.loayza.asistencia.bd;

import pe.loayza.asistencia.modelo.ContadorDeHoy;
import pe.loayza.asistencia.modelo.Jornada;

/**
 * Lo que devuelve guardar una marcacion. <b>Si existe este objeto, la
 * marcacion esta guardada</b>, pase lo que pase con la celda.
 *
 * @param id               el que le dio MySQL.
 * @param marcacionesHoy   cuantas lleva hoy en esa sede, contando esta.
 * @param jornada          la celda recalculada. <b>Nula solo si
 *                         {@code celdaActualizada} es falso.</b>
 * @param celdaActualizada falso si la celda del cronograma no se pudo
 *                         reescribir. La marcacion sigue guardada; la celda
 *                         se corrige sola con la siguiente marcacion de ese
 *                         dia, porque cada vez se recalcula entera.
 * @param repetida         era un reenvio de una marcacion que ya estaba
 *                         guardada (misma clave de envio): no se guardo otra,
 *                         y {@code id} es el de la original. Para el celular es
 *                         un exito igual que cualquier otro: ✓✓.
 */
public record MarcacionRegistrada(
        long id,
        int marcacionesHoy,
        Jornada jornada,
        boolean celdaActualizada,
        boolean repetida) {

    /** «Marcacion 3 de 4 de hoy», lo que ve la trabajadora al terminar. */
    public ContadorDeHoy contador() {
        return ContadorDeHoy.para(marcacionesHoy);
    }
}
