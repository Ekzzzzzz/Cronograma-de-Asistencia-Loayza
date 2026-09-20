package pe.loayza.asistencia.modelo;

/**
 * Una persona de la plantilla. Entre 10 y 20 por sede, siete sedes hoy y
 * posiblemente mas manana.
 *
 * <h3>Lo que este record NO guarda, y por que</h3>
 * <b>No hay cargo ni tipo de jornada.</b> Lo quito el cliente (ADR-012) y no
 * es un recorte: este sistema registra <i>horas trabajadas</i>, y ni el puesto
 * -podologa, cajera- ni el contrato -completa, media- cambian una sola de esas
 * horas. Un dato que no se usa hay que mantenerlo al dia para siempre, se
 * queda obsoleto en silencio, y sobre todo <b>tienta a inventar reglas con
 * el</b>: en cuanto existe una columna «media jornada», alguien acaba
 * proponiendo avisar cuando pase de cuatro horas, y eso rechazaria horas extra
 * legitimas.
 *
 * <h3>Casi todo lo demas admite ausencia, y tambien a proposito</h3>
 * Quien no aparece en la lista <b>escribe su nombre y marca igual</b>. Nadie se
 * queda sin registrar su jornada por un problema de padron. Esa persona entra
 * como {@link #provisional()} y la administradora despues la confirma como
 * alta nueva o la fusiona con quien ya existia mal escrita.
 *
 * <p>Es el equilibrio entre dos cosas que chocaban: no bloquear a nadie, y no
 * ensuciar el padron. Se resuelve dejando entrar y revisando despues, en vez
 * de revisar antes y dejar a alguien fuera.
 *
 * @param sedeHabitual solo <b>preselecciona</b> el desplegable para ahorrar un
 *                     toque. No restringe: cualquiera puede marcar en
 *                     cualquier sede, que es el caso central de este sistema.
 * @param activa       baja logica, nunca borrado. Si alguien deja la empresa,
 *                     el cronograma de meses anteriores tiene que seguir
 *                     saliendo.
 * @param provisional  mientras sea cierto, esta persona <b>no entra al
 *                     cronograma exportado</b>. Si aparece en el dashboard:
 *                     es justo donde la administradora la descubre.
 */
public record Trabajadora(
        int id,
        String nombre,
        String documento,
        Integer sedeHabitual,
        boolean activa,
        boolean provisional) {

    public Trabajadora {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Una trabajadora sin nombre no se puede registrar");
        }
        nombre = nombre.strip();
    }

    /**
     * Alta por nombre libre. Es el constructor del caso «no estoy en la
     * lista»: sin documento, porque nadie lo sabe todavia y no se inventa.
     */
    public static Trabajadora provisionalNueva(String nombre, Integer sede) {
        return new Trabajadora(0, nombre, null, sede, true, true);
    }

    /** Si esta fila puede aparecer en el Excel que se entrega. */
    public boolean entraAlCronograma() {
        return activa && !provisional;
    }
}
