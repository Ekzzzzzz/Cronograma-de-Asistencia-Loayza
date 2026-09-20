package pe.loayza.asistencia.modelo;

import java.math.BigDecimal;

/**
 * Uno de los siete locales. Es un dato fijo: no hay pantalla para crear sedes,
 * se siembran en {@code SQL/03_datos_prueba.sql} y se cargan una vez al
 * arrancar en {@link CatalogoSedes}.
 *
 * <p><b>Latitud y longitud se guardan aunque hoy no se usen.</b> No es codigo
 * muerto por descuido: la validacion por GPS quedo aplazada, no descartada, y
 * las dos sedes mas cercanas estan a 3,3 km, asi que el dia que entre no habra
 * ninguna ambiguedad. Guardarlas ahora no cuesta nada; averiguarlas despues,
 * si.
 *
 * @param id        1 a 7, fijo. No es autoincremental: los scripts lo escriben.
 * @param nombre    «Los Olivos», «La Molina»... es lo que ve la trabajadora.
 * @param plusCode  el codigo del que salieron las coordenadas.
 * @param activa    si aparece en el desplegable. Nunca se borra una sede: sus
 *                  marcaciones historicas tienen que seguir teniendo a donde
 *                  apuntar.
 */
public record Sede(
        int id,
        String nombre,
        String plusCode,
        BigDecimal latitud,
        BigDecimal longitud,
        boolean activa) {

    public Sede {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Una sede sin nombre no le sirve a nadie");
        }
    }
}
