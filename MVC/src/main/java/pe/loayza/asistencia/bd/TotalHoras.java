package pe.loayza.asistencia.bd;

import java.math.BigDecimal;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Las horas de una persona en una sede, sumadas en un rango de fechas. La
 * devuelve {@code sp_totales_horas}.
 *
 * <p><b>Suma celda a celda, y cada celda ya viene sumada turno a turno</b>
 * (regla 3): una jornada de 9-1 y 2-6 aporta 480 minutos, no 540. Nunca se
 * calcula restando la primera entrada de la ultima salida.
 *
 * @param minutos el dato bueno. Usar este para cualquier calculo.
 * @param horas   {@code minutos / 60} redondeado a dos decimales por MySQL:
 *                solo para ensenar. Sumar horas ya redondeadas acumula error.
 */
public record TotalHoras(
        int trabajadoraId,
        String nombre,
        int sedeId,
        String sede,
        int diasTrabajados,
        int turnos,
        int minutos,
        BigDecimal horas) {

    static final RowMapper<TotalHoras> MAPPER = new DataClassRowMapper<>(TotalHoras.class);
}
