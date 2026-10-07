package pe.loayza.asistencia.bd;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * Las fotos de las marcaciones, que desde ADR-014 viven en su propia tabla
 * ({@code foto_marcacion}) y llegan <b>despues</b> que la marcacion.
 *
 * <p>Esta clase guarda <b>donde</b> esta cada foto y su retencion; el archivo
 * en si no vive en MySQL.
 */
@Repository
public class FotosBD {

    private final SimpleJdbcCall registrar;
    private final SimpleJdbcCall purgar;

    public FotosBD(JdbcTemplate jdbc) {
        this.registrar = new SimpleJdbcCall(jdbc).withProcedureName("sp_registrar_foto");
        this.purgar = new SimpleJdbcCall(jdbc).withProcedureName("sp_purgar_fotos");
    }

    /**
     * Asocia una foto a la marcacion que se envio con esa clave.
     *
     * <p>Se busca por <b>clave de envio</b> y no por id porque el celular puede
     * no conocer el id: es justo el caso en que la respuesta de la marcacion
     * se perdio por el camino. Reenviar la misma foto no la duplica.
     *
     * @return el id de la marcacion, o vacio si no hay ninguna con esa clave.
     *         Vacio no es un error de la trabajadora: la capa control decide.
     */
    public Optional<Long> registrarFoto(String claveEnvio, String ruta, String miniRuta) {
        Long marcacion = Resultados.numero(registrar.execute(new MapSqlParameterSource()
                .addValue("p_clave_envio", claveEnvio)
                .addValue("p_ruta", ruta)
                .addValue("p_mini_ruta", miniRuta)), "p_marcacion");
        return Optional.ofNullable(marcacion);
    }

    /**
     * Marca como purgadas las fotos de marcaciones con mas de {@code dias}
     * dias. La marcacion y su miniatura se quedan: siguen siendo evidencia de
     * jornada laboral.
     *
     * <p>El mismo procedimiento lo corre cada noche el evento
     * {@code ev_purga_fotos} de MySQL; este metodo existe para poder lanzarlo
     * a mano.
     *
     * @return cuantas fotos se marcaron.
     */
    public int marcarPurgadas(int dias) {
        return Resultados.numero(purgar.execute(new MapSqlParameterSource()
                .addValue("p_dias", dias)), "p_afectadas").intValue();
    }
}
