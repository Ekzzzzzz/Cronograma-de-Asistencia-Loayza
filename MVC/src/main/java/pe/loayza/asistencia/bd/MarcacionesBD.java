package pe.loayza.asistencia.bd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import pe.loayza.asistencia.modelo.Jornada;
import pe.loayza.asistencia.modelo.Marcacion;
import pe.loayza.asistencia.modelo.TipoMarcacion;

/**
 * Las marcaciones: guardarlas y leerlas.
 *
 * <h3>Un {@code SimpleJdbcCall} por procedimiento, creado una sola vez</h3>
 * La primera vez que se usa, cada {@code SimpleJdbcCall} le pregunta a MySQL
 * la firma del procedimiento —nombre, tipo y direccion de cada parametro— y
 * con eso arma la llamada. Despues de esa primera vez es seguro usarlo desde
 * muchos hilos a la vez, asi que se crea en el constructor y se reutiliza.
 * Crear uno por peticion repetiria la consulta al catalogo en cada marcacion.
 *
 * <p>{@code @Repository} es un {@code @Component} con un nombre que dice lo que
 * hace: Spring crea una sola instancia y se la inyecta a quien la pida.
 */
@Repository
public class MarcacionesBD {

    private static final Logger log = LoggerFactory.getLogger(MarcacionesBD.class);

    private final SimpleJdbcCall contexto;
    private final SimpleJdbcCall registrarMarcacion;
    private final SimpleJdbcCall actualizarJornada;
    private final SimpleJdbcCall marcacionesDelDia;
    private final SimpleJdbcCall historial;

    /** Ejecuta la segunda mitad del circuito bajo un punto de guardado. */
    private final TransactionTemplate tramoDeLaCelda;

    public MarcacionesBD(JdbcTemplate jdbc, PlatformTransactionManager transacciones) {
        MarcacionMapper marcacion = new MarcacionMapper();
        this.contexto = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_contexto_marcacion")
                .returningResultSet("fila", (rs, i) -> {
                    String ultimo = rs.getString("ultimo_tipo");
                    return new ContextoMarcacion(rs.getInt("marcaciones_hoy"),
                            ultimo == null ? null : TipoMarcacion.valueOf(ultimo));
                });
        this.registrarMarcacion = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_registrar_marcacion")
                .returningResultSet("dia", marcacion);
        this.actualizarJornada = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_actualizar_jornada");
        this.marcacionesDelDia = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_marcaciones_del_dia")
                .returningResultSet("filas", FilaMarcacion.MAPPER);
        // Mismas columnas que sp_marcaciones_del_dia, mismo mapper.
        this.historial = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_historial_trabajadora")
                .returningResultSet("filas", FilaMarcacion.MAPPER);

        // NESTED dentro de una transaccion abierta = un SAVEPOINT de JDBC.
        this.tramoDeLaCelda = new TransactionTemplate(transacciones);
        this.tramoDeLaCelda.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
    }

    /**
     * Guarda una marcacion y reescribe su celda del cronograma. <b>Si este
     * metodo devuelve, la marcacion esta guardada.</b>
     *
     * <h3>El circuito</h3>
     * <pre>
     * BEGIN  (READ COMMITTED)
     *   sp_registrar_marcacion  -&gt; candado de la persona, ¿reenvio?, INSERT, y el dia entero
     *   SAVEPOINT
     *     Jornada.de(dia)        -&gt; el modelo empareja los turnos
     *     sp_actualizar_jornada  -&gt; reescribe la celda
     *   si algo de esto falla: ROLLBACK TO SAVEPOINT (se pierde la celda, no la marcacion)
     * COMMIT
     * </pre>
     *
     * <h3>Por que un punto de guardado (ADR-013)</h3>
     * Una transaccion es todo o nada: sin el, un error al calcular o guardar
     * la celda <b>desharia tambien la marcacion</b>, y eso es rechazarla. La
     * celda es un dato derivado —se recalcula entera desde el dia completo en
     * cada marcacion—, asi que si se pierde la arregla sola la siguiente
     * marcacion de ese dia. La marcacion es el hecho, y no se reconstruye.
     *
     * <h3>Por que {@code READ COMMITTED}</h3>
     * Con el aislamiento por defecto de MySQL ({@code REPEATABLE READ}) una
     * transaccion congela su vista de los datos en su primera lectura. Con
     * {@code READ COMMITTED} cada lectura ve lo ultimo confirmado: el envio
     * que espero en el candado de la persona ve con seguridad la marcacion del
     * que le precedio.
     *
     * @throws org.springframework.dao.DataAccessException si no se pudo guardar
     *         <b>la marcacion</b> —la base caida, o una trabajadora o sede que no
     *         existen—. Es lo unico que hace fallar este metodo, y entonces no
     *         queda nada guardado a medias.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MarcacionRegistrada registrar(MarcacionNueva nueva) {
        Marcacion m = nueva.marcacion();

        Map<String, Object> r = registrarMarcacion.execute(new MapSqlParameterSource()
                .addValue("p_trabajadora", m.trabajadoraId())
                .addValue("p_sede", m.sedeId())
                .addValue("p_tipo", m.tipo().name())
                .addValue("p_momento", alSegundo(m.momento()))
                .addValue("p_fuente_hora", m.fuenteHora().name())
                .addValue("p_momento_recibido", alSegundo(m.momentoRecibido()))
                .addValue("p_notas", Recorte.aColumna(m.notas(), Recorte.NOTAS))
                .addValue("p_clave_envio", nueva.claveEnvio())
                .addValue("p_origen", m.origen().name())
                .addValue("p_corregida_por", nueva.corregidaPor())
                .addValue("p_motivo", Recorte.aColumna(nueva.motivo(), Recorte.MOTIVO)));

        long id = Resultados.numero(r, "p_id");
        int hoy = Resultados.numero(r, "p_marcaciones_hoy").intValue();
        boolean repetida = Resultados.verdad(r, "p_repetida");
        List<Marcacion> dia = Resultados.lista(r, "dia");

        // La celda es la de la marcacion GUARDADA. En un reenvio puede no
        // coincidir con lo que llego ahora: el servidor recalcula la hora de
        // cada intento y puede diferir en algun segundo (ADR-014).
        Marcacion guardada = dia.stream().filter(x -> x.id() == id).findFirst().orElse(m);

        // ---- A partir de aqui la marcacion ya esta escrita. Nada de lo que
        // ---- sigue puede deshacerla.
        try {
            Jornada jornada = tramoDeLaCelda.execute(punto -> {
                Jornada j = Jornada.de(guardada.sedeId(), guardada.fecha(), guardada.trabajadoraId(), dia);
                guardarCelda(j);
                return j;
            });
            return new MarcacionRegistrada(id, hoy, jornada, true, repetida);
        } catch (TransactionException e) {
            // No se pudo ni volver al punto de guardado: la transaccion entera
            // quedo en un estado que no se conoce. Tragarse esto seria decir
            // "guardada" de una marcacion que quiza no lo esta, que es peor que
            // cualquier error. Que suba y se deshaga todo.
            throw e;
        } catch (RuntimeException e) {
            log.error("Marcacion {} GUARDADA, pero su celda (sede {}, {}, trabajadora {}) no se pudo"
                    + " reescribir; la corregira la siguiente marcacion de ese dia",
                    id, guardada.sedeId(), guardada.fecha(), guardada.trabajadoraId(), e);
            return new MarcacionRegistrada(id, hoy, null, false, repetida);
        }
    }

    /**
     * Cuantas lleva hoy y cual fue la ultima, para el contador y para
     * preseleccionar el boton. Se lee <b>antes</b> de marcar. Una sola consulta
     * sobre el indice: 0,7 ms medidos en ADR-012.
     */
    public ContextoMarcacion contexto(int trabajadoraId, int sedeId, LocalDateTime momento) {
        List<ContextoMarcacion> fila = Resultados.lista(contexto.execute(new MapSqlParameterSource()
                .addValue("p_trabajadora", trabajadoraId)
                .addValue("p_sede", sedeId)
                .addValue("p_momento", alSegundo(momento))), "fila");
        // COUNT(*) siempre devuelve una fila, aunque no haya marcaciones.
        return fila.getFirst();
    }

    /** Todas las marcaciones de un dia, en orden de reloj. {@code sedeId} nulo: todas. */
    public List<FilaMarcacion> delDia(Integer sedeId, LocalDate fecha) {
        return Resultados.lista(marcacionesDelDia.execute(new MapSqlParameterSource()
                .addValue("p_sede", sedeId)
                .addValue("p_fecha", fecha)), "filas");
    }

    /**
     * Donde estuvo una persona entre dos fechas, <b>cruzando todas las
     * sedes</b>: la unica consulta del sistema que no filtra por sede.
     */
    public List<FilaMarcacion> historial(int trabajadoraId, LocalDate desde, LocalDate hasta) {
        return Resultados.lista(historial.execute(new MapSqlParameterSource()
                .addValue("p_trabajadora", trabajadoraId)
                .addValue("p_desde", desde)
                .addValue("p_hasta", hasta)), "filas");
    }

    private void guardarCelda(Jornada j) {
        actualizarJornada.execute(new MapSqlParameterSource()
                .addValue("p_sede", j.sedeId())
                .addValue("p_fecha", j.fecha())
                .addValue("p_trabajadora", j.trabajadoraId())
                .addValue("p_primera", j.primeraEntrada())
                .addValue("p_ultima", j.ultimaSalida())
                .addValue("p_turnos", j.turnosCompletos())
                .addValue("p_minutos", j.minutos())
                .addValue("p_etiqueta", j.etiqueta()));
    }

    /**
     * {@code DATETIME} guarda segundos, no fracciones. Si se le manda
     * {@code 12:00:00.6}, MySQL <b>redondea</b> a {@code 12:00:01}: la hora
     * guardada seria posterior a la real. Se corta aqui, a la vista, en vez de
     * dejar que el motor redondee por su cuenta.
     */
    private static LocalDateTime alSegundo(LocalDateTime t) {
        return t == null ? null : t.truncatedTo(ChronoUnit.SECONDS);
    }
}
