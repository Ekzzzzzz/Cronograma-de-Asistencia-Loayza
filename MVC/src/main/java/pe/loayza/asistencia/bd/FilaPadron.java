package pe.loayza.asistencia.bd;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Una persona en la pantalla «¿Quien eres?» del formulario de marcacion.
 *
 * <p><b>Lleva lo minimo a proposito.</b> El formulario es publico y sin login:
 * cualquiera con el enlace ve esta lista. Por eso no hay documento ni nada que
 * no haga falta para que una persona se reconozca.
 *
 * @param sedeHabitual solo para ordenar la rejilla; no impide marcar en otra.
 * @param provisional  se registro escribiendo su nombre; sale para que la
 *                     proxima vez lo toque en vez de escribirlo de nuevo.
 */
public record FilaPadron(int id, String nombre, Integer sedeHabitual, boolean provisional) {

    static final RowMapper<FilaPadron> MAPPER = new DataClassRowMapper<>(FilaPadron.class);
}
