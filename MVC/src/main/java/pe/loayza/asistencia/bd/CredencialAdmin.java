package pe.loayza.asistencia.bd;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * La cuenta de la administradora, con su hash BCrypt.
 *
 * <p><b>El hash se compara en Java, nunca en MySQL</b>: un procedimiento que
 * recibiera la contrasena en claro la dejaria escrita en el log de consultas
 * del motor. Por eso {@code sp_autenticar_admin} devuelve el hash y no un
 * «si» o «no».
 *
 * <h3>Por que {@code toString()} esta escrito a mano</h3>
 * El de un record imprime <b>todos</b> sus campos. Un
 * {@code log.debug("admin: {}", credencial)} cualquiera dejaria el hash en los
 * logs, que se copian, se suben y se comparten. Un hash filtrado se puede
 * atacar sin conexion durante el tiempo que haga falta.
 *
 * @param clave hash BCrypt de 60 caracteres. Nunca la contrasena.
 */
public record CredencialAdmin(
        int id,
        String usuario,
        String clave,
        String nombre,
        boolean activo,
        LocalDateTime ultimoAcceso) {

    static final RowMapper<CredencialAdmin> MAPPER = new DataClassRowMapper<>(CredencialAdmin.class);

    @Override
    public String toString() {
        return "CredencialAdmin[id=" + id + ", usuario=" + usuario + ", clave=***, nombre=" + nombre
                + ", activo=" + activo + ", ultimoAcceso=" + ultimoAcceso + "]";
    }
}
