package pe.loayza.asistencia.bd;

/**
 * Ajusta un texto al tamano de su columna <b>antes</b> de mandarlo a MySQL.
 *
 * <h3>Por que existe: es la regla 1 aplicada a los textos</h3>
 * MySQL esta en modo estricto. Si un parametro {@code VARCHAR(500)} recibe 501
 * caracteres, <b>no lo corta: da error</b>, y el error deshace la llamada
 * entera. Una nota demasiado larga haria perder la marcacion completa, y este
 * sistema no rechaza ninguna. Asi que se corta <b>la nota, nunca la
 * marcacion</b>.
 *
 * <p>El corte se ve: el texto acaba en {@code …}, para que nadie crea que la
 * nota decia solo eso.
 *
 * <h3>Por que cuenta «puntos de codigo» y no {@code length()}</h3>
 * {@code VARCHAR(500)} en {@code utf8mb4} son 500 <i>caracteres</i>. Java
 * cuenta en unidades UTF-16, donde un emoji ocupa dos. Cortar con
 * {@code substring(0, 500)} podria partir un emoji por la mitad y mandar a
 * MySQL medio caracter, que tambien es un error.
 */
final class Recorte {

    static final int NOTAS = 500;
    static final int MOTIVO = 200;
    static final int DETALLE = 200;
    static final int NOMBRE = 80;

    private static final String ELIPSIS = "…";

    private Recorte() {
    }

    static String aColumna(String texto, int maxCaracteres) {
        if (texto == null) {
            return null;
        }
        if (texto.codePointCount(0, texto.length()) <= maxCaracteres) {
            return texto;
        }
        int fin = texto.offsetByCodePoints(0, maxCaracteres - 1);
        return texto.substring(0, fin) + ELIPSIS;
    }
}
