package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;

import pe.loayza.asistencia.modelo.TipoIncidencia;

/**
 * Algo que el sistema <b>acepto</b> y merece una mirada humana.
 *
 * @param marcacionId la marcacion que SI se guardo. Nulo solo si llego una
 *                    peticion que ni siquiera decia quien era.
 * @param ip          IPv4 o IPv6 en texto; MySQL la guarda en binario.
 * @param detalle     se corta a 200 caracteres si hace falta: nunca hace
 *                    fallar el registro.
 */
public record IncidenciaNueva(
        LocalDateTime momento,
        Long marcacionId,
        String ip,
        Integer trabajadoraId,
        Integer sedeId,
        TipoIncidencia tipo,
        String detalle) {

    public IncidenciaNueva {
        if (momento == null || tipo == null) {
            throw new IllegalArgumentException("Una incidencia necesita momento y tipo");
        }
    }
}
