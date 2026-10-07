package pe.loayza.asistencia.bd;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * El padron: la lista de «¿Quien eres?» y las provisionales. <b>Nadie se queda
 * sin marcar por no estar en la lista.</b>
 */
@Repository
public class TrabajadorasBD {

    private final SimpleJdbcCall registrarProvisional;
    private final SimpleJdbcCall provisionales;
    private final SimpleJdbcCall padron;

    public TrabajadorasBD(JdbcTemplate jdbc) {
        this.padron = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_padron")
                .returningResultSet("filas", FilaPadron.MAPPER);
        this.registrarProvisional = new SimpleJdbcCall(jdbc).withProcedureName("sp_registrar_provisional");
        this.provisionales = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_provisionales")
                .returningResultSet("filas", FilaProvisional.MAPPER);
    }

    /**
     * Da de alta a quien escribio su nombre, o devuelve la provisional que ya
     * existia con ese mismo nombre. Sin esa reutilizacion, quien marca entrada
     * y salida escribiendo su nombre las dos veces saldria como dos personas.
     *
     * <p>El nombre se limpia de espacios y se corta a 80 caracteres: un nombre
     * demasiado largo haria fallar el alta, y sin alta no hay marcacion.
     *
     * @return el id con el que marcar.
     */
    public int registrarProvisional(String nombre, Integer sedeId) {
        String limpio = Recorte.aColumna(nombre == null ? null : nombre.strip(), Recorte.NOMBRE);
        if (limpio == null || limpio.isEmpty()) {
            throw new IllegalArgumentException("Hace falta un nombre para registrar a alguien");
        }
        Map<String, Object> r = registrarProvisional.execute(new MapSqlParameterSource()
                .addValue("p_nombre", limpio)
                .addValue("p_sede", sedeId));
        return Resultados.numero(r, "p_id").intValue();
    }

    /**
     * Los nombres para «¿Quien eres?», por orden alfabetico.
     *
     * @param sedeId una sede, o {@code null} para todas: quien cubre otra sede
     *               no esta en la rejilla de esa sede.
     */
    public List<FilaPadron> padron(Integer sedeId) {
        return Resultados.lista(padron.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)), "filas");
    }

    /** Las que esperan confirmacion, las mas recientes primero. */
    public List<FilaProvisional> provisionales() {
        return Resultados.lista(provisionales.execute(), "filas");
    }
}
