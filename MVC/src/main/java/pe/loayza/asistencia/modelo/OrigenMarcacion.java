package pe.loayza.asistencia.modelo;

/**
 * Quien creo la marcacion.
 *
 * <p>Sostiene la correccion de olvidos de ADR-006. Cuando alguien olvida
 * marcar la salida, la administradora <b>inserta una marcacion nueva</b> con
 * origen {@code ADMINISTRADORA}; nunca edita la existente. La marcacion sigue
 * siendo un hecho inmutable y ademas queda escrito quien lo puso y por que.
 */
public enum OrigenMarcacion {

    /** La marco la persona desde su telefono, con foto. */
    TRABAJADORA,

    /**
     * La puso la administradora a mano. <b>Nunca lleva foto</b>, y por eso
     * {@code fotoRuta} admite nulo en el modelo de datos: una correccion
     * manual no puede tener una. En el dashboard y en la exportacion las dos
     * se distinguen a simple vista, a proposito.
     */
    ADMINISTRADORA;

    /** Una marcacion puesta a mano exige motivo escrito. */
    public boolean exigeMotivo() {
        return this == ADMINISTRADORA;
    }
}
