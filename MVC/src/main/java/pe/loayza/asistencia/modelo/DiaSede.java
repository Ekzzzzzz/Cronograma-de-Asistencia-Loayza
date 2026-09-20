package pe.loayza.asistencia.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Un dia de una sede entera: todas las jornadas de esa fecha en ese local. Es
 * la pestana del dashboard, y otro nodo del Composite.
 *
 * <p>Es el primer nivel donde <b>se ve el valor del patron</b>: preguntar las
 * horas de todo el local es la misma llamada que preguntar las de un turno
 * suelto, {@link #minutos()}, y esta clase no sabe sumar. Solo pide a sus
 * partes que sumen ellas.
 */
public record DiaSede(int sedeId, LocalDate fecha, List<Jornada> jornadas)
        implements PeriodoTrabajado {

    public DiaSede {
        List<Jornada> copia = new ArrayList<>(jornadas == null ? List.of() : jornadas);
        // Primero quien sigue dentro, luego por hora de llegada. Es el orden
        // que la administradora necesita: lo que esta pasando ahora arriba.
        copia.sort(Comparator
                .comparing((Jornada j) -> !j.tieneTurnoAbierto())
                .thenComparing(Jornada::primeraEntrada,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        jornadas = List.copyOf(copia);
    }

    @Override
    public int minutos() {
        return jornadas.stream().mapToInt(Jornada::minutos).sum();
    }

    @Override
    public String titulo() {
        return "Sede " + sedeId + " — " + fecha;
    }

    @Override
    public List<Jornada> partes() {
        return jornadas;
    }

    /** Cuantas personas siguen con un turno abierto ahora mismo. */
    public int turnosAbiertos() {
        return (int) jornadas.stream().filter(Jornada::tieneTurnoAbierto).count();
    }

    /**
     * Las jornadas que hay que mirar: algo no cuadra en ellas. Son las que el
     * dashboard marca, y la razon de que existan las advertencias siempre
     * (ADR-010): si el sistema ya no rechaza nada, lo unico que queda es
     * <b>ensenar bien lo que entro</b>.
     */
    public List<Jornada> conAdvertencia() {
        return jornadas.stream()
                .filter(j -> j.exigeAdvertencia() || j.tieneTurnoAbierto())
                .toList();
    }

    public int personas() {
        return jornadas.size();
    }
}
