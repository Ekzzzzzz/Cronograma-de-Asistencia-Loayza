package pe.loayza.asistencia.bd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import pe.loayza.asistencia.modelo.FuenteHora;
import pe.loayza.asistencia.modelo.Marcacion;
import pe.loayza.asistencia.modelo.OrigenMarcacion;
import pe.loayza.asistencia.modelo.TipoMarcacion;

/**
 * Una fila de la tabla {@code marcacion} convertida en un {@link Marcacion}
 * del modelo. Es la traduccion mas importante de toda la capa: de aqui sale la
 * materia prima del Composite.
 *
 * <h3>Por que a mano y no con {@code DataClassRowMapper}</h3>
 * Spring sabe mapear un record por nombre de columna sin escribir nada, y en
 * esta capa se usa para las filas planas de los listados. Aqui no, por dos
 * motivos:
 * <ul>
 *   <li>{@code Marcacion} <b>valida en su constructor</b>: una marcacion sin
 *       tipo o sin hora no puede existir. Si una fila llegara asi, el error
 *       tiene que decir que columna fue, y una linea por campo lo deja a la
 *       vista.</li>
 *   <li>Tres columnas son {@code ENUM} en MySQL y enums en Java. Que la
 *       conversion de texto a enum este escrita es lo que permite ver, al leer
 *       esta clase, que los dos vocabularios coinciden palabra por palabra.</li>
 * </ul>
 *
 * <h3>El contrato de columnas</h3>
 * Lee <b>por nombre</b>, nunca por posicion. Cualquier procedimiento que
 * devuelva marcaciones tiene que traer estas nueve columnas con estos nombres:
 * {@code id, trabajadora_id, sede_id, tipo, momento, fuente_hora,
 * momento_recibido, origen, notas}. Las columnas de mas se ignoran, asi que
 * un listado puede anadir el nombre de la sede o la miniatura sin romper este
 * mapper.
 *
 * <p><b>No decide nada.</b> No empareja, no corrige, no descarta: traduce. Si
 * alguna vez aparece un {@code if} de negocio aqui, se escapo del modelo.
 */
final class MarcacionMapper implements RowMapper<Marcacion> {

    @Override
    public Marcacion mapRow(ResultSet rs, int fila) throws SQLException {
        return new Marcacion(
                rs.getLong("id"),
                rs.getInt("trabajadora_id"),
                rs.getInt("sede_id"),
                TipoMarcacion.valueOf(rs.getString("tipo")),
                // DATETIME -> LocalDateTime sin pasar por java.sql.Timestamp,
                // que arrastraria la zona horaria del equipo donde corre Java.
                rs.getObject("momento", LocalDateTime.class),
                FuenteHora.valueOf(rs.getString("fuente_hora")),
                // Nulo en toda marcacion normal: solo existe si vino encolada.
                rs.getObject("momento_recibido", LocalDateTime.class),
                OrigenMarcacion.valueOf(rs.getString("origen")),
                rs.getString("notas"));
    }
}
