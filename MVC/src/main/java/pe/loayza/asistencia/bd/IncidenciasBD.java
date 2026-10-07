package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * La contrapartida de no rechazar nada: <b>ensenar bien lo que entro</b>.
 *
 * <p>Sin esta tabla, un sistema que lo acepta todo aceptaria en silencio y
 * nadie se enteraria de nada (ADR-012).
 */
@Repository
public class IncidenciasBD {

    private final SimpleJdbcCall registrar;
    private final SimpleJdbcCall listar;
    private final SimpleJdbcCall marcarRevisada;

    public IncidenciasBD(JdbcTemplate jdbc) {
        this.registrar = new SimpleJdbcCall(jdbc).withProcedureName("sp_registrar_incidencia");
        this.listar = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_incidencias")
                .returningResultSet("filas", FilaIncidencia.MAPPER);
        this.marcarRevisada = new SimpleJdbcCall(jdbc).withProcedureName("sp_marcar_incidencia_revisada");
    }

    public void registrar(IncidenciaNueva i) {
        registrar.execute(new MapSqlParameterSource()
                .addValue("p_momento", i.momento())
                .addValue("p_marcacion", i.marcacionId())
                .addValue("p_ip", i.ip())
                .addValue("p_trabajadora", i.trabajadoraId())
                .addValue("p_sede", i.sedeId())
                .addValue("p_tipo", i.tipo().name())
                .addValue("p_detalle", Recorte.aColumna(i.detalle(), Recorte.DETALLE)));
    }

    /**
     * Las incidencias entre dos momentos, las mas recientes primero.
     *
     * @param soloPendientes deja fuera lo ya revisado, para que la lista no
     *                       crezca sin fin.
     */
    public List<FilaIncidencia> listar(LocalDateTime desde, LocalDateTime hasta, boolean soloPendientes) {
        return Resultados.lista(listar.execute(new MapSqlParameterSource()
                .addValue("p_desde", desde)
                .addValue("p_hasta", hasta)
                .addValue("p_solo_pendientes", soloPendientes)), "filas");
    }

    public void marcarRevisada(long id) {
        marcarRevisada.execute(new MapSqlParameterSource().addValue("p_id", id));
    }
}
