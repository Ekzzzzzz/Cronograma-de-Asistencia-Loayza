package pe.loayza.asistencia.modelo;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Un tramo de trabajo: una entrada y su salida. Es <b>la hoja</b> del
 * Composite ({@link PeriodoTrabajado}).
 *
 * <p><b>Aqui esta la decision mas importante de todo el modelo:</b> un turno
 * puede estar cojo, y cuando lo esta <b>se ve</b>.
 *
 * <ul>
 *   <li>Entrada sin salida → {@code 10:12AM -}</li>
 *   <li>Salida sin entrada → {@code - 8:46PM}</li>
 * </ul>
 *
 * El sistema <b>no inventa la hora que falta y no descarta la marcacion que
 * sobra</b>. Es lo que pidio el usuario con estas palabras: «la hora y el
 * guion, con el hueco a la vista». Rellenar el hueco seria inventar una hora
 * que nadie puede probar; descartar la marcacion seria borrar evidencia. La
 * tercera opcion —ensenarlo roto— es la unica honesta, y ademas es la que hace
 * que la administradora se entere de que hay algo que arreglar.
 *
 * <p>Por eso mismo un turno cojo <b>suma cero minutos</b>: no se sabe cuanto
 * duro, y suponerlo falsearia el total de horas.
 */
public record Turno(Marcacion entrada, Marcacion salida) implements PeriodoTrabajado {

    /** {@code 9:05AM}. En mayusculas y sin espacio, como el Excel del cliente. */
    private static final DateTimeFormatter RELOJ =
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

    public Turno {
        if (entrada == null && salida == null) {
            throw new IllegalArgumentException("Un turno sin ninguna marcacion no existe");
        }
        if (entrada != null && entrada.tipo() != TipoMarcacion.ENTRADA) {
            throw new IllegalArgumentException("La marcacion de entrada tiene que ser una ENTRADA");
        }
        if (salida != null && salida.tipo() != TipoMarcacion.SALIDA) {
            throw new IllegalArgumentException("La marcacion de salida tiene que ser una SALIDA");
        }
    }

    public static Turno completo(Marcacion entrada, Marcacion salida) {
        return new Turno(entrada, salida);
    }

    /** Entro y todavia no ha salido, o se le olvido marcar la salida. */
    public static Turno soloEntrada(Marcacion entrada) {
        return new Turno(entrada, null);
    }

    /** Marco salida sin que hubiera una entrada abierta. Se acepta igual. */
    public static Turno soloSalida(Marcacion salida) {
        return new Turno(null, salida);
    }

    public boolean completo() {
        return entrada != null && salida != null;
    }

    /** Falta la salida: o sigue trabajando, o se le olvido cerrar. */
    public boolean abierto() {
        return entrada != null && salida == null;
    }

    /** Una salida que no tenia entrada delante. */
    public boolean huerfano() {
        return entrada == null;
    }

    /**
     * Minutos del tramo. <b>Cero si esta cojo</b>, por lo dicho arriba: no se
     * sabe cuanto duro y suponerlo falsearia las horas de la persona.
     */
    @Override
    public int minutos() {
        if (!completo()) {
            return 0;
        }
        long m = Duration.between(entrada.momento(), salida.momento()).toMinutes();
        // Una salida anterior a su entrada no deberia poder llegar aqui, pero
        // si alguna vez llega, un total negativo contaminaria la suma del dia
        // entero y nadie sabria de donde salio.
        return m < 0 ? 0 : (int) m;
    }

    /**
     * El texto de esta linea en la celda del Excel: {@code 9:05AM - 1:03PM},
     * {@code 10:12AM -} o {@code - 8:46PM}.
     */
    @Override
    public String titulo() {
        String a = entrada == null ? "" : RELOJ.format(entrada.hora());
        String b = salida == null ? "" : RELOJ.format(salida.hora());
        if (entrada == null) {
            return "- " + b;
        }
        if (salida == null) {
            return a + " -";
        }
        return a + " - " + b;
    }

    /** Es la hoja: no se descompone en nada mas pequeno. */
    @Override
    public List<PeriodoTrabajado> partes() {
        return List.of();
    }

    public LocalTime horaEntrada() {
        return entrada == null ? null : entrada.hora();
    }

    public LocalTime horaSalida() {
        return salida == null ? null : salida.hora();
    }

    /** Si alguna de las dos marcaciones trae hora del telefono. */
    public boolean exigeAdvertencia() {
        return (entrada != null && entrada.exigeAdvertencia())
                || (salida != null && salida.exigeAdvertencia());
    }
}
