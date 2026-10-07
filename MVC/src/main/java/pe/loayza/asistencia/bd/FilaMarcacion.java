package pe.loayza.asistencia.bd;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import pe.loayza.asistencia.modelo.Marcacion;

/**
 * Una marcacion tal como la necesita una pantalla: el hecho, mas lo que hace
 * falta para ensenarlo.
 *
 * <p><b>Contiene un {@link Marcacion}, no lo copia.</b> La marcacion sale del
 * mismo {@link MarcacionMapper} que usa el resto de la capa, asi que la misma
 * fila sirve para dos cosas sin traducirla dos veces: pintar la tabla del
 * dashboard y alimentar {@code Jornada.de(...)}, que es quien arma los turnos.
 *
 * <p>La devuelven {@code sp_marcaciones_del_dia} y
 * {@code sp_historial_trabajadora}, que traen <b>las mismas columnas a
 * proposito</b>: un solo mapper que mantener.
 *
 * @param trabajadora  el nombre, para no tener que buscarlo aparte.
 * @param provisional  se registro escribiendo su nombre y espera confirmacion.
 * @param sinFoto      no hay foto. Puede ser un fallo de la camara o una
 *                     marcacion puesta a mano; la incidencia dice cual.
 * @param corregidaPor nombre de la administradora, solo si la puso ella.
 * @param incidencias  cuantas incidencias apuntan a esta marcacion.
 */
public record FilaMarcacion(
        Marcacion marcacion,
        String trabajadora,
        boolean provisional,
        String sede,
        String fotoMiniRuta,
        boolean fotoPurgada,
        boolean sinFoto,
        String corregidaPor,
        String motivo,
        int incidencias) {

    static final RowMapper<FilaMarcacion> MAPPER = new RowMapper<>() {

        private final MarcacionMapper marcaciones = new MarcacionMapper();

        @Override
        public FilaMarcacion mapRow(ResultSet rs, int fila) throws SQLException {
            return new FilaMarcacion(
                    marcaciones.mapRow(rs, fila),
                    rs.getString("trabajadora"),
                    rs.getBoolean("provisional"),
                    rs.getString("sede"),
                    rs.getString("foto_mini_ruta"),
                    rs.getBoolean("foto_purgada"),
                    rs.getBoolean("sin_foto"),
                    rs.getString("corregida_por"),
                    rs.getString("motivo"),
                    rs.getInt("incidencias"));
        }
    };
}
