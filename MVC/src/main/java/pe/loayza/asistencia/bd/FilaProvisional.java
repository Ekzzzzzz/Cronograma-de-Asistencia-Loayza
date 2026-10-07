package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Alguien que se registro escribiendo su nombre porque no estaba en la lista,
 * con lo que la administradora necesita para decidir si es un alta nueva o una
 * persona que ya existe mal escrita.
 *
 * @param sedeHabitual el nombre de la sede, no el id.
 * @param marcaciones  cuantas lleva; cero si se registro y aun no marco.
 */
public record FilaProvisional(
        int id,
        String nombre,
        LocalDateTime creadaEn,
        String sedeHabitual,
        int marcaciones,
        LocalDateTime primeraMarcacion,
        LocalDateTime ultimaMarcacion) {

    static final RowMapper<FilaProvisional> MAPPER = new DataClassRowMapper<>(FilaProvisional.class);
}
