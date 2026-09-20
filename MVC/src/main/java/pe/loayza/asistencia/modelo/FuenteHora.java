package pe.loayza.asistencia.modelo;

/**
 * Quien puso la hora de una marcacion.
 *
 * <p>Existe por la cola sin senal (ADR-008). La regla que sostiene todo el
 * sistema es esta: <b>el sistema nunca afirma una hora que no puede probar</b>.
 * Si la marcacion se envio sin cobertura y el momento lo puso el telefono, la
 * fila lo dice, el dashboard lo muestra y la advertencia sale siempre, aunque
 * el desfase sea de un minuto (ADR-010).
 */
public enum FuenteHora {

    /** Lo normal: la hora la puso el servidor al recibir la marcacion. */
    SERVIDOR,

    /**
     * La marcacion se quedo encolada en el telefono sin senal. El momento es
     * lo que dijo el dispositivo; cuando llego de verdad se guarda aparte, en
     * {@code momentoRecibido}, y la diferencia entre los dos es justo lo que
     * la administradora necesita ver.
     */
    DISPOSITIVO;

    /** Si esta hora hay que marcarla en pantalla con una advertencia. */
    public boolean exigeAdvertencia() {
        return this == DISPOSITIVO;
    }
}
