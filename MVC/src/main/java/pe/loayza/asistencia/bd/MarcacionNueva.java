package pe.loayza.asistencia.bd;

import pe.loayza.asistencia.modelo.Marcacion;

/**
 * Lo que hace falta para guardar una marcacion: el hecho, mas los datos que
 * solo existen en el momento de guardarla.
 *
 * <p>Envuelve un {@link Marcacion} del modelo en vez de repetir sus campos,
 * para que las comprobaciones de su constructor —una marcacion sin tipo o sin
 * hora no existe— no tengan que escribirse dos veces. Su {@code id} se ignora:
 * lo pone MySQL.
 *
 * <h3>No lleva foto (ADR-014)</h3>
 * La foto llega despues, en otro envio, y se guarda con
 * {@link FotosBD#registrarFoto}. Asi la marcacion existe siempre antes que su
 * foto y nunca espera por ella.
 *
 * @param claveEnvio   el UUID que el celular genero al tocar el boton. Con el
 *                     se reconoce un reenvio y no se guarda dos veces. Nulo en
 *                     las que pone la administradora a mano.
 * @param corregidaPor id de la administradora, solo si la puso ella a mano.
 * @param motivo       obligatorio cuando la pone la administradora; eso lo
 *                     comprueba la capa control, no esta.
 */
public record MarcacionNueva(
        Marcacion marcacion,
        String claveEnvio,
        Integer corregidaPor,
        String motivo) {

    public MarcacionNueva {
        if (marcacion == null) {
            throw new IllegalArgumentException("No hay marcacion que guardar");
        }
    }

    /** El caso de casi siempre: la marca ella desde un celular. */
    public static MarcacionNueva deTrabajadora(Marcacion marcacion, String claveEnvio) {
        return new MarcacionNueva(marcacion, claveEnvio, null, null);
    }

    /** La pone la administradora a mano: sin clave de envio y con motivo. */
    public static MarcacionNueva correccion(Marcacion marcacion, int administradoraId, String motivo) {
        return new MarcacionNueva(marcacion, null, administradoraId, motivo);
    }
}
