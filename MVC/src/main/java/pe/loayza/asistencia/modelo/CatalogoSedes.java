package pe.loayza.asistencia.modelo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Patron Singleton — las siete sedes, cargadas una sola vez</h2>
 *
 * Una unica copia del catalogo de sedes para toda la aplicacion.
 *
 * <p><b>Que problema real resuelve.</b> El desplegable de sedes sale en cada
 * marcacion; el dashboard lo pinta una vez por pestana; la exportacion lo
 * consulta por cada hoja. Son siete filas que <b>no cambian nunca</b>: no hay
 * pantalla para crear sedes, se siembran en {@code SQL/03_datos_prueba.sql}.
 * Ir a la base a leerlas en cada pantalla seria pagar un viaje por un dato que
 * ya se sabe, y en la rafaga del cambio de turno —mas de 80 personas a la vez—
 * esos viajes son justo los que no sobran.
 *
 * <h3>Por que asi y no de otras maneras</h3>
 * <ul>
 *   <li><b>La instancia es {@code static final}.</b> La crea el cargador de
 *       clases, que ya garantiza que ocurre una sola vez y de forma segura
 *       entre hilos. El doble chequeo con {@code synchronized} que suele
 *       ensenarse resuelve un problema que aqui no existe, y es mas facil de
 *       escribir mal que bien.</li>
 *   <li><b>Los datos son {@code volatile} y se reemplazan enteros.</b> No se
 *       modifica el mapa: se construye uno nuevo y se cambia la referencia.
 *       Asi un hilo que este leyendo mientras se recarga ve el catalogo viejo
 *       completo o el nuevo completo, nunca uno a medias.</li>
 * </ul>
 *
 * <h3>La objecion honesta, y por que se mantiene</h3>
 * En una aplicacion Spring esto se haria con un {@code @Bean}, que ya es un
 * singleton y ademas se puede sustituir en las pruebas. El Singleton clasico
 * tiene fama justificada de dificultar las pruebas: es estado global.
 *
 * <p>Se mantiene por dos razones, y la segunda es la buena: PC-3 pide el
 * patron <b>en la capa modelo</b>, y esta capa <b>no conoce Spring</b> — es su
 * regla principal. Un {@code @Bean} aqui obligaria a que el modelo importara
 * el framework, que es exactamente lo que no puede pasar. El costo se paga con
 * {@link #limpiar()}, que existe solo para que cada prueba empiece en blanco.
 */
public final class CatalogoSedes {

    private static final CatalogoSedes INSTANCIA = new CatalogoSedes();

    private volatile Map<Integer, Sede> porId = Map.of();

    private CatalogoSedes() {
        // Nadie mas puede crear una. Es el punto del patron.
    }

    public static CatalogoSedes instancia() {
        return INSTANCIA;
    }

    /**
     * Carga el catalogo. Lo llama la capa control al arrancar, leyendo de la
     * base; el modelo no sabe de donde salen las sedes, solo las guarda.
     */
    public void cargar(List<Sede> sedes) {
        if (sedes == null || sedes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El catalogo de sedes no puede quedar vacio: sin sedes no se puede marcar");
        }
        Map<Integer, Sede> nuevo = new LinkedHashMap<>();
        for (Sede s : sedes) {
            nuevo.put(s.id(), s);
        }
        this.porId = Map.copyOf(nuevo);
    }

    /** Si todavia no se ha cargado. Arrancar sin catalogo es un fallo grave. */
    public boolean vacio() {
        return porId.isEmpty();
    }

    /**
     * La sede con ese id.
     *
     * @throws IllegalArgumentException si no existe. Es deliberado que reviente
     *         en vez de devolver nulo: un id de sede que no esta en el catalogo
     *         solo puede venir de un formulario manipulado o de un error de
     *         programacion, y las dos cosas hay que verlas, no tragarlas.
     */
    public Sede porId(int id) {
        Sede s = porId.get(id);
        if (s == null) {
            throw new IllegalArgumentException("No existe la sede " + id);
        }
        return s;
    }

    /** Las que salen en el desplegable, en el orden en que se cargaron. */
    public List<Sede> activas() {
        return porId.values().stream().filter(Sede::activa).toList();
    }

    public List<Sede> todas() {
        return List.copyOf(porId.values());
    }

    /**
     * Vacia el catalogo. <b>Solo para las pruebas.</b> Es el precio de tener
     * estado global: sin esto, una prueba que carga sedes dejaria el catalogo
     * contaminado para la siguiente, y el orden de ejecucion pasaria a
     * importar.
     */
    public void limpiar() {
        this.porId = Map.of();
    }
}
