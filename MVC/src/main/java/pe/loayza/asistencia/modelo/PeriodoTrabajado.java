package pe.loayza.asistencia.modelo;

import java.util.List;

/**
 * <h2>Patron Composite — la interfaz comun</h2>
 *
 * Un trozo de tiempo trabajado. Puede ser un turno suelto, el dia de una
 * persona, el dia de una sede entera o la semana completa de esa sede.
 *
 * <p><b>Que problema real resuelve aqui.</b> El dashboard y la exportacion
 * preguntan lo mismo en cuatro sitios distintos: «cuantas horas salen». Sin
 * este patron habria cuatro maneras de sumar —una por pantalla— y el dia que
 * cambiara la regla habria que acordarse de las cuatro. Con el, sumar es
 * siempre {@link #minutos()}, y el que suma no necesita saber si esta
 * preguntandole a un turno o a una semana de siete sedes.
 *
 * <p><b>Donde esta la jerarquia:</b>
 * <pre>
 *   SemanaSede  ──contiene──▶  DiaSede  ──contiene──▶  Jornada
 *                                                           │
 *                                                      contiene
 *                                                           ▼
 *                                                        Turno   ← la hoja
 *                                                           │
 *                                                    hecho de 1 o 2
 *                                                           ▼
 *                                                      Marcacion
 * </pre>
 *
 * <p><b>Por que {@link Marcacion} no implementa esta interfaz.</b> Podria, y
 * seria mas simetrico. Pero una marcacion suelta no es tiempo trabajado: una
 * entrada a las 9:00 no son cero minutos ni son ocho horas, es un instante.
 * El tiempo aparece cuando hay un par, y ahi es donde empieza {@link Turno}.
 * Estirar el patron un nivel mas solo para que quedara bonito seria el
 * <i>Golden Hammer</i> del capitulo de antipatrones.
 */
public interface PeriodoTrabajado {

    /**
     * Minutos efectivamente trabajados.
     *
     * <p><b>Siempre sumando parte por parte, jamas restando los extremos.</b>
     * Es la regla que mas facil se rompe: una jornada de 9-1 y 3-8 son nueve
     * horas, no once. Si alguien alguna vez calcula horas con la primera
     * entrada y la ultima salida, este sistema empieza a pagar almuerzos.
     */
    int minutos();

    /** Como se llama este periodo en pantalla. */
    String titulo();

    /**
     * Las partes de las que se compone. Una hoja devuelve la lista vacia, y
     * por eso quien recorre el arbol no necesita preguntar de que tipo es cada
     * nodo: ese {@code if} que no hay que escribir es medio patron.
     */
    List<? extends PeriodoTrabajado> partes();

    /** Las horas en el formato que se lee en el dashboard: {@code 8h 15m}. */
    default String horasLegibles() {
        int m = minutos();
        return (m / 60) + "h " + String.format("%02dm", m % 60);
    }

    /** Si este nodo no tiene partes, es una hoja. */
    default boolean esHoja() {
        return partes().isEmpty();
    }
}
