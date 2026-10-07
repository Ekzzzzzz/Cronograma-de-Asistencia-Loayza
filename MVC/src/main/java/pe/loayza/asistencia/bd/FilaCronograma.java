package pe.loayza.asistencia.bd;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Una celda del Excel: una persona, un dia, en una sede.
 *
 * <p>La devuelven <b>los dos</b> procedimientos de exportacion,
 * {@code sp_cronograma_semanal} y {@code sp_cronograma_diario}, que traen las
 * mismas columnas a proposito (ADR-012): el generador de Excel es uno solo y
 * lo unico que cambia es cuantas columnas de fecha pinta.
 *
 * <p>Las celdas vacias <b>vienen</b>: la semanal es la rejilla completa, cada
 * persona por cada uno de los siete dias, con {@code etiqueta} vacia donde no
 * marco. Java no tiene que rellenar huecos.
 *
 * @param diaSemana {@code DAYOFWEEK} de MySQL: 1 = domingo, 2 = lunes ... 7 =
 *                  sabado. <b>Ojo: no es el {@code DayOfWeek} de Java</b>, que
 *                  empieza en lunes = 1. Para todo lo que no sea depurar, usar
 *                  {@code fecha().getDayOfWeek()}.
 * @param etiqueta  el texto literal de la celda, un turno por linea. Vacio si
 *                  no marco ese dia.
 */
public record FilaCronograma(
        int trabajadoraId,
        String nombre,
        LocalDate fecha,
        int diaSemana,
        LocalTime primeraEntrada,
        LocalTime ultimaSalida,
        int turnos,
        int minutos,
        String etiqueta) {

    static final RowMapper<FilaCronograma> MAPPER = new DataClassRowMapper<>(FilaCronograma.class);

    /** Si en este dia no hay ninguna marcacion. */
    public boolean vacia() {
        return etiqueta == null || etiqueta.isEmpty();
    }
}
