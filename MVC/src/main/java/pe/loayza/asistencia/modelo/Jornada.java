package pe.loayza.asistencia.modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lo que una persona trabajo en <b>una sede</b> y <b>un dia</b>. Es
 * exactamente una celda del Excel del cronograma, y un nodo intermedio del
 * Composite: se compone de {@link Turno}.
 *
 * <p><b>Esta clase es el corazon del sistema.</b> Todo lo demas mueve datos de
 * un sitio a otro; aqui es donde una lista de marcaciones sueltas se convierte
 * en una jornada con sentido. Por eso esta escrita en Java y no en SQL: se
 * prueba con un {@code new} y una lista, sin base de datos y sin Spring.
 *
 * <p><b>Y por eso solo esta escrita una vez.</b> El procedimiento
 * {@code sp_registrar_marcacion} devuelve el dia entero ordenado y no
 * empareja nada; emparejar es asunto de aqui. Si la regla viviera tambien en
 * SQL, cambiarla obligaria a acordarse de dos sitios: el <i>Shotgun
 * Surgery</i> del capitulo de antipatrones.
 *
 * <h2>Como empareja</h2>
 * Recorre las marcaciones en orden de reloj con una sola entrada pendiente:
 * <ul>
 *   <li>Llega una ENTRADA y no habia nada pendiente → queda pendiente.</li>
 *   <li>Llega una SALIDA y habia una pendiente → se cierra el turno.</li>
 *   <li>Llega una ENTRADA y <b>ya habia otra pendiente</b> → la anterior se
 *       cierra cojo, {@code 9:03AM -}, y la nueva queda pendiente.</li>
 *   <li>Llega una SALIDA y <b>no hay nada pendiente</b> → turno huerfano,
 *       {@code - 8:46PM}.</li>
 * </ul>
 *
 * <p><b>Ningun caso descarta una marcacion y ningun caso la rechaza</b>
 * (ADR-010). Doble entrada, doble salida y varias marcaciones seguidas en la
 * misma sede se aceptan todas y se ven todas. El sistema no le dice que no a
 * nadie: ensena lo que paso y deja que una persona lo interprete.
 */
public final class Jornada implements PeriodoTrabajado {

    /**
     * Tiene que coincidir con {@code jornada_diaria.etiqueta}, que es
     * {@code VARCHAR(400)}.
     *
     * <p><b>Era 160, y el tope cayo con ADR-012.</b> Mientras existia el
     * limite de 12 marcaciones al dia, la celda no podia pasar de ~120
     * caracteres y 160 sobraba. Sin limite, alguien que marque veinte veces
     * genera una celda mas larga que eso, y el sintoma habria sido el peor
     * posible: <b>MySQL cortandola en silencio</b>. Una jornada a medias que
     * nadie sabe que esta a medias no se encuentra nunca.
     *
     * <p>Se corta aqui, por linea entera, y ademas se guarda el numero de
     * turnos aparte, que es el que no miente.
     */
    static final int ETIQUETA_MAX = 400;

    private final int sedeId;
    private final LocalDate fecha;
    private final int trabajadoraId;
    private final List<Marcacion> marcaciones;
    private final List<Turno> turnos;

    private Jornada(int sedeId, LocalDate fecha, int trabajadoraId,
                    List<Marcacion> marcaciones, List<Turno> turnos) {
        this.sedeId = sedeId;
        this.fecha = fecha;
        this.trabajadoraId = trabajadoraId;
        this.marcaciones = List.copyOf(marcaciones);
        this.turnos = List.copyOf(turnos);
    }

    /**
     * Construye la jornada a partir de las marcaciones de esa persona en esa
     * sede ese dia. Es la unica forma de crear una: no hay constructor
     * publico, porque una Jornada cuyos turnos no salgan de sus marcaciones
     * seria una celda que no se puede justificar ante nadie.
     */
    public static Jornada de(int sedeId, LocalDate fecha, int trabajadoraId, List<Marcacion> marcaciones) {
        List<Marcacion> ordenadas = new ArrayList<>(marcaciones == null ? List.of() : marcaciones);
        // El orden lo pone esta clase y no quien la llama. Si llegaran
        // desordenadas -y desde una cola sin senal llegan desordenadas- el
        // emparejamiento saldria mal sin dar ningun error.
        ordenadas.sort(Comparator.comparing(Marcacion::momento).thenComparingLong(Marcacion::id));
        return new Jornada(sedeId, fecha, trabajadoraId, ordenadas, emparejar(ordenadas));
    }

    private static List<Turno> emparejar(List<Marcacion> ordenadas) {
        List<Turno> resultado = new ArrayList<>();
        Marcacion pendiente = null;

        for (Marcacion m : ordenadas) {
            if (m.esEntrada()) {
                if (pendiente != null) {
                    // Dos entradas seguidas. La primera no se pierde ni se
                    // corrige: se cierra cojo y queda a la vista.
                    resultado.add(Turno.soloEntrada(pendiente));
                }
                pendiente = m;
            } else {
                if (pendiente != null) {
                    resultado.add(Turno.completo(pendiente, m));
                    pendiente = null;
                } else {
                    // Salida sin entrada delante. Tampoco se descarta.
                    resultado.add(Turno.soloSalida(m));
                }
            }
        }
        if (pendiente != null) {
            resultado.add(Turno.soloEntrada(pendiente));
        }
        return resultado;
    }

    // ---------------------------------------------------------------- Composite

    /** Suma turno a turno. Los cojos aportan cero, que es lo unico honesto. */
    @Override
    public int minutos() {
        return turnos.stream().mapToInt(Turno::minutos).sum();
    }

    @Override
    public String titulo() {
        return "Jornada del " + fecha;
    }

    @Override
    public List<Turno> partes() {
        return turnos;
    }

    // ---------------------------------------------------------- La celda del Excel

    /**
     * El texto literal de la celda: <b>un par por linea</b>.
     *
     * <pre>
     *   9:05AM - 1:03PM
     *   2:01PM - 6:12PM
     * </pre>
     *
     * <p>{@code DESCANSO}, {@code NO TURNO} e {@code INASISTENCIA} no los
     * escribe el sistema: los pone la administradora a mano sobre el archivo
     * exportado. El sistema solo escribe horas, porque son lo unico de lo que
     * tiene prueba.
     */
    public String etiqueta() {
        StringBuilder sb = new StringBuilder();
        for (Turno t : turnos) {
            String linea = t.titulo();
            if (sb.length() > 0 && sb.length() + 1 + linea.length() > ETIQUETA_MAX) {
                // Se corta por linea entera, nunca a mitad de una hora: media
                // hora escrita es peor que una hora que falta.
                break;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(linea);
        }
        return sb.toString();
    }

    /** La hora de la primera ENTRADA del dia, o nulo si no hubo ninguna. */
    public LocalTime primeraEntrada() {
        return marcaciones.stream()
                .filter(Marcacion::esEntrada)
                .map(Marcacion::hora)
                .findFirst()
                .orElse(null);
    }

    /** La hora de la ultima SALIDA del dia, o nulo si no hubo ninguna. */
    public LocalTime ultimaSalida() {
        return marcaciones.reversed().stream()
                .filter(m -> !m.esEntrada())
                .map(Marcacion::hora)
                .findFirst()
                .orElse(null);
    }

    /**
     * Cuantos turnos se cerraron. <b>Solo cuenta los completos</b>: es el
     * numero que el modelo de datos compara contra el tope de 6 pares, y un
     * turno abierto todavia no es un par.
     */
    public int turnosCompletos() {
        return (int) turnos.stream().filter(Turno::completo).count();
    }

    /**
     * Si esta persona entro y no ha cerrado.
     *
     * <p><b>No es lo mismo que «no hay ultima salida».</b> Alguien que hace
     * 9-1, 2-6 y vuelve a entrar a las 7 tiene ultima salida a las 6 y sin
     * embargo tiene un turno abierto. Con multi-marcacion ese es el caso
     * normal, no el raro, asi que se responde mirando el ultimo turno y no la
     * ultima hora.
     */
    public boolean tieneTurnoAbierto() {
        return !turnos.isEmpty() && turnos.getLast().abierto();
    }

    /** Si alguna marcacion del dia trae hora del telefono o llego con desfase. */
    public boolean exigeAdvertencia() {
        return marcaciones.stream().anyMatch(Marcacion::exigeAdvertencia);
    }

    /** El desfase mas grande del dia, en minutos. Cero si no hubo ninguno. */
    public long desfaseMaximoMinutos() {
        return marcaciones.stream().mapToLong(Marcacion::desfaseMinutos).max().orElse(0);
    }

    /** El contador que ve la trabajadora en pantalla tras marcar. */
    public ContadorDeHoy contador() {
        return ContadorDeHoy.para(marcaciones.size());
    }

    public int sedeId()          { return sedeId; }
    public LocalDate fecha()     { return fecha; }
    public int trabajadoraId()   { return trabajadoraId; }
    public List<Marcacion> marcaciones() { return marcaciones; }
    public List<Turno> turnos()  { return turnos; }
    public boolean vacia()       { return marcaciones.isEmpty(); }

    @Override
    public String toString() {
        return "Jornada[sede=" + sedeId + ", fecha=" + fecha + ", trabajadora=" + trabajadoraId
                + ", turnos=" + turnos.size() + ", " + horasLegibles() + "]";
    }
}
