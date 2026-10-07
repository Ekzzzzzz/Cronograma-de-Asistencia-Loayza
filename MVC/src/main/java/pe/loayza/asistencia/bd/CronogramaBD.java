package pe.loayza.asistencia.bd;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * Lo que se lee de {@code jornada_diaria}: las celdas ya calculadas.
 *
 * <p>Esta clase <b>solo lee</b>. Las celdas las escribe
 * {@link MarcacionesBD#registrar}, en la misma transaccion que la marcacion
 * que las cambia; aqui se consultan para el dashboard y para el Excel.
 */
@Repository
public class CronogramaBD {

    private final SimpleJdbcCall semanal;
    private final SimpleJdbcCall diario;
    private final SimpleJdbcCall diaSede;
    private final SimpleJdbcCall totales;

    public CronogramaBD(JdbcTemplate jdbc) {
        // Las dos exportaciones, el MISMO mapper: es la promesa de ADR-012.
        this.semanal = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_cronograma_semanal")
                .returningResultSet("filas", FilaCronograma.MAPPER);
        this.diario = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_cronograma_diario")
                .returningResultSet("filas", FilaCronograma.MAPPER);
        this.diaSede = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_dia_sede")
                .returningResultSet("filas", FilaDiaSede.MAPPER);
        this.totales = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_totales_horas")
                .returningResultSet("filas", TotalHoras.MAPPER);
    }

    /**
     * La rejilla de lunes a domingo de una sede: cada persona por cada dia,
     * con las celdas vacias incluidas. Es la exportacion semanal.
     *
     * @throws IllegalArgumentException si {@code lunes} no es lunes. Esto no
     *         es una regla de negocio escondida: el procedimiento suma seis
     *         dias a lo que reciba, y con un miercoles devolveria una
     *         «semana» de miercoles a martes sin dar ningun error.
     */
    public List<FilaCronograma> semanal(int sedeId, LocalDate lunes) {
        if (lunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("La semana del cronograma empieza en lunes; llego " + lunes);
        }
        return Resultados.lista(semanal.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)
                .addValue("p_lunes", lunes)), "filas");
    }

    /** Un dia de una sede, con las mismas columnas que la semanal. */
    public List<FilaCronograma> diario(int sedeId, LocalDate fecha) {
        return Resultados.lista(diario.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)
                .addValue("p_fecha", fecha)), "filas");
    }

    /**
     * La pestana del dashboard: cada persona que marco en esa sede ese dia.
     * A diferencia del cronograma, aqui <b>si</b> salen las provisionales:
     * el dashboard es donde la administradora las descubre.
     */
    public List<FilaDiaSede> diaSede(int sedeId, LocalDate fecha) {
        return Resultados.lista(diaSede.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)
                .addValue("p_fecha", fecha)), "filas");
    }

    /**
     * Minutos por persona y sede entre dos fechas, ambas incluidas.
     *
     * @param sedeId una sede, o {@code null} para totalizar todas.
     */
    public List<TotalHoras> totales(Integer sedeId, LocalDate desde, LocalDate hasta) {
        return Resultados.lista(totales.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)
                .addValue("p_desde", desde)
                .addValue("p_hasta", hasta)), "filas");
    }
}
