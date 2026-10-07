package pe.loayza.asistencia.bd;

import java.util.List;
import java.util.Map;

/**
 * Saca valores tipados del mapa que devuelve {@code SimpleJdbcCall.execute()}.
 *
 * <p>Ese mapa es {@code Map<String, Object>}: guarda juntos los parametros
 * {@code OUT} (por su nombre, p. ej. {@code p_id}) y los conjuntos de filas
 * (por la clave que se dio en {@code returningResultSet}). Sacar una lista de
 * ahi exige un <i>cast</i> que el compilador no puede comprobar. Se escribe
 * una vez, aqui, en vez de repetirlo con un {@code @SuppressWarnings} en cada
 * clase.
 */
final class Resultados {

    private Resultados() {
    }

    /** Las filas guardadas bajo esa clave. Nunca nulo: sin filas, lista vacia. */
    @SuppressWarnings("unchecked")
    static <T> List<T> lista(Map<String, Object> resultado, String clave) {
        Object valor = resultado.get(clave);
        return valor == null ? List.of() : (List<T>) valor;
    }

    /**
     * Un parametro {@code OUT} numerico. MySQL lo devuelve como {@code Long},
     * {@code Integer} o {@code BigInteger} segun el tipo declarado en el
     * procedimiento; {@link Number} los cubre a todos.
     */
    static Long numero(Map<String, Object> resultado, String parametro) {
        Object valor = resultado.get(parametro);
        return valor == null ? null : ((Number) valor).longValue();
    }

    /**
     * Un parametro {@code OUT BOOLEAN}. En MySQL {@code BOOLEAN} es
     * {@code TINYINT(1)}, y el driver puede devolverlo como {@code Boolean} o
     * como numero segun la version: se aceptan los dos.
     */
    static boolean verdad(Map<String, Object> resultado, String parametro) {
        Object valor = resultado.get(parametro);
        if (valor instanceof Boolean b) {
            return b;
        }
        return valor instanceof Number n && n.intValue() != 0;
    }
}
