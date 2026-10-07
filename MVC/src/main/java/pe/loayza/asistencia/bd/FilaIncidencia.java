package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

import pe.loayza.asistencia.modelo.TipoIncidencia;
import pe.loayza.asistencia.modelo.TipoMarcacion;

/**
 * Una incidencia tal como la ve la administradora: <b>junto a la marcacion que
 * se guardo</b>, no en una lista aparte que nadie abre (ADR-012).
 *
 * <p>{@link DataClassRowMapper} convierte tambien los enums: lee
 * {@code 'FOTO_NO_GUARDADA'} como texto y lo pasa a
 * {@link TipoIncidencia#FOTO_NO_GUARDADA}. Si alguien anadiera un valor al
 * {@code ENUM} de MySQL sin anadirlo a Java, esta conversion fallaria al leer
 * la fila, que es justo cuando hay que enterarse.
 *
 * @param marcacionTipo    nulo si la incidencia no apunta a ninguna marcacion.
 * @param ip               en texto, ya convertida por {@code INET6_NTOA}.
 */
public record FilaIncidencia(
        long id,
        LocalDateTime momento,
        TipoIncidencia tipo,
        String detalle,
        boolean revisada,
        Long marcacionId,
        TipoMarcacion marcacionTipo,
        LocalDateTime marcacionMomento,
        String ip,
        Integer trabajadoraId,
        String trabajadora,
        Integer sedeId,
        String sede) {

    static final RowMapper<FilaIncidencia> MAPPER = new DataClassRowMapper<>(FilaIncidencia.class);
}
