package pe.loayza.asistencia.bd;

import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import pe.loayza.asistencia.modelo.Sede;

/**
 * Las sedes. Es lo que carga el Singleton {@code CatalogoSedes} del modelo al
 * arrancar: el modelo guarda el catalogo pero no sabe de donde sale, y esta
 * clase es ese «de donde».
 *
 * <p>Aqui se mapea directamente al record {@link Sede} del modelo con
 * {@link DataClassRowMapper}: el mapper llama a su constructor, asi que la
 * comprobacion del modelo —una sede sin nombre no existe— se aplica tambien a
 * lo que llega de la base.
 */
@Repository
public class SedesBD {

    private final SimpleJdbcCall sedes;

    public SedesBD(JdbcTemplate jdbc) {
        this.sedes = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_sedes")
                .returningResultSet("filas", new DataClassRowMapper<>(Sede.class));
    }

    /** Todas, tambien las inactivas, por orden alfabetico. */
    public List<Sede> todas() {
        return Resultados.lista(sedes.execute(), "filas");
    }
}
