package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;

import pe.loayza.asistencia.modelo.TipoIncidencia;

/**
 * Algo que el sistema <b>acepto</b> y merece una mirada humana.
 *
 * @param marcacionId la marcacion que SI se guardo. Nulo si no la hay: una
 *                    peticion que no decia quien era, o una foto sin marcacion.
 * @param ip          IPv4 o IPv6 en texto; MySQL la guarda en binario.
 * @param detalle     se corta a 200 caracteres si hace falta: nunca hace
 *                    fallar el registro.
 * @param claveEnvio  la clave del toque, cuando se conoce (ADR-014).
 * @param fotoRuta    la foto que acompana a la incidencia, cuando la hay.
 */
public record IncidenciaNueva(
        LocalDateTime momento,
        Long marcacionId,
        String ip,
        Integer trabajadoraId,
        Integer sedeId,
        TipoIncidencia tipo,
        String detalle,
        String claveEnvio,
        String fotoRuta) {

    public IncidenciaNueva {
        if (momento == null || tipo == null) {
            throw new IllegalArgumentException("Una incidencia necesita momento y tipo");
        }
    }

    /** Una incidencia sobre una marcacion que se guardo, sin foto ni clave aparte. */
    public static IncidenciaNueva sobre(LocalDateTime momento, Long marcacionId, String ip,
                                        Integer trabajadoraId, Integer sedeId,
                                        TipoIncidencia tipo, String detalle) {
        return new IncidenciaNueva(momento, marcacionId, ip, trabajadoraId, sedeId, tipo, detalle, null, null);
    }

    /**
     * Llego una foto cuya clave no corresponde a ninguna marcacion. La foto
     * ya se guardo; esto deja el aviso con su ruta y su clave (decision del
     * usuario, 2026-10-06).
     *
     * @param trabajadoraId y {@code sedeId}: lo que declaro el celular al
     *                      mandar la foto, para que el aviso salga en la fila
     *                      de esa persona en el dashboard.
     */
    public static IncidenciaNueva fotoSinMarcacion(LocalDateTime momento, String claveEnvio, String fotoRuta,
                                                   String ip, Integer trabajadoraId, Integer sedeId) {
        return new IncidenciaNueva(momento, null, ip, trabajadoraId, sedeId,
                TipoIncidencia.FOTO_SIN_MARCACION, "Clave de envio sin marcacion", claveEnvio, fotoRuta);
    }
}
