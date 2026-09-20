package pe.loayza.asistencia.modelo;

/**
 * Algo que el sistema <b>acepto</b> y no entendio del todo.
 *
 * <p><b>Esta clase sustituye a {@code MotivoRechazo}, y el cambio de nombre es
 * el cambio de fondo.</b> Antes habia seis motivos para decirle que no a una
 * trabajadora. Ahora no hay ninguno: este sistema <b>no rechaza ninguna
 * marcacion</b> (ADR-012).
 *
 * <p>El motivo es del cliente y es bueno: las trabajadoras marcan con prisa,
 * entre paciente y paciente. <b>Que a alguien se le invalide una marcacion es
 * el peor fallo posible</b>, porque le borra una hora de trabajo real y la
 * obliga a perseguir a la administradora para arreglarlo. Frente a eso, un
 * dato raro en una lista no es nada.
 *
 * <h3>La contrapartida, que es la parte seria</h3>
 * Un sistema que acepta todo y no dice nada es peor que uno estricto: acepta
 * en silencio y nadie se entera de nada. Por eso cada cosa rara deja una fila
 * aqui, enlazada a la marcacion que <b>si</b> se guardo, y el dashboard las
 * muestra hasta que alguien las marca como revisadas.
 *
 * <p><b>El sistema pasa de decidir a informar.</b> Quien decide es una
 * persona, que es quien puede llamar a la sede y preguntar.
 */
public enum TipoIncidencia {

    /**
     * Llego la marcacion pero la foto no se pudo guardar: la camara devolvio
     * un archivo vacio, roto, o algo que no era una imagen.
     *
     * <p><b>La marcacion se guarda igual, sin foto.</b> La hora trabajada no
     * depende de que la camara funcionara. La foto es corroboracion, no es el
     * hecho.
     */
    FOTO_NO_GUARDADA("No se pudo guardar la foto; la marcacion si quedo registrada"),

    /**
     * La imagen supera el limite de tamano. Con la compresion en el navegador
     * esto no deberia ocurrir nunca desde el formulario propio, asi que
     * cuando ocurre merece una mirada.
     */
    FOTO_DEMASIADO_PESADA("La foto pesaba demasiado y no se guardo"),

    /**
     * La imagen declara unas dimensiones absurdas (mas de 5000 x 5000). Es la
     * bomba de descompresion: un archivo pequeno que al decodificarse ocupa
     * gigabytes y puede tumbar el servidor para todos los demas.
     *
     * <p>Las dimensiones se leen de la cabecera <b>antes</b> de decodificar, y
     * la foto se descarta; <b>la marcacion no</b>. Es la unica barrera del
     * sistema que sigue existiendo, y no protege contra la trabajadora sino
     * contra que una sola peticion deje sin servicio a las otras 98.
     */
    FOTO_SOSPECHOSA("La foto tenia un tamano anormal; se guardo la marcacion sin ella"),

    /**
     * La hora la puso el telefono, no el servidor. Pasa con la cola sin senal.
     * Se anota <b>siempre que haya desfase</b>, sin umbral: el criterio de
     * «esto merece mirarse» es de la administradora, no del programa.
     */
    HORA_DEL_TELEFONO("La hora la puso el telefono porque no habia senal"),

    /**
     * Una marcacion encolada que llego con muchas horas de retraso.
     *
     * <p><b>Antes esto era un rechazo</b> («COLA_VENCIDA», mas de 2 horas),
     * pensado para que nadie fabricara historia laboral atrasando el reloj del
     * telefono. El problema es que castigaba a quien de verdad se quedo sin
     * cobertura toda la manana. Ahora entra, se marca, y el desfase se ve
     * grande en el dashboard: quien decide es una persona.
     */
    COLA_MUY_ANTIGUA("Esta marcacion estuvo mucho tiempo esperando en el telefono"),

    /**
     * Dos envios identicos con segundos de diferencia: casi siempre el boton
     * pulsado dos veces.
     *
     * <p><b>Se guardan las dos.</b> Decidir cual sobra es decidir por ella, y
     * si de verdad marco dos veces, eso paso. El formulario evita el problema
     * por delante -el boton se desactiva al pulsarlo-, que es una ayuda y no
     * un rechazo: si aun asi llegan dos, entran las dos.
     */
    ENVIO_DUPLICADO("Llegaron dos marcaciones iguales casi al mismo tiempo");

    private final String descripcion;

    TipoIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Lo que lee la administradora en el dashboard. */
    public String descripcion() {
        return descripcion;
    }

    /**
     * Si la marcacion se guardo sin foto por culpa de esto.
     *
     * <p>Sirve para que el dashboard distinga de un vistazo las que no tienen
     * foto <b>por un fallo</b> de las que no la tienen <b>porque las puso la
     * administradora a mano</b>, que es algo completamente distinto.
     */
    public boolean dejoLaMarcacionSinFoto() {
        return this == FOTO_NO_GUARDADA
                || this == FOTO_DEMASIADO_PESADA
                || this == FOTO_SOSPECHOSA;
    }
}
