package pe.loayza.asistencia.modelo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * El hecho que registra este sistema: alguien dijo que entraba o salia, en un
 * sitio, a una hora. Es la <b>hoja del Composite</b>: lo unico que no se
 * descompone en partes mas pequenas.
 *
 * <p><b>Es inmutable, y no solo porque sea un record.</b> Una marcacion es
 * evidencia de jornada laboral. Nunca se edita y nunca se borra; corregir un
 * olvido es <i>insertar otra</i> con {@link OrigenMarcacion#ADMINISTRADORA} y
 * un motivo escrito. Asi el historico sigue contando lo que de verdad paso, y
 * ademas queda registrado quien lo cambio.
 *
 * @param momento         la hora que vale. Normalmente la pone el servidor.
 * @param momentoRecibido solo cuando la marcacion venia encolada: cuando llego
 *                        de verdad. La diferencia con {@code momento} es lo
 *                        que ve la administradora.
 */
public record Marcacion(
        long id,
        int trabajadoraId,
        int sedeId,
        TipoMarcacion tipo,
        LocalDateTime momento,
        FuenteHora fuenteHora,
        LocalDateTime momentoRecibido,
        OrigenMarcacion origen,
        String notas) {

    public Marcacion {
        if (tipo == null) {
            throw new IllegalArgumentException("Una marcacion sin tipo no dice nada");
        }
        if (momento == null) {
            throw new IllegalArgumentException("Una marcacion sin hora no sirve de evidencia");
        }
        // Si no se dice nada, es una marcacion normal: hora del servidor y
        // puesta por la trabajadora. Son los dos casos de siempre.
        if (fuenteHora == null) {
            fuenteHora = FuenteHora.SERVIDOR;
        }
        if (origen == null) {
            origen = OrigenMarcacion.TRABAJADORA;
        }
    }

    /** Atajo para las pruebas y para el caso normal, que es casi siempre. */
    public static Marcacion de(int trabajadoraId, int sedeId, TipoMarcacion tipo, LocalDateTime momento) {
        return new Marcacion(0, trabajadoraId, sedeId, tipo, momento,
                FuenteHora.SERVIDOR, null, OrigenMarcacion.TRABAJADORA, null);
    }

    public LocalDate fecha() {
        return momento.toLocalDate();
    }

    public LocalTime hora() {
        return momento.toLocalTime();
    }

    public boolean esEntrada() {
        return tipo == TipoMarcacion.ENTRADA;
    }

    /**
     * Cuanto se retraso esta marcacion entre que se hizo y que llego, en
     * minutos. Cero si llego al momento.
     *
     * <p><b>La advertencia se muestra siempre que haya desfase</b>, sin umbral
     * (ADR-010). Un umbral obligaria a elegir un numero que nadie ha pedido, y
     * el criterio de «esto merece mirarse» es de la administradora, no del
     * programa.
     */
    public long desfaseMinutos() {
        if (momentoRecibido == null) {
            return 0;
        }
        return Math.abs(Duration.between(momento, momentoRecibido).toMinutes());
    }

    /** Si esta marcacion tiene que salir marcada en pantalla. */
    public boolean exigeAdvertencia() {
        return fuenteHora.exigeAdvertencia() || desfaseMinutos() > 0;
    }
}
