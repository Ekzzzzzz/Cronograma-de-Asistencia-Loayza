package pe.loayza.asistencia.bd;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * La cuenta de la administradora. Esta clase <b>no autentica</b>: devuelve el
 * hash para que lo compare quien sepa hacerlo (Spring Security, en la capa
 * control) y apunta el acceso cuando ya se comprobo.
 */
@Repository
public class AdministradoresBD {

    private final SimpleJdbcCall buscar;
    private final SimpleJdbcCall marcarAcceso;

    public AdministradoresBD(JdbcTemplate jdbc) {
        this.buscar = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_autenticar_admin")
                .returningResultSet("fila", CredencialAdmin.MAPPER);
        this.marcarAcceso = new SimpleJdbcCall(jdbc).withProcedureName("sp_marcar_acceso_admin");
    }

    /** La cuenta con ese usuario, si existe. Vacio no es un error: es un usuario que no existe. */
    public Optional<CredencialAdmin> buscar(String usuario) {
        List<CredencialAdmin> fila = Resultados.lista(buscar.execute(new MapSqlParameterSource()
                .addValue("p_usuario", usuario)), "fila");
        return fila.stream().findFirst();
    }

    /**
     * Apunta que entro. Se llama <b>despues</b> de comprobar el hash, nunca
     * antes: si no, un intento fallido dejaria escrito un acceso que no ocurrio.
     */
    public void marcarAcceso(int id) {
        marcarAcceso.execute(new MapSqlParameterSource().addValue("p_id", id));
    }
}
