package pe.loayza.asistencia.bd;

import java.time.LocalTime;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Una persona en la pestana del dashboard de una sede, un dia. La devuelve
 * {@code sp_dia_sede}, leyendo la celda ya calculada de {@code jornada_diaria}.
 *
 * <h3>Aqui si se usa el mapper automatico</h3>
 * {@link DataClassRowMapper} llena el record emparejando cada columna con el
 * componente del mismo nombre: {@code primera_entrada} con
 * {@code primeraEntrada}. Sirve para filas planas como esta, sin validacion ni
 * enums, donde escribir el mapper a mano seria copiar doce lineas sin anadir
 * nada. Para {@code Marcacion}, que si valida, se escribe a mano
 * ({@link MarcacionMapper}).
 *
 * <p><b>La condicion es que los nombres coincidan exactamente.</b> Renombrar
 * una columna en el procedimiento deja el campo en nulo o en cero sin dar
 * error, y por eso las pruebas de esta capa leen cada columna al menos una vez.
 *
 * @param turnoAbierto           la ultima marcacion del dia fue una entrada:
 *                               sigue dentro del local.
 * @param desfaseMaximoMinutos   nulo si ninguna marcacion vino encolada.
 * @param incidenciasPendientes  sin revisar, de esa persona ese dia.
 */
public record FilaDiaSede(
        int trabajadoraId,
        String nombre,
        boolean provisional,
        LocalTime primeraEntrada,
        LocalTime ultimaSalida,
        int turnos,
        int minutos,
        String etiqueta,
        boolean turnoAbierto,
        int marcacionesConHoraDelTelefono,
        Integer desfaseMaximoMinutos,
        int incidenciasPendientes) {

    static final RowMapper<FilaDiaSede> MAPPER = new DataClassRowMapper<>(FilaDiaSede.class);
}
