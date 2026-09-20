package pe.loayza.asistencia.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * La semana de lunes a domingo de una sede: lo que se exporta a Excel. Es la
 * raiz del Composite.
 *
 * <p>El usuario fijo que la exportacion es <b>solo la semana de lunes a
 * domingo</b>, ni un rango libre ni un mes. Por eso la clase no acepta una
 * fecha cualquiera: exige un lunes. Un «rango flexible» habria sido mas
 * general y menos util, y ademas habria abierto la puerta a exportar medias
 * semanas que no cuadran con ningun cronograma.
 */
public record SemanaSede(int sedeId, LocalDate lunes, List<DiaSede> dias)
        implements PeriodoTrabajado {

    public SemanaSede {
        if (lunes == null || lunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException(
                    "La semana del cronograma empieza en lunes; llego: " + lunes);
        }
        List<DiaSede> copia = new ArrayList<>(dias == null ? List.of() : dias);
        copia.sort(Comparator.comparing(DiaSede::fecha));
        dias = List.copyOf(copia);
    }

    @Override
    public int minutos() {
        return dias.stream().mapToInt(DiaSede::minutos).sum();
    }

    @Override
    public String titulo() {
        return "Semana del " + lunes + " al " + lunes.plusDays(6);
    }

    @Override
    public List<DiaSede> partes() {
        return dias;
    }

    public LocalDate domingo() {
        return lunes.plusDays(6);
    }

    /**
     * Las horas de una persona en toda la semana.
     *
     * <p>Recorre el arbol sin preguntar de que tipo es cada nodo, que es justo
     * lo que el Composite viene a ahorrar.
     */
    public int minutosDe(int trabajadoraId) {
        return dias.stream()
                .flatMap(d -> d.jornadas().stream())
                .filter(j -> j.trabajadoraId() == trabajadoraId)
                .mapToInt(Jornada::minutos)
                .sum();
    }

    /** Quienes aparecen en la semana, sin repetir. Son las filas del Excel. */
    public List<Integer> trabajadoras() {
        return dias.stream()
                .flatMap(d -> d.jornadas().stream())
                .map(Jornada::trabajadoraId)
                .distinct()
                .sorted()
                .toList();
    }
}
