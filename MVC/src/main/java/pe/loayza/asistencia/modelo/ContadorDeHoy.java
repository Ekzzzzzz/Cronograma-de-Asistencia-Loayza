package pe.loayza.asistencia.modelo;

/**
 * El «Marcacion 2 de 2 de hoy» que ve la trabajadora despues de enviar.
 *
 * <p><b>Es una clase entera para una division, y tiene motivo.</b> El usuario
 * corrigio esto expresamente: no se le dice <i>«Marcacion 1 de 12»</i>, se le
 * dice <i>«Marcacion 1 de 2»</i>. Cuando marca otra vez, <i>«2 de 2»</i>; si
 * vuelve a marcar, <i>«3 de 4»</i>, y asi.
 *
 * <pre>
 *   marcaciones hechas : 1    2    3    4    5    6
 *   lo que ella lee    : 1/2  2/2  3/4  4/4  5/6  6/6
 * </pre>
 *
 * <h3>Por que en pares y no contra el tope</h3>
 * <ul>
 *   <li><b>Porque es lo que ella esta haciendo.</b> Nadie va a trabajar
 *       pensando «hoy me quedan diez marcaciones». Piensa en entradas y
 *       salidas, y el mensaje le confirma que el par quedo cerrado.</li>
 *   <li><b>Porque el formulario es publico.</b> Decir «de 12» seria anunciarle
 *       a cualquiera donde esta exactamente la barrera del endpoint. El tope
 *       existe, pero no se publica.</li>
 * </ul>
 *
 * @param hechas cuantas marcaciones lleva hoy en esa sede.
 * @param total  el par al que pertenece la ultima.
 */
public record ContadorDeHoy(int hechas, int total) {

    public static ContadorDeHoy para(int hechas) {
        if (hechas < 0) {
            throw new IllegalArgumentException("No se puede haber marcado menos de cero veces");
        }
        // Redondeo al par siguiente: 1 y 2 -> 2, 3 y 4 -> 4.
        return new ContadorDeHoy(hechas, ((hechas + 1) / 2) * 2);
    }

    /** El texto tal cual sale en pantalla. */
    public String mensaje() {
        return "Marcacion " + hechas + " de " + total + " de hoy";
    }

    /** Si acaba de cerrar un par: util para decidir el tono del mensaje. */
    public boolean parCompleto() {
        return hechas > 0 && hechas % 2 == 0;
    }
}
