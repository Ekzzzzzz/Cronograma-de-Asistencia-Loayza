package pe.loayza.asistencia.modelo;

/**
 * Entrada o salida. No hay mas.
 *
 * <p><b>Ojo con lo que este enum NO significa.</b> Que una marcacion diga
 * {@code ENTRADA} no quiere decir que sea valida, ni que toque entrar, ni que
 * la anterior fuera una salida. Desde ADR-010 el sistema <b>no rechaza nada</b>
 * por su secuencia: dos entradas seguidas se aceptan las dos y quedan a la
 * vista. Este enum solo dice que dijo la persona al marcar.
 */
public enum TipoMarcacion {
    ENTRADA,
    SALIDA;

    /**
     * Lo que tocaria a continuacion. La palabra importa: <b>sugiere</b>.
     * La fabrica de la capa control lo usa para preseleccionar el boton y
     * ahorrarle un toque a la trabajadora, no para validar.
     */
    public TipoMarcacion siguienteSugerido() {
        return this == ENTRADA ? SALIDA : ENTRADA;
    }
}
